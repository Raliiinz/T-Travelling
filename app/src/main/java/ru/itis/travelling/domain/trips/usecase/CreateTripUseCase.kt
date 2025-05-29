package ru.itis.travelling.domain.trips.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(trip: TripDetails): ResultWrapper<TripDetails> {
        return withContext(dispatcher) {
            tripRepository.createTrip(trip)
        }
    }
}
