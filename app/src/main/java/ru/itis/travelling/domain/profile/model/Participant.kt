package ru.itis.travelling.domain.profile.model

import ru.itis.travelling.presentation.transactions.fragments.add.AddTransactionViewModel.Companion.ZERO_AMOUNT

data class Participant(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String,
    val shareAmount: String? = null,
    val isRepaid: Boolean? = null
)

fun Participant.isPaid(): Boolean {
    return this.shareAmount == ZERO_AMOUNT
}