package ru.itis.t_travelling.domain.trips.usecase

import ru.itis.t_travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class LeaveTripUseCase @Inject constructor(
    private val tripsRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String, userPhone: String) {
        tripsRepository.leaveTrip(tripId, userPhone)
    }
}