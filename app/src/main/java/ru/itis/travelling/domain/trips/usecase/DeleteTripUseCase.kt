package ru.itis.travelling.domain.trips.usecase

import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class DeleteTripUseCase @Inject constructor(
    private val tripsRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String) {
        tripsRepository.deleteTrip(tripId)
    }
}
