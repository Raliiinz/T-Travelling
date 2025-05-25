package ru.itis.travelling.domain.trips.usecase

import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class GetTripDetailsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String): TripDetails? {
        return tripRepository.getTripDetails(tripId)
    }
}
