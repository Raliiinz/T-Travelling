package ru.itis.t_travelling.presentation.trips.fragments

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
import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.usecase.DeleteTripUseCase
import ru.itis.t_travelling.domain.trips.usecase.GetTripDetailsUseCase
import ru.itis.t_travelling.domain.trips.usecase.LeaveTripUseCase
import ru.itis.t_travelling.presentation.base.navigation.Navigator
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
                if (trip != null) {
                    delay(2000)
                    _tripState.update { TripDetailsState.Success(trip) }
                } else {
                    _events.emit(TripDetailsEvent.Error("Поездка не найдена"))
                }
            } catch (e: Exception) {
                _events.emit(TripDetailsEvent.Error(e.message ?: "Не удалось загрузить информацию о поездке"))
            }
        }
    }

    fun onEditClicked(currentUserPhone: String, trip: Trip) {
        viewModelScope.launch {
            val isAdmin = checkAdminStatusUseCase(currentUserPhone, trip)

            if (isAdmin) {
                navigator.navigateToEditTrip(trip.id)
            } else {
                _events.emit(TripDetailsEvent.ShowAdminAlert)
            }
        }
    }

    fun onLeaveOrDeleteClicked(currentUserPhone: String, trip: Trip) {
        viewModelScope.launch {
            val isAdmin = checkAdminStatusUseCase(currentUserPhone, trip)
            if (isAdmin) {
                _events.emit(TripDetailsEvent.ShowDeleteConfirmation)
            } else {
                _events.emit(TripDetailsEvent.ShowLeaveConfirmation)
            }
        }
    }

    fun confirmLeaveTrip(tripId: String, userPhone: String) {
        viewModelScope.launch {
            try {
                leaveTripUseCase(tripId, userPhone)
                _events.emit(TripDetailsEvent.NavigateToTrips)
            } catch (e: Exception) {
                _events.emit(TripDetailsEvent.Error(e.message ?: "Не удалось покинуть поездку"))
            }
        }
    }

    fun confirmDeleteTrip(tripId: String) {
        viewModelScope.launch {
            try {
                deleteTripUseCase(tripId)
                _events.emit(TripDetailsEvent.NavigateToTrips)
            } catch (e: Exception) {
                _events.emit(TripDetailsEvent.Error(e.message ?: "Не удалось удалить поездку"))
            }
        }
    }

    private fun checkAdminStatusUseCase(userPhone: String, trip: Trip): Boolean {
        return trip.admin.phone == userPhone
    }

    fun navigateToTrips(phone: String) {
        viewModelScope.launch {
            navigator.navigateToTripsFragment(phone)
        }
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