package ru.itis.travelling.data.transactions.mapper

import ru.itis.travelling.data.transactions.remote.model.ParticipantRequest
import ru.itis.travelling.data.transactions.remote.model.UpdateTransactionRequest
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import javax.inject.Inject
import kotlin.text.uppercase

class UpdateTransactionMapper @Inject constructor() {
    fun mapToUpdateRequest(transactionDetails: TransactionDetails): UpdateTransactionRequest {
        return UpdateTransactionRequest(
            category = transactionDetails.category.uppercase(),
            totalCost = transactionDetails.totalCost.toDoubleOrNull() ?: 0.0,
            description = transactionDetails.description,
            participant = transactionDetails.participants.map { participant ->
                ParticipantRequest(
                    phoneNumber = participant.phone,
                    shareAmount = participant.shareAmount?.toDoubleOrNull() ?: 0.0
                )
            }
        )
    }
}