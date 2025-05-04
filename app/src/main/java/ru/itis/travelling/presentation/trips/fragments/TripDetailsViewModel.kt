package ru.itis.travelling.presentation.trips.fragments

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
import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.usecase.DeleteTripUseCase
import ru.itis.travelling.domain.trips.usecase.GetTripDetailsUseCase
import ru.itis.travelling.domain.trips.usecase.LeaveTripUseCase
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.trips.util.FormatUtils
import javax.inject.Inject

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val leaveTripUseCase: LeaveTripUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val navigator: Navigator
) : ViewModel() {

    private val _tripState = MutableStateFlow<TripDetailsState>(TripDetailsState.Loading)
    val tripState: StateFlow<TripDetailsState> = _tripState

    private val _events = MutableSharedFlow< TripDetailsEvent>()
    val events: SharedFlow<TripDetailsEvent> = _events

    fun loadTripDetails(tripId: String) {
        viewModelScope.launch {
            _tripState.update { TripDetailsState.Loading }
            try {
                val trip = getTripDetailsUseCase(tripId)
                val formattedTrip = trip?.copy(
                    price = FormatUtils.formatPriceWithThousands(trip.price),
                    participants = prepareParticipantsList(trip)
                )
                if (formattedTrip != null) {
                    delay(2000)
                    _tripState.update { TripDetailsState.Success(formattedTrip) }
                } else {
                    _events.emit(TripDetailsEvent.Error("Поездка не найдена"))
                }
            } catch (e: Exception) {
                _events.emit(TripDetailsEvent.Error(e.message ?: "Не удалось загрузить информацию о поездке"))
            }
        }
    }

    fun onEditClicked(currentUserPhone: String) {
        viewModelScope.launch {
            when (val currentState = _tripState.value) {
                is TripDetailsState.Success -> {
                    val isAdmin = checkAdminStatus(currentUserPhone, currentState.trip)
                    if (isAdmin) {
                        navigator.navigateToEditTrip(currentState.trip.id)
                    } else {
                        _events.emit(TripDetailsEvent.ShowAdminAlert)
                    }
                }
                else -> _events.emit(TripDetailsEvent.Error("Trip data not loaded"))
            }
        }
    }

    fun onLeaveOrDeleteClicked(currentUserPhone: String) {
        viewModelScope.launch {
            when (val currentState = _tripState.value) {
                is TripDetailsState.Success -> {
                    val isAdmin = checkAdminStatus(currentUserPhone, currentState.trip)
                    if (isAdmin) {
                        _events.emit(TripDetailsEvent.ShowDeleteConfirmation)
                    } else {
                        _events.emit(TripDetailsEvent.ShowLeaveConfirmation)
                    }
                }
                else -> _events.emit(TripDetailsEvent.Error("Trip data not loaded"))
            }
        }
    }

    fun confirmLeaveTrip(userPhone: String) {
        viewModelScope.launch {
            when (val currentState = _tripState.value) {
                is TripDetailsState.Success -> {
                    try {
                        leaveTripUseCase(currentState.trip.id, userPhone)
                        _events.emit(TripDetailsEvent.NavigateToTrips)
                    } catch (e: Exception) {
                        _events.emit(TripDetailsEvent.Error(e.message ?: "Не удалось покинуть поездку"))
                    }
                }
                else -> _events.emit(TripDetailsEvent.Error("Trip data not loaded"))
            }
        }
    }

    fun confirmDeleteTrip() {
        viewModelScope.launch {
            when (val currentState = _tripState.value) {
                is TripDetailsState.Success -> {
                    try {
                        deleteTripUseCase(currentState.trip.id)
                        _events.emit(TripDetailsEvent.NavigateToTrips)
                    } catch (e: Exception) {
                        _events.emit(TripDetailsEvent.Error(e.message ?: "Не удалось удалить поездку"))
                    }
                }
                else -> _events.emit(TripDetailsEvent.Error("Trip data not loaded"))
            }
        }
    }

    private fun checkAdminStatus(userPhone: String, trip: Trip): Boolean {
        return trip.admin.phone == userPhone
    }

    fun navigateToTrips(phone: String) {
        viewModelScope.launch {
            navigator.navigateToTripsFragment(phone)
        }
    }

    private fun prepareParticipantsList(trip: Trip): MutableList<Participant> {
        val allParticipants = mutableListOf<Participant>()
        allParticipants.add(trip.admin)
        allParticipants.addAll(trip.participants.filter { p -> p.id != trip.admin.id })
        return allParticipants
    }

    sealed class TripDetailsState {
        object Loading : TripDetailsState()
        data class Success(val trip: Trip) : TripDetailsState()
    }

    sealed class TripDetailsEvent {
        data class Error(val message: String) : TripDetailsEvent()
        object ShowAdminAlert : TripDetailsEvent()
        object ShowLeaveConfirmation : TripDetailsEvent()
        object ShowDeleteConfirmation : TripDetailsEvent()
        object NavigateToTrips : TripDetailsEvent()
    }
}
