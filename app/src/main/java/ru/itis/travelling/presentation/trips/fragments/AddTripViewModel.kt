package ru.itis.travelling.presentation.trips.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.domain.trips.model.Contact
import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.usecase.CreateTripUseCase
import ru.itis.travelling.domain.trips.usecase.GetContactsUseCase
import ru.itis.travelling.presentation.base.navigation.Navigator
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

    fun updateDates(newDates: Pair<LocalDate, LocalDate>) {
        _datesState.value = newDates
    }

    fun initializeWithAdmin(adminPhone: String) {
        _fullParticipants.update {
            listOf(Participant(id = adminPhone, phone = adminPhone))
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = AddTripUiState.Loading
            try {
                _contactsState.value = getContactsUseCase()
                _uiState.value = AddTripUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AddTripUiState.Error(
                    if (e is SecurityException) "Permission denied"
                    else e.message ?: "Failed to load contacts"
                )
            }
        }
    }

    fun addParticipants(newParticipants: List<Contact>, adminPhone: String) {
        _fullParticipants.update { currentList ->
            val newSelectedIds = newParticipants.map { it.id.toString() }.toSet()
            val existingIds = currentList.map { it.id }.toSet()

            val (admin, others) = currentList.partition { it.phone == adminPhone }
            val filteredOthers = others.filter { newSelectedIds.contains(it.id) }

            val participantsToAdd = newParticipants
                .filterNot { existingIds.contains(it.id.toString()) }
                .map { Participant(it.id.toString(), it.phoneNumber) }

            admin + filteredOthers + participantsToAdd
        }
    }

    fun createTrip(title: String, cost: String, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = AddTripUiState.Loading

            runCatching {
                validateTripData(title, cost)
                val trip = Trip(
                    id = generateTripId(),
                    destination = title.trim(),
                    startDate = _datesState.value.first.toString(),
                    endDate = _datesState.value.second.toString(),
                    price = (cost.toDoubleOrNull() ?: 0.0).toString(),
                    admin = Participant(
                        id = phoneNumber,
                        phone = phoneNumber,
                    ),
                    participants = _fullParticipants.value as MutableList<Participant>
                )
                createTripUseCase.invoke(trip)
            }.onSuccess {
                ::handleTripCreationSuccess
            }.onFailure {
                ::handleTripCreationError
            }
        }
    }

    private fun handleTripCreationSuccess(trip: Trip) {
        clearFormState()
        _uiState.value = AddTripUiState.Success
        navigator.navigateToTripsFragment(trip.admin.phone)
    }

    private fun handleTripCreationError(e: Throwable) {
        _uiState.value = when (e) {
            is ValidationException -> AddTripUiState.Error(e.message ?: "Validation error")
            else -> AddTripUiState.Error(e.message ?: "Failed to create trip")
        }
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
        _fullParticipants.value = emptyList()
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
        data class Error(val message: String) : AddTripUiState()
    }
}