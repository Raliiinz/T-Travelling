package ru.itis.travelling.domain.trips.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class ConfirmParticipationUseCase @Inject constructor(
    private val repository: TripRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(travelId: String): ResultWrapper<Unit> {
        return withContext(dispatcher) {
            repository.confirmParticipation(travelId)
        }
    }
}