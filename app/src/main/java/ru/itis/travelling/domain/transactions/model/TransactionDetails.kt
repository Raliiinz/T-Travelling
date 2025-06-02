package ru.itis.travelling.domain.transactions.model

import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.domain.profile.model.ParticipantDto
import java.time.Instant

data class TransactionDetails (
    val id: String,
    val totalCost: String,
    val description: String,
    val createdAt: Instant,
    val category: TransactionCategory,
    val creator: ParticipantDto,
    val participants: MutableList<Participant>
)
