package ru.itis.travelling.presentation.trips.fragments.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.profile.model.ParticipantDto
import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.domain.trips.usecase.ConfirmParticipationUseCase
import ru.itis.travelling.domain.trips.usecase.DeleteTripUseCase
import ru.itis.travelling.domain.trips.usecase.DenyParticipationUseCase
import ru.itis.travelling.domain.trips.usecase.GetTripDetailsUseCase
import ru.itis.travelling.domain.trips.usecase.LeaveTripUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.trips.fragments.details.state.InvitationUiState
import ru.itis.travelling.presentation.trips.fragments.details.state.TripDetailsEvent
import ru.itis.travelling.presentation.trips.fragments.details.state.TripDetailsState
import ru.itis.travelling.presentation.trips.util.DateUtils
import ru.itis.travelling.presentation.trips.util.FormatUtils
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import javax.inject.Inject

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val leaveTripUseCase: LeaveTripUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val confirmParticipationUseCase: ConfirmParticipationUseCase,
    private val denyParticipationUseCase: DenyParticipationUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    private val navigator: Navigator
) : ViewModel() {

    private val _tripState = MutableStateFlow<TripDetailsState>(TripDetailsState.Loading)
    val tripState: StateFlow<TripDetailsState> = _tripState

    private val _events = MutableSharedFlow< TripDetailsEvent>()
    val events: SharedFlow<TripDetailsEvent> = _events

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent

    private val _uiState = MutableStateFlow<InvitationUiState>(InvitationUiState.Idle)
    val uiState: StateFlow<InvitationUiState> = _uiState

    fun acceptInvitation(tripId: String) {
        viewModelScope.launch {
            _uiState.value = InvitationUiState.Loading
            when (val result = confirmParticipationUseCase(tripId)) {
                is ResultWrapper.Success -> {
                    _uiState.value = InvitationUiState.Success(
                        message = R.string.invitation_accepted,
                        shouldHideActions = true
                    )
                }
                is ResultWrapper.GenericError -> {
                    _uiState.value = InvitationUiState.Error(
                        message = result.error ?: "Unknown error",
                        errorCode = result.code
                    )
                }
                ResultWrapper.NetworkError -> {
                    _uiState.value = InvitationUiState.Error(
                        message = R.string.error_network
                    )
                }
            }
        }
    }

    fun declineInvitation(tripId: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = InvitationUiState.Loading
            when (val result = denyParticipationUseCase(tripId)) {
                is ResultWrapper.Success -> {
                    navigator.navigateToTripsFragment(phone)
                }
                is ResultWrapper.GenericError -> {
                    _uiState.value = InvitationUiState.Error(
                        message = result.error ?: "Unknown error",
                        errorCode = result.code
                    )
                }
                ResultWrapper.NetworkError -> {
                    _uiState.value = InvitationUiState.Error(
                        message = R.string.error_network
                    )
                }
            }
        }
    }

    fun loadTripDetails(tripId: String) {
        viewModelScope.launch {
            _tripState.update { TripDetailsState.Loading }
            when (val result = getTripDetailsUseCase(tripId)) {
                is ResultWrapper.Success -> {
                    result.value.let { trip ->
                        val formattedTrip = trip.copy(
                            price = FormatUtils.formatPriceWithThousands(trip.price),
                            startDate = DateUtils.formatDateForDisplay(trip.startDate),
                            endDate = DateUtils.formatDateForDisplay(trip.endDate),
                            participants = prepareParticipantsList(trip)
                        )
                        _tripState.update { TripDetailsState.Success(formattedTrip) }
                    }

                }

                is ResultWrapper.GenericError -> {
                    handleTripError(result.code)
                }

                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
        }
    }

    fun onEditClicked(currentUserPhone: String) {
        viewModelScope.launch {
            when (val currentState = _tripState.value) {
                is TripDetailsState.Success -> {
                    val isAdmin = checkAdminStatus(currentUserPhone, currentState.trip)
                    if (isAdmin) {
                        navigator.showAddTripBottomSheet(currentUserPhone, currentState.trip.id)
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

    fun confirmLeaveTrip() {
        viewModelScope.launch {
            when (val currentState = _tripState.value) {
                is TripDetailsState.Success -> {
                    when (val result = leaveTripUseCase(currentState.trip.id)) {
                        is ResultWrapper.Success -> {
                            _events.emit(TripDetailsEvent.NavigateToTrips)
                        }

                        is ResultWrapper.GenericError -> {
                            handleTripError(result.code)
                        }

                        is ResultWrapper.NetworkError -> {
                            _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                        }
                    }
                }
                else -> _events.emit(TripDetailsEvent.Error("Trip data not loaded"))
            }
        }
    }

    fun confirmDeleteTrip(tripId: String) {
        viewModelScope.launch {
            _tripState.update { TripDetailsState.Loading }
            when (val result = deleteTripUseCase(tripId)) {
                is ResultWrapper.Success -> {
                    _events.emit(TripDetailsEvent.NavigateToTrips)
                }
                is ResultWrapper.GenericError -> {
                    handleTripError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
        }
    }

    fun confirmDeleteTrip() {
        viewModelScope.launch {
            when (val currentState = _tripState.value) {
                is TripDetailsState.Success -> {
                    confirmDeleteTrip(currentState.trip.id)
                }
                else -> _events.emit(TripDetailsEvent.Error("Trip data not loaded"))
            }
        }
    }

    private fun checkAdminStatus(userPhone: String, trip: TripDetails): Boolean {
        return trip.admin.phone == userPhone
    }

    fun navigateToTrips(phone: String) {
        viewModelScope.launch {
            navigator.navigateToTripsFragment(phone)
        }
    }

    fun onTransactionsClicked(phone: String) {
        viewModelScope.launch {
            when (val currentState = _tripState.value) {
                is TripDetailsState.Success -> {
                    navigator.navigateToTransactionsFragment(currentState.trip.id, phone)
                }
                else -> _events.emit(TripDetailsEvent.Error("Trip data not loaded"))
            }
        }
    }

    private fun prepareParticipantsList(trip: TripDetails): MutableList<ParticipantDto> {
        val allParticipants = mutableListOf<ParticipantDto>()

        val formattedAdmin = trip.admin.copy(
            phone = PhoneNumberUtils.formatPhoneNumber(trip.admin.phone)
        )
        allParticipants.add(formattedAdmin)

        trip.participants.forEach { participant ->
            val formattedParticipant = participant.copy(
                phone = PhoneNumberUtils.formatPhoneNumber(participant.phone)
            )
            allParticipants.add(formattedParticipant)
        }

        return allParticipants
    }

    private suspend fun handleTripError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)
        val messageRes = when (reason) {
            ErrorEvent.FailureReason.Unauthorized -> R.string.error_unauthorized_trip
            ErrorEvent.FailureReason.NotFound -> R.string.error_not_found_trip
            ErrorEvent.FailureReason.Forbidden -> R.string.error_forbidden
            ErrorEvent.FailureReason.Conflict -> R.string.error_conflict
            ErrorEvent.FailureReason.Server -> R.string.error_server
            ErrorEvent.FailureReason.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
    }
}
