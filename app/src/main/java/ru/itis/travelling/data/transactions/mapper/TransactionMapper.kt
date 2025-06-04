package ru.itis.travelling.data.transactions.mapper

import com.google.gson.Gson
import ru.itis.travelling.data.transactions.remote.model.TransactionResponse
import ru.itis.travelling.domain.transactions.model.Transaction
import ru.itis.travelling.domain.transactions.model.TransactionCategory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransactionMapper @Inject constructor() {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private val gson = Gson()

    fun mapToDomain(response: TransactionResponse): Transaction {
        val jsonResponse = gson.toJson(response)
        println("Incoming TransactionResponse JSON: $jsonResponse")

        return Transaction(
            id = response.id.toString(),
            totalCost = response.totalCost.toString(),
            description = response.description,
//            createdAt = LocalDateTime.parse(response.createdAt, dateFormatter)
//                .atOffset(ZoneOffset.UTC)
//                .toInstant(),
            category = TransactionCategory.valueOf(response.category)
        )
    }
}