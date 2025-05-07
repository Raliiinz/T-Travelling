package ru.itis.travelling.presentation.trips.fragments.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.domain.contacts.model.Contact
import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.usecase.CreateTripUseCase
import ru.itis.travelling.domain.contacts.usecase.GetContactsUseCase
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import ru.itis.travelling.presentation.utils.PhoneNumberUtils.formatPhoneNumber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AddTripViewModel @Inject constructor(
    private val createTripUseCase: CreateTripUseCase,
    private val getContactsUseCase: GetContactsUseCase,
    private val navigator: Navigator
) : ViewModel() {

    private val _contactsState = MutableStateFlow<List<Contact>>(emptyList())
    val contactsState: StateFlow<List<Contact>> = _contactsState

    private val _uiState = MutableStateFlow<AddTripUiState>(AddTripUiState.Idle)
    val uiState: StateFlow<AddTripUiState> = _uiState

    private val _datesState = MutableStateFlow(
        LocalDate.now() to LocalDate.now().plusDays(1)
    )
    val datesState: StateFlow<Pair<LocalDate, LocalDate>> = _datesState

    private val _fullParticipants = MutableStateFlow<List<Participant>>(emptyList())
    val fullParticipants: StateFlow<List<Participant>> = _fullParticipants

    private val _events = MutableSharedFlow<AddTripEvent>()
    val events: SharedFlow<AddTripEvent> = _events

    fun updateDates(newDates: Pair<LocalDate, LocalDate>) {
        _datesState.update { newDates }
    }

    fun initializeWithAdmin(adminPhone: String) {
        val formattedPhone = formatPhoneNumber(adminPhone)
        _fullParticipants.update { listOf(
            Participant(id = adminPhone, phone = formattedPhone)
        )}
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

    fun addParticipants(newParticipants: List<Contact>, adminPhone: String) {
        _fullParticipants.update { currentList ->
            val newSelectedIds = newParticipants.map { it.id.toString() }.toSet()
            val existingIds = currentList.map { it.id }.toSet()

            val (admin, others) = currentList.partition { it.phone == formatPhoneNumber(adminPhone) }
            val filteredOthers = others.filter { newSelectedIds.contains(it.id) }

            val participantsToAdd = newParticipants
                .filterNot { existingIds.contains(it.id.toString()) }
                .map {
                    Participant(
                        id = it.id.toString(),
                        phone = formatPhoneNumber(it.phoneNumber)
                    )
                }

            admin + filteredOthers + participantsToAdd
        }
    }

    fun createTrip(title: String, cost: String, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { AddTripUiState.Loading }
            delay(2000)

            runCatching {
                validateTripData(title, cost)
                val normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(phoneNumber)

                val trip = Trip(
                    id = generateTripId(),
                    destination = title.trim(),
                    startDate = _datesState.value.first.toString(),
                    endDate = _datesState.value.second.toString(),
                    price = (cost.toDoubleOrNull() ?: 0.0).toString(),
                    admin = Participant(
                        id = normalizedPhone,
                        phone = normalizedPhone
                    ),
                    participants = _fullParticipants.value.map {
                        it.copy(phone = PhoneNumberUtils.normalizePhoneNumber(it.phone))
                    }.toMutableList()
                )
                createTripUseCase.invoke(trip)
                trip
            }.onSuccess { trip ->
                println("DEBUG: Trip created successfully! Trip: $trip")
                handleTripCreationSuccess(trip)
            }.onFailure { e->
                println("DEBUG: Failed to create trip. Error: ${e.message}")
                handleTripCreationError(e)
            }
        }
    }

    private fun handleTripCreationSuccess(trip: Trip) {
        viewModelScope.launch {
            clearFormState()
            _uiState.update { AddTripUiState.Success }
            navigateToTrips(trip.admin.phone)
        }
    }

    private suspend fun handleTripCreationError(e: Throwable) {
        val message = if (e is ValidationException) {
            e.message ?: "Validation error"
        } else {
            e.message ?: "Failed to create trip"
        }
        _events.emit(AddTripEvent.Error(message))
    }

    private fun validateTripData(title: String, cost: String) {
        require(title.isNotBlank()) { "Trip title cannot be empty" }
        require(cost.isNotBlank()) { "Trip cost cannot be empty" }
        require(cost.matches(COST_REGEX)) { "Invalid cost format" }
        require(_fullParticipants.value.isNotEmpty()) { "Select at least one participant" }
    }

    private fun generateTripId(): String {
        return "TRIP_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }

    private fun clearFormState() {
        _fullParticipants.update { emptyList() }
    }

    fun navigateToTrips(phoneNumber: String) {
        viewModelScope.launch {
            navigator.navigateToTripsFragment(phoneNumber)
        }
    }

    companion object {
        private val COST_REGEX = Regex("^\\d+(\\.\\d{1,2})?$")
    }

    class ValidationException(message: String) : Exception(message)

    sealed class AddTripUiState {
        object Idle : AddTripUiState()
        object Loading : AddTripUiState()
        object Success : AddTripUiState()
    }

    sealed class AddTripEvent {
        data class Error(val message: String) : AddTripEvent()
    }
}