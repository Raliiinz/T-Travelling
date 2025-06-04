package ru.itis.travelling.domain.transactions.model

import java.time.Instant

data class Transaction(
    val id: String,
    val totalCost: String,
    val description: String,
    val category: TransactionCategory
)
