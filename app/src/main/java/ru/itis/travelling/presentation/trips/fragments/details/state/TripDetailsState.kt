package ru.itis.travelling.presentation.trips.fragments.details.state

import ru.itis.travelling.domain.trips.model.TripDetails

sealed class TripDetailsState {
    object Loading : TripDetailsState()
    data class Success(val trip: TripDetails) : TripDetailsState()
}