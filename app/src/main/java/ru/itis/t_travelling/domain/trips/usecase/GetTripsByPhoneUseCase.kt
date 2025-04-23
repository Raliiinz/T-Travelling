package ru.itis.t_travelling.domain.trips.usecase

import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class GetTripsByPhoneUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(phoneNumber: String): List<Trip> {
        return tripRepository.getTripsByPhone(phoneNumber)
    }
}