package ru.itis.t_travelling.presentation.trips.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.repository.TripRepository
import javax.inject.Inject


@HiltViewModel
class TripsViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _tripsState = MutableStateFlow<TripsState>(TripsState.Loading)
    val tripsState: StateFlow<TripsState> = _tripsState

    fun loadTrips(phoneNumber: String) {
        viewModelScope.launch {
            try {
                val trips = tripRepository.getTripsByPhone(phoneNumber)
                _tripsState.value = TripsState.Success(trips)
            } catch (e: Exception) {
                _tripsState.value = TripsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class TripsState {
        object Loading : TripsState()
        data class Success(val trips: List<Trip>) : TripsState()
        data class Error(val message: String) : TripsState()
    }
}