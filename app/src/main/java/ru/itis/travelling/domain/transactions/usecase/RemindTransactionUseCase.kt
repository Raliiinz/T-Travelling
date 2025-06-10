package ru.itis.travelling.domain.transactions.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.transactions.repository.TransactionRepository
import javax.inject.Inject

class RemindTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(transactionId: String): ResultWrapper<Unit> {
        return withContext(dispatcher) {
            repository.remindTransaction(transactionId)
        }
    }
}