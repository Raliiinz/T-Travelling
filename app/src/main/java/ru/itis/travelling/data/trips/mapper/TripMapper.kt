package ru.itis.travelling.data.trips.mapper

import ru.itis.travelling.data.trips.remote.model.TripResponse
import ru.itis.travelling.domain.trips.model.Trip
import javax.inject.Inject

class TripMapper @Inject constructor() {
    fun mapToDomain(response: TripResponse): Trip {
        return Trip(
            id = response.id.toString(),
            destination = response.name,
            startDate = response.dateOfBegin,
            endDate = response.dateOfEnd,
            price = response.totalBudget.toString()
        )
    }
}