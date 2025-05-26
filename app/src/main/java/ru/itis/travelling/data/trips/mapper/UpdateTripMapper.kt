package ru.itis.travelling.data.trips.mapper

import ru.itis.travelling.data.trips.remote.model.UpdateTripRequest
import ru.itis.travelling.domain.trips.model.TripDetails
import javax.inject.Inject

class UpdateTripMapper @Inject constructor() {
    fun mapToUpdateRequest(trip: TripDetails): UpdateTripRequest {
        return UpdateTripRequest(
            id = trip.id.toLong(),
            name = trip.destination,
            totalBudget = trip.price.toDoubleOrNull() ?: 0.0,
            dateOfBegin = trip.startDate,
            dateOfEnd = trip.endDate,
            participantPhones = trip.participants.map { it.phone }
        )
    }
}