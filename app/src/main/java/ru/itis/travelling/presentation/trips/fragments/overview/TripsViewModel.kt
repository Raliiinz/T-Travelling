package ru.itis.travelling.presentation.trips.fragments.overview

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
import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.domain.trips.usecase.GetTripsByPhoneUseCase
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.trips.util.DateUtils
import ru.itis.travelling.presentation.trips.util.FormatUtils
import javax.inject.Inject


@HiltViewModel
class TripsViewModel @Inject constructor(
    private val getTripsByPhoneUseCase: GetTripsByPhoneUseCase,
    private val navigator: Navigator
) : ViewModel() {

    private val _tripsState = MutableStateFlow<TripsState>(TripsState.Loading)
    val tripsState: StateFlow<TripsState> = _tripsState

    private val _events = MutableSharedFlow<TripsEvent>()
    val events: SharedFlow<TripsEvent> = _events

    fun loadTrips(phoneNumber: String) {
        viewModelScope.launch {
            _tripsState.update { TripsState.Loading }
            delay(2000)
            try {
                val trips = getTripsByPhoneUseCase.invoke(phoneNumber)
                    .map { trip ->
                        trip.copy(
                            price = FormatUtils.formatPriceWithThousands(trip.price),
                            startDate = DateUtils.formatDateForDisplay(trip.startDate),
                            endDate = DateUtils.formatDateForDisplay(trip.endDate)
                        )
                    }
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
        data class Success(val trips: List<TripDetails>) : TripsState()
    }

    sealed class TripsEvent {
        data class Error(val message: String) : TripsEvent()
    }
}
