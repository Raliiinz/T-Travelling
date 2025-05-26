package ru.itis.travelling.presentation.trips.fragments.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.contacts.model.Contact
import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.domain.trips.usecase.CreateTripUseCase
import ru.itis.travelling.domain.contacts.usecase.GetContactsUseCase
import ru.itis.travelling.domain.trips.usecase.GetTripDetailsUseCase
import ru.itis.travelling.domain.trips.usecase.UpdateTripUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.trips.util.DateUtils
import ru.itis.travelling.presentation.trips.util.DateUtils.toLocalDate
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import ru.itis.travelling.presentation.utils.PhoneNumberUtils.formatPhoneNumber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddTripViewModel @Inject constructor(
    private val createTripUseCase: CreateTripUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    private val navigator: Navigator
) : ViewModel() {

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    private val _tripTitle = MutableStateFlow("")
    val tripTitle: StateFlow<String> = _tripTitle

    private val _tripCost = MutableStateFlow("")
    val tripCost: StateFlow<String> = _tripCost

    private val _contactsState = MutableStateFlow<List<Contact>>(emptyList())
    val contactsState: StateFlow<List<Contact>> = _contactsState

    private val _uiState = MutableStateFlow<AddTripUiState>(AddTripUiState.Idle)
    val uiState: StateFlow<AddTripUiState> = _uiState

    private val _datesState = MutableStateFlow(
        LocalDate.now() to LocalDate.now().plusDays(1)
    )
    val datesState: StateFlow<Pair<LocalDate, LocalDate>> = _datesState

    private val _formattedDates = MutableStateFlow<Pair<String, String>>("" to "")
    val formattedDates: StateFlow<Pair<String, String>> = _formattedDates

    private val _admin = MutableStateFlow<Participant?>(null)
    val admin: StateFlow<Participant?> = _admin

    private val _fullParticipants = MutableStateFlow<List<Participant>>(emptyList())
    val fullParticipants: StateFlow<List<Participant>> = _fullParticipants

    private val _events = MutableSharedFlow<AddTripEvent>()
    val events: SharedFlow<AddTripEvent> = _events

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent

    init {
        viewModelScope.launch {
            datesState.collect { (startDate, endDate) ->
                _formattedDates.value = formatDates(startDate, endDate)
            }
        }
    }

    private fun formatDates(startDate: LocalDate, endDate: LocalDate): Pair<String, String> {
        return DateUtils.formatDateForDisplay(startDate) to DateUtils.formatDateForDisplay(endDate)
    }

    fun updateDates(newDates: Pair<LocalDate, LocalDate>) {
        _datesState.update { newDates }
    }

    fun loadTripForEditing(tripId: String) {
        _isEditMode.value = true
        viewModelScope.launch {
            _uiState.update { AddTripUiState.Loading }
            delay(2000)
            when (val result = getTripDetailsUseCase(tripId)) {
                is ResultWrapper.Success -> {
                    result.value.let { trip ->
                        _tripTitle.value = trip.destination
                        _tripCost.value = trip.price
                        _datesState.update {
                            trip.startDate.toLocalDate() to trip.endDate.toLocalDate()
                        }
                        _admin.value = Participant(phone = formatPhoneNumber(trip.admin.phone))

                        val formattedParticipants = trip.participants.map { participant ->
                            participant.copy(phone = formatPhoneNumber(participant.phone))
                        }

                        _fullParticipants.update { formattedParticipants }
                    }
                    _uiState.update { AddTripUiState.Idle }
                }

                is ResultWrapper.GenericError -> {
                    handleTripError(result.code)
                    _uiState.update { AddTripUiState.Idle }
                }

                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                    _uiState.update { AddTripUiState.Idle }
                }
            }
        }
    }

    fun initializeWithAdmin(adminPhone: String) {
        _isEditMode.value = false
        val formattedPhone = formatPhoneNumber(adminPhone)
        _admin.value = Participant(phone = formattedPhone)
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.update { AddTripUiState.Loading }
            delay(2000)
            try {
                val contacts = getContactsUseCase().map { contact ->
                    contact.copy(phoneNumber = formatPhoneNumber(contact.phoneNumber))
                }
                _contactsState.update { contacts }
                _uiState.update { AddTripUiState.Idle }
            } catch (e: Exception) {
                val message = if (e is SecurityException) {
                    e.message ?: "Permission denied"
                } else {
                    e.message ?: "Failed to load contacts"
                }
                _events.emit(AddTripEvent.Error(message))
            }
        }
    }

    fun addParticipants(newParticipants: List<Contact>) {
        _fullParticipants.update { currentList ->
            val existingIds = currentList.map { it.phone }.toSet()

            val participantsToAdd = newParticipants
                .filterNot { existingIds.contains(it.phoneNumber.toString()) }
                .map {
                    Participant(
                        phone = formatPhoneNumber(it.phoneNumber)
                    )
                }

            currentList + participantsToAdd
        }
    }

    fun saveTrip(tripId: String?, title: String, cost: String, phoneNumber: String) {
        when {
            title.isBlank() -> {
                viewModelScope.launch {
                    _events.emit(AddTripEvent.ValidationError(
                        ValidationErrorEvent.ValidationError(
                            ValidationErrorEvent.ValidationFailureReason.EMPTY_TITLE
                        )
                    ))
                }
                return
            }
            cost.isBlank() -> {
                viewModelScope.launch {
                    _events.emit(AddTripEvent.ValidationError(
                        ValidationErrorEvent.ValidationError(
                            ValidationErrorEvent.ValidationFailureReason.EMPTY_COST
                        )
                    ))
                }
                return
            }
            _fullParticipants.value.isEmpty() -> {
                viewModelScope.launch {
                    _events.emit(AddTripEvent.ValidationError(
                        ValidationErrorEvent.ValidationError(
                            ValidationErrorEvent.ValidationFailureReason.NO_PARTICIPANTS
                        )
                    ))
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { AddTripUiState.Loading }

            val normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(phoneNumber)
            val trip = TripDetails(
                id = tripId ?: "",
                destination = title.trim(),
                startDate = _datesState.value.first.toString(),
                endDate = _datesState.value.second.toString(),
                price = (cost.toDoubleOrNull() ?: 0.0).toString(),
                admin = Participant(phone = normalizedPhone),
                participants = _fullParticipants.value
                    .filterNot { it.phone == normalizedPhone }
                    .map { it.copy(phone = PhoneNumberUtils.normalizePhoneNumber(it.phone)) }
                    .toMutableList()
            )

            val result = if (tripId == null) {
                createTripUseCase(trip)
            } else {
                updateTripUseCase(trip)
            }

            when (result) {
                is ResultWrapper.Success<*> -> {
                    handleTripSaveSuccess(trip, isNewTrip = tripId == null)
                }
                is ResultWrapper.GenericError -> {
                    handleTripError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }

            _uiState.update { AddTripUiState.Idle }
        }
    }

    private fun handleTripSaveSuccess(trip: TripDetails, isNewTrip: Boolean) {
        viewModelScope.launch {
            clearFormState()
            _uiState.update { AddTripUiState.Success }

            if (isNewTrip) {
                navigateToTrips(trip.admin.phone)
            } else {
                navigateToTripDetails(trip.id, trip.admin.phone)
            }
        }
    }

    private suspend fun handleTripError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)
        val messageRes = when (reason) {
            ErrorEvent.FailureReason.BadRequest -> R.string.error_bad_request_add
            ErrorEvent.FailureReason.Unauthorized -> R.string.error_unauthorized_trip
            ErrorEvent.FailureReason.Forbidden -> R.string.error_forbidden
            ErrorEvent.FailureReason.NotFound -> R.string.error_not_found_trip
            ErrorEvent.FailureReason.Server -> R.string.error_server
            ErrorEvent.FailureReason.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
    }

    private fun clearFormState() {
        _fullParticipants.update { emptyList() }
    }

    fun observeCombinedParticipants(): Flow<List<Participant>> {
        return combine(admin, fullParticipants) { admin, participants ->
            if (admin != null) {
                listOf(admin) + participants
            } else {
                participants
            }
        }
    }

    fun navigateToTrips(phoneNumber: String) {
        viewModelScope.launch {
            navigator.navigateToTripsFragment(phoneNumber)
        }
    }

    fun navigateToTripDetails (tripId: String, phoneNumber: String) {
        viewModelScope.launch {
            navigator.navigateToTripDetailsFragment(tripId, phoneNumber)
        }
    }

    sealed class AddTripUiState {
        data object Idle : AddTripUiState()
        data object Loading : AddTripUiState()
        data object Success : AddTripUiState()
    }

    sealed class ValidationErrorEvent {
        data class ValidationError(val reason: ValidationFailureReason) : ValidationErrorEvent()

        enum class ValidationFailureReason {
            EMPTY_TITLE,
            EMPTY_COST,
            NO_PARTICIPANTS
        }
    }

    sealed class AddTripEvent {
        data class ValidationError(val error: ValidationErrorEvent.ValidationError) : AddTripEvent()
        data class Error(val message: String) : AddTripEvent()
    }
}