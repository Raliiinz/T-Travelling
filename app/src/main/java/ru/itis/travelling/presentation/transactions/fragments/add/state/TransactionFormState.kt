package ru.itis.travelling.presentation.transactions.fragments.add.state

import ru.itis.travelling.domain.transactions.model.TransactionCategory
import ru.itis.travelling.presentation.transactions.util.SplitType

data class TransactionFormState(
    val category: TransactionCategory? = null,
    val totalAmount: String = "",
    val description: String = "",
    val splitType: SplitType = SplitType.ONE_PERSON
)