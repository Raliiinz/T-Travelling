package ru.itis.t_travelling.presentation.trips.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _tripsState = MutableStateFlow<TripsState>(TripsState.Loading)
    val tripsState: StateFlow<TripsState> = _tripsState

    fun loadTrips(phoneNumber: String) {
        viewModelScope.launch {
            _tripsState.update { TripsState.Loading }
            try {
                val trips = getTripsByPhoneUseCase.invoke(phoneNumber)
                _tripsState.update { TripsState.Success(trips) }
            } catch (e: Exception) {
                _tripsState.update { TripsState.Error(e.message ?: "Unknown error") }
            }
        }
    }

    fun onTripClicked(tripId: String, phoneNumber: String) {
        navigator.navigateToTripDetailsFragment(tripId, phoneNumber)
    }

    sealed class TripsState {
        object Loading : TripsState()
        data class Success(val trips: List<Trip>) : TripsState()
        data class Error(val message: String) : TripsState()
    }
}