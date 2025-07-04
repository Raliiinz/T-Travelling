package ru.itis.travelling.domain.transactions.repository

import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.transactions.model.Transaction
import ru.itis.travelling.domain.transactions.model.TransactionDetails

interface TransactionRepository {
    suspend fun getTransactions(transactionId: String): ResultWrapper<List<Transaction>>

    suspend fun createTransaction(
        travelId: String,
        transactionDetails: TransactionDetails
    ): ResultWrapper<TransactionDetails>

    suspend fun getTransactionDetails(transactionId: String): ResultWrapper<TransactionDetails>

    suspend fun deleteTransaction(transactionId: String): ResultWrapper<Unit>

    suspend fun updateTransaction(transactionDetails: TransactionDetails, transactionId: String): ResultWrapper<TransactionDetails>

    suspend fun remindTransaction(transactionId: String): ResultWrapper<Unit>
}
