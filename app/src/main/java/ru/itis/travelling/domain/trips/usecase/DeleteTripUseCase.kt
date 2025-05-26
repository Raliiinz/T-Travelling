package ru.itis.travelling.domain.trips.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class DeleteTripUseCase @Inject constructor(
    private val tripsRepository: TripRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(tripId: String) : ResultWrapper<Unit> {
        return withContext(dispatcher) {
            tripsRepository.deleteTrip(tripId)
        }
    }
}
