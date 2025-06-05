package ru.itis.travelling.data.transactions.mapper

import ru.itis.travelling.data.transactions.remote.model.TransactionResponse
import ru.itis.travelling.domain.transactions.model.Transaction
import ru.itis.travelling.domain.transactions.model.TransactionCategory
import javax.inject.Inject

class TransactionMapper @Inject constructor() {
    fun mapToDomain(response: TransactionResponse): Transaction {

        return Transaction(
            id = response.id.toString(),
            totalCost = response.totalCost.toString(),
            description = response.description,
            category = TransactionCategory.valueOf(response.category)
        )
    }
}