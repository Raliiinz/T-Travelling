package ru.itis.t_travelling.presentation.trips.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.usecase.GetTripsByPhoneUseCase
import ru.itis.t_travelling.presentation.base.navigation.Navigator
import javax.inject.Inject


@HiltViewModel
class TripsViewModel @Inject constructor(
    private val getTripsByPhoneUseCase: GetTripsByPhoneUseCase,
    private val navigator: Navigator
) : ViewModel() {

    private val _tripsState = MutableStateFlow<TripsState>(TripsState.Idle)
    val tripsState: StateFlow<TripsState> = _tripsState

    private val _events = MutableSharedFlow<TripsEvent>()
    val events: SharedFlow<TripsEvent> = _events

    fun loadTrips(phoneNumber: String) {
        viewModelScope.launch {
            _tripsState.update { TripsState.Loading }
            try {
                val trips = getTripsByPhoneUseCase.invoke(phoneNumber)
                _tripsState.update { TripsState.Success(trips) }
            } catch (e: Exception) {
                _events.emit(TripsEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun onTripClicked(tripId: String, phoneNumber: String) {
        navigator.navigateToTripDetailsFragment(tripId, phoneNumber)
    }

    sealed class TripsState {
        object Idle : TripsState()
        object Loading : TripsState()
        data class Success(val trips: List<Trip>) : TripsState()
    }

    sealed class TripsEvent {
        data class Error(val message: String) : TripsEvent()
    }
}