package ru.itis.travelling.presentation.trips.fragments

import androidx.fragment.app.FragmentActivity
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
    private val navigator: Navigator,
    private val getContactsUseCase: GetContactsUseCase,
) : ViewModel() {

    private val _contactsState = MutableStateFlow<List<Contact>>(emptyList())
    val contactsState: StateFlow<List<Contact>> = _contactsState

    private val _uiState = MutableStateFlow<AddTripUiState>(AddTripUiState.Idle)
    val uiState: StateFlow<AddTripUiState> = _uiState

    private val _datesState = MutableStateFlow<Pair<LocalDate, LocalDate>>(
        Pair(
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        )
    )
    val datesState: StateFlow<Pair<LocalDate, LocalDate>> = _datesState

    fun updateDates(newDates: Pair<LocalDate, LocalDate>) {
        _datesState.value = newDates
    }

    private val _fullParticipants = MutableStateFlow<List<Participant>>(emptyList())
    val fullParticipants: StateFlow<List<Participant>> = _fullParticipants

    fun initializeWithAdmin(adminPhone: String) {
        val admin = Participant(
            id = adminPhone,
            phone = adminPhone,
        )
        _fullParticipants.update { listOf(admin) }
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = AddTripUiState.Loading
            try {
                _contactsState.value = getContactsUseCase()
                _uiState.value = AddTripUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AddTripUiState.Error(e.message ?: "Failed to load contacts")
            }
        }
    }

    fun addParticipants(newParticipants: List<Contact>, adminPhone: String) {
        _fullParticipants.update { currentList ->
            val existingPhones = currentList.map { it.phone }.toSet()
            val admin = currentList.find { it.phone == adminPhone }
            val newParticipantsToAdd = newParticipants.filter { !existingPhones.contains(it.phoneNumber) }
                .map { Participant(it.id.toString(), it.phoneNumber) }

            // Ensure admin is always first
            buildList {
                if (admin != null) add(admin)
                addAll(currentList.filter { it.phone != adminPhone })
                addAll(newParticipantsToAdd)
            }
        }
    }

    fun removeParticipant(participantId: String, adminPhone: String) {
        if (participantId != adminPhone) { // Prevent removing admin
            _fullParticipants.value = _fullParticipants.value.filter { it.id != participantId }
        }
    }

    fun createTrip(title: String, cost: String, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = AddTripUiState.Loading

            runCatching {
                // 1. Валидация данных
                validateTripData(title, cost)

                // 3. Создание объекта поездки
                val trip = Trip(
                    id = generateTripId(), // Генерация ID
                    destination = title.trim(),
                    startDate = _datesState.value.first.toString(),
                    endDate = _datesState.value.second.toString(),
                    price = (cost.toDoubleOrNull() ?: 0.0).toString(), // Безопасное преобразование
                    admin = Participant(
                        id = phoneNumber,
                        phone = phoneNumber,
                    ),
                    participants = _fullParticipants.value as MutableList<Participant>
                )

                // 4. Сохранение поездки
                createTripUseCase.invoke(trip)
            }.onSuccess {
                // 5. Очистка состояния после успешного создания
                clearFormState()
                _uiState.value = AddTripUiState.Success
                navigator.navigateToTripsFragment(phoneNumber)
            }.onFailure { e ->
                // 6. Обработка ошибок
                _uiState.value = when (e) {
                    is ValidationException -> AddTripUiState.Error(e.message ?: "Validation error")
                    else -> AddTripUiState.Error(e.message ?: "Failed to create trip")
                }
            }
        }
    }

// Вспомогательные методы:

    private fun validateTripData(title: String, cost: String) {
        when {
            title.isBlank() -> throw ValidationException("Trip title cannot be empty")
            cost.isBlank() -> throw ValidationException("Trip cost cannot be empty")
            !cost.matches(Regex("^\\d+(\\.\\d{1,2})?$")) -> throw ValidationException("Invalid cost format")
            _fullParticipants.value.isEmpty() -> throw ValidationException("Select at least one participant")
        }
    }



    private fun generateTripId(): String {
        return "TRIP_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }

    private fun clearFormState() {
        _fullParticipants.value = emptyList()
    }

    // Класс для кастомных ошибок валидации
    class ValidationException(message: String) : Exception(message)

    sealed class AddTripUiState {
        object Idle : AddTripUiState()
        object Loading : AddTripUiState()
        object Success : AddTripUiState()
        data class Error(val message: String) : AddTripUiState()
        data class ContactsLoaded(val contacts: List<Participant>) : AddTripUiState()
    }


    fun navigateToTrips(phoneNumber: String) {
        viewModelScope.launch {
            navigator.navigateToTripsFragment(phoneNumber)
        }
    }
}