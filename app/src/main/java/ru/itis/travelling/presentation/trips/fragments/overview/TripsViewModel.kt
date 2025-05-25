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
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.usecase.GetActiveTripsUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.trips.fragments.add.AddTripViewModel.AddTripUiState
import ru.itis.travelling.presentation.trips.util.DateUtils
import ru.itis.travelling.presentation.trips.util.FormatUtils
import javax.inject.Inject


@HiltViewModel
class TripsViewModel @Inject constructor(
    private val getActiveTripsUseCase: GetActiveTripsUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    private val navigator: Navigator
) : ViewModel() {

    private val _tripsState = MutableStateFlow<TripsState>(TripsState.Loading)
    val tripsState: StateFlow<TripsState> = _tripsState

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent

    fun loadTrips() {
        viewModelScope.launch {
            _tripsState.update { TripsState.Loading }
            delay(2000)
            when (val result = getActiveTripsUseCase()) {
                is ResultWrapper.Success -> {
                    val trips = result.value.map { trip ->
                        trip.copy(
                            price = FormatUtils.formatPriceWithThousands(trip.price.toString()),
                            startDate = DateUtils.formatDateForDisplay(trip.startDate),
                            endDate = DateUtils.formatDateForDisplay(trip.endDate)
                        )
                    }
                    _tripsState.update { TripsState.Success(trips) }
                }

                is ResultWrapper.GenericError -> {
                    handleTripError(result.code)
                }

                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
            _tripsState.update { TripsState.Idle }
        }
    }

    private suspend fun handleTripError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)
        val messageRes = when (reason) {
            ErrorEvent.FailureReason.Unauthorized -> R.string.error_unauthorized_trip
            ErrorEvent.FailureReason.Server -> R.string.error_server
            ErrorEvent.FailureReason.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
    }

    fun onTripClicked(tripId: String, phoneNumber: String) {
        navigator.navigateToTripDetailsFragment(tripId, phoneNumber)
    }

    sealed class TripsState {
        object Idle : TripsState()
        object Loading : TripsState()
        data class Success(val trips: List<Trip>) : TripsState()
    }

}
