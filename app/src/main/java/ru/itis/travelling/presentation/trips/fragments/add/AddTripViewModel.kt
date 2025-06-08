package ru.itis.travelling.presentation.trips.fragments.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.contacts.model.Contact
import ru.itis.travelling.domain.profile.model.ParticipantDto
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
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.toMutableList
import kotlin.collections.toSet

@HiltViewModel
class AddTripViewModel @Inject constructor(
    private val createTripUseCase: CreateTripUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    private val navigator: Navigator
) : ViewModel() {

    private val _tripState = MutableStateFlow<TripDetails?>(null)
    val tripState: StateFlow<TripDetails?> = _tripState

    val tripTitle: StateFlow<String> = tripState.map { it?.destination.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val tripCost: StateFlow<String> = tripState.map { it?.price.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val datesState: StateFlow<Pair<LocalDate, LocalDate>> = tripState.map {
        (it?.startDate?.toLocalDate() ?: LocalDate.now()) to
                (it?.endDate?.toLocalDate() ?: LocalDate.now().plusDays(1))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now() to LocalDate.now().plusDays(1))

    val admin: StateFlow<ParticipantDto?> = tripState.map {
        it?.admin?.copy(phone = formatPhoneNumber(it.admin.phone))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val fullParticipants: StateFlow<List<ParticipantDto>> = tripState.map {
        it?.participants?.map { p -> p.copy(phone = formatPhoneNumber(p.phone)) } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    private val _contactsState = MutableStateFlow<List<Contact>>(emptyList())
    val contactsState: StateFlow<List<Contact>> = _contactsState

    private val _uiState = MutableStateFlow<AddTripUiState>(AddTripUiState.Idle)
    val uiState: StateFlow<AddTripUiState> = _uiState

    private val _formattedDates = MutableStateFlow<Pair<String, String>>("" to "")
    val formattedDates: StateFlow<Pair<String, String>> = _formattedDates

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
        _tripState.update { current ->
            current?.copy(
                startDate = newDates.first.toString(),
                endDate = newDates.second.toString()
            )
        }
    }

    fun loadTripForEditing(tripId: String) {
        _isEditMode.value = true
        viewModelScope.launch {
            _uiState.update { AddTripUiState.Loading }
            when (val result = getTripDetailsUseCase(tripId)) {
                is ResultWrapper.Success -> {
                    _tripState.value = result.value
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
        _tripState.value = TripDetails(
            id = "",
            destination = "",
            startDate = LocalDate.now().toString(),
            endDate = LocalDate.now().plusDays(1).toString(),
            price = "",
            admin = ParticipantDto(phone = formattedPhone),
            participants = mutableListOf()
        )
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.update { AddTripUiState.Loading }
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
        _tripState.update { current ->
            current?.let {
                val newPhones = newParticipants.map {
                    PhoneNumberUtils.normalizePhoneNumber(it.phoneNumber)
                }.toSet()
                val currentParticipants = it.participants.map {
                    it.copy(phone = PhoneNumberUtils.normalizePhoneNumber(it.phone))
                }
                val updatedParticipants = currentParticipants
                    .filter { participant ->
                        newPhones.contains(PhoneNumberUtils.normalizePhoneNumber(participant.phone))
                    }
                    .toMutableList()
                newParticipants.forEach { contact ->
                    val normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(contact.phoneNumber)
                    if (!updatedParticipants.any {
                            PhoneNumberUtils.normalizePhoneNumber(it.phone) == normalizedPhone
                        }) {
                        updatedParticipants.add(ParticipantDto(phone = contact.phoneNumber))
                    }
                }
                it.copy(participants = updatedParticipants)
            }
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
            _tripState.value?.participants.isNullOrEmpty() -> {
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

            val currentTrip = tripState.value
            if (currentTrip == null) return@launch

            val normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(phoneNumber)
            val tripToSubmit = currentTrip.copy(
                id = tripId ?: "",
                destination = title.trim(),
                startDate = currentTrip.startDate,
                endDate = currentTrip.endDate,
                price = (cost.toDoubleOrNull() ?: 0.0).toString(),
                admin = ParticipantDto(phone = normalizedPhone),
                participants = currentTrip.participants
                    .filterNot { it.phone == normalizedPhone }
                    .map { it.copy(phone = PhoneNumberUtils.normalizePhoneNumber(it.phone)) }
                    .toMutableList()
            )

            val result = if (tripId == null) {
                createTripUseCase(tripToSubmit)
            } else {
                updateTripUseCase(tripToSubmit)
            }

            when (result) {
                is ResultWrapper.Success<*> -> {
                    handleTripSaveSuccess(tripToSubmit, isNewTrip = tripId == null)
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
        _tripState.update {
            it?.copy(participants = mutableListOf())
        }
    }

    fun observeCombinedParticipants(): Flow<List<ParticipantDto>> {
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
