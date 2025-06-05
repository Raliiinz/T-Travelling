package ru.itis.travelling.domain.transactions.model


data class Transaction(
    val id: String,
    val totalCost: String,
    val description: String,
    val category: TransactionCategory
)
