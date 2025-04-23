package ru.itis.t_travelling.presentation.trips.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.usecase.GetTripDetailsUseCase
import ru.itis.t_travelling.presentation.base.navigation.Navigator
import javax.inject.Inject

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val navigator: Navigator
) : ViewModel() {

    private val _tripState = MutableStateFlow<TripDetailsState>(TripDetailsState.Loading)
    val tripState: StateFlow<TripDetailsState> = _tripState

    fun loadTripDetails(tripId: String) {
        viewModelScope.launch {
            _tripState.update { TripDetailsState.Loading }
            try {
                val trip = getTripDetailsUseCase(tripId)
                _tripState.update {
                    if (trip != null) {
                        TripDetailsState.Success(trip)
                    } else {
                        TripDetailsState.Error("Trip not found")
                    }
                }
            } catch (e: Exception) {
                _tripState.update {
                    TripDetailsState.Error(e.message ?: "Failed to load trip details")
                }
            }
        }
    }

    fun navigateToTrips(phone: String) {
        viewModelScope.launch {
            navigator.navigateToTripsFragment(phone)
        }
    }

    sealed class TripDetailsState {
        object Loading : TripDetailsState()
        data class Success(val trip: Trip) : TripDetailsState()
        data class Error(val message: String) : TripDetailsState()
    }
}
