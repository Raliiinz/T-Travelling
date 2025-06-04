package ru.itis.travelling.data.transactions.repository

import android.util.Log
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
    private val TAG = "TransactionRepository"

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

            Log.d(TAG, "Creating transaction for travelId: $travelId")
            Log.d(TAG, "TransactionDetails input: $transactionDetails")

            val request = transactionDetailsMapper.mapToRequest(transactionDetails)

            // Логируем сформированный запрос
            Log.d(TAG, "TransactionDetailsRequest to be sent: ${request.toString()}")
            Log.d(TAG, "Request details: " +
                    "category=${request.category}, " +
                    "totalCost=${request.totalCost}, " +
                    "description=${request.description}, " +
                    "createdAt=${request.createdAt}, " +
                    "participantsCount=${request.participant.size}")

            val response = transactionApi.createTransaction(travelId.toLong(), request)

            Log.d(TAG,"Raw response: ${response.raw()}")
            val body = apiHelper.handleResponse(response)

            Log.d(TAG,"TransactionDetailsResponse received: $body")
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
