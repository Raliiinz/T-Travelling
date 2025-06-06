package ru.itis.travelling.domain.transactions.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.transactions.model.Transaction
import ru.itis.travelling.domain.transactions.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(travelId: String): ResultWrapper<List<Transaction>> {
        return withContext(dispatcher) {
            repository.getTransactions(travelId)
        }
    }
}