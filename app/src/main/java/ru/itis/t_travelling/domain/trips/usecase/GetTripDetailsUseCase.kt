package ru.itis.t_travelling.domain.trips.usecase

import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class GetTripDetailsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String): Trip? {
        return tripRepository.getTripDetails(tripId)
    }
}