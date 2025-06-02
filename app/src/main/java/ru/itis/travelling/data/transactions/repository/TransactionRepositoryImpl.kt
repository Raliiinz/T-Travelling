package ru.itis.travelling.data.transactions.repository

import ru.itis.travelling.data.network.ApiHelper
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.data.transactions.mapper.TransactionDetailsMapper
import ru.itis.travelling.data.transactions.mapper.TransactionMapper
import ru.itis.travelling.data.transactions.remote.api.TransactionApi
import ru.itis.travelling.domain.transactions.model.Transaction
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import ru.itis.travelling.domain.transactions.repository.TransactionRepository
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionApi: TransactionApi,
    private val apiHelper: ApiHelper,
    private val transactionMapper: TransactionMapper,
    private val transactionDetailsMapper: TransactionDetailsMapper
) : TransactionRepository {

    override suspend fun getTransactions(travelId: String): ResultWrapper<List<Transaction>> {
        return apiHelper.safeApiCall {
            val response = transactionApi.getTransactions(travelId.toLong())
            val body = apiHelper.handleResponse(response)
            body.map { transactionMapper.mapToDomain(it) }
        }
    }

    override suspend fun createTransaction(
        travelId: String,
        transactionDetails: TransactionDetails
    ): ResultWrapper<TransactionDetails> {
        return apiHelper.safeApiCall {
            val request = transactionDetailsMapper.mapToRequest(transactionDetails)
            val response = transactionApi.createTransaction(travelId.toLong(), request)
            val body = apiHelper.handleResponse(response)
            transactionDetailsMapper.mapToResponse(body)
        }
    }

    override suspend fun getTransactionDetails(transactionId: String): ResultWrapper<TransactionDetails> {
        return apiHelper.safeApiCall {
            val response = transactionApi.getTransactionDetails(transactionId.toLong())
            val body = apiHelper.handleResponse(response)
            transactionDetailsMapper.mapToResponse(body)
        }
    }
}
