package ru.itis.travelling.data.transactions.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.itis.travelling.data.transactions.remote.model.TransactionDetailsRequest
import ru.itis.travelling.data.transactions.remote.model.TransactionDetailsResponse
import ru.itis.travelling.data.transactions.remote.model.TransactionResponse

interface TransactionApi {
    @GET("/api/v1/transactions")
    suspend fun getTransactions(
        @Query("travelId") travelId: Long
    ): Response<List<TransactionResponse>>

    @POST("/api/v1/transactions")
    suspend fun createTransaction(
        @Query("travelId") travelId: Long,
        @Body request: TransactionDetailsRequest
    ): Response<TransactionDetailsResponse>

    @GET("/api/v1/transactions/{transactionId}")
    suspend fun getTransactionDetails(
        @Path("transactionId") transactionId: Long
    ): Response<TransactionDetailsResponse>
}