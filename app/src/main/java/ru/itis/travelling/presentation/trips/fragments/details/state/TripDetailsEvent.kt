package ru.itis.travelling.presentation.trips.fragments.details.state

sealed class TripDetailsEvent {
    data class Error(val message: String) : TripDetailsEvent()
    object ShowAdminAlert : TripDetailsEvent()
    object ShowLeaveConfirmation : TripDetailsEvent()
    object ShowDeleteConfirmation : TripDetailsEvent()
    object NavigateToTrips : TripDetailsEvent()
}