package ru.itis.travelling.domain.trips.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class GetActiveTripsUseCase @Inject constructor(
    private val repository: TripRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher

) {
    suspend operator fun invoke(): ResultWrapper<List<Trip>> {
        return withContext(dispatcher) {
            repository.getActiveTrips()
        }
    }
}