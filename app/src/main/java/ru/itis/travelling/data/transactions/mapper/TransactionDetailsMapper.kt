package ru.itis.travelling.data.transactions.mapper

import ru.itis.travelling.data.profile.mapper.ParticipantMapper
import ru.itis.travelling.data.transactions.remote.model.ParticipantRequest
import ru.itis.travelling.data.transactions.remote.model.TransactionDetailsRequest
import ru.itis.travelling.data.transactions.remote.model.TransactionDetailsResponse
import ru.itis.travelling.domain.transactions.model.TransactionCategory
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import java.time.Instant
import javax.inject.Inject

class TransactionDetailsMapper @Inject constructor(
    private val participantMapper: ParticipantMapper
) {
    fun mapToResponse(response: TransactionDetailsResponse): TransactionDetails {
        return TransactionDetails(
            id = response.id.toString(),
            totalCost = response.totalCost.toString(),
            description = response.description,
            createdAt = Instant.parse(response.createdAt),
            category = TransactionCategory.valueOf(response.category),
            participants = response.participants.map { participantMapper.mapParticipant(it) }
                .toMutableList(),
            creator = participantMapper.mapParticipantDto(response.creator),
        )
    }
    fun mapToRequest(transactionDetails: TransactionDetails): TransactionDetailsRequest {
        return TransactionDetailsRequest(
            category = transactionDetails.category.name,
            totalCost = transactionDetails.totalCost.toDoubleOrNull() ?: 0.0,
            description = transactionDetails.description,
            createdAt = transactionDetails.createdAt.toString(),
            participant = transactionDetails.participants.map { participant ->
                ParticipantRequest(
                    phoneNumber = participant.phone,
                    shareAmount = participant.shareAmount!!
                )
            }

        )
    }
}