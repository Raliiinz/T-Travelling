package ru.itis.travelling.domain.transactions.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import ru.itis.travelling.domain.transactions.repository.TransactionRepository
import javax.inject.Inject

class CreateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(travelId: String, transactionDetails: TransactionDetails): ResultWrapper<TransactionDetails> {
        return withContext(dispatcher) {
            repository.createTransaction(travelId, transactionDetails)
        }
    }
}