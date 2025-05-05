package ru.itis.travelling.domain.trips.usecase

import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(trip: Trip) {
        tripRepository.createTrip(trip)
    }
}
