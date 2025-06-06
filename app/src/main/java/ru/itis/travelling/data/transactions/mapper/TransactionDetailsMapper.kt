package ru.itis.travelling.data.transactions.mapper

import ru.itis.travelling.data.profile.mapper.ParticipantMapper
import ru.itis.travelling.data.transactions.remote.model.ParticipantRequest
import ru.itis.travelling.data.transactions.remote.model.TransactionDetailsRequest
import ru.itis.travelling.data.transactions.remote.model.TransactionDetailsResponse
import ru.itis.travelling.data.util.DateUtils
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import javax.inject.Inject

class TransactionDetailsMapper @Inject constructor(
    private val participantMapper: ParticipantMapper
) {
    fun mapToResponse(response: TransactionDetailsResponse): TransactionDetails {
        return TransactionDetails(
            id = response.id.toString(),
            totalCost = response.totalCost.toString(),
            description = response.description,
            category = response.category,
            participants = response.participants.map { participantMapper.mapParticipant(it) }
                .toList(),
            creator = participantMapper.mapParticipantDto(response.creator),
        )
    }
    fun mapToRequest(transactionDetails: TransactionDetails): TransactionDetailsRequest {
        val request =  TransactionDetailsRequest(
            category = transactionDetails.category.uppercase(),
            totalCost = transactionDetails.totalCost.toDoubleOrNull() ?: 0.0,
            description = transactionDetails.description,
            createdAt = DateUtils.formatCurrentTimeForApi(),
            participant = transactionDetails.participants.map { participant ->
                ParticipantRequest(
                    phoneNumber = participant.phone,
                    shareAmount = participant.shareAmount?.toDoubleOrNull() ?: 0.0
                )
            }

        )
        return request
    }
}
