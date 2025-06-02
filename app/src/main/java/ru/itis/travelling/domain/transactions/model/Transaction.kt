package ru.itis.travelling.domain.transactions.model

import java.time.Instant

data class Transaction(
    val id: String,
    val totalCost: String,
    val description: String,
    val createdAt: Instant,
    val category: TransactionCategory
)
