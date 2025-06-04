package ru.itis.travelling.data.transactions.mapper

import android.util.Log
import com.google.gson.Gson
import ru.itis.travelling.data.profile.mapper.ParticipantMapper
import ru.itis.travelling.data.transactions.remote.model.ParticipantRequest
import ru.itis.travelling.data.transactions.remote.model.TransactionDetailsRequest
import ru.itis.travelling.data.transactions.remote.model.TransactionDetailsResponse
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransactionDetailsMapper @Inject constructor(
    private val participantMapper: ParticipantMapper
) {
    private val apiDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")


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
            createdAt = formatCurrentTime(),
            participant = transactionDetails.participants.map { participant ->
                ParticipantRequest(
                    phoneNumber = participant.phone,
                    shareAmount = participant.shareAmount?.toDoubleOrNull() ?: 0.0
                )
            }

        )
        Log.d("TransactionDetailsMapper", "Sending request: ${Gson().toJson(request)}")
        return request
    }

    private fun formatCurrentTime(): String {
        val currentDateTime = LocalDateTime.now()

        val formattedDateTime = currentDateTime.format(apiDateTimeFormatter)

        return if (formattedDateTime.length == 19) { // Если нет миллисекунд
            "$formattedDateTime.${currentDateTime.nano / 1_000_000}" // Добавляем миллисекунды
        } else {
            formattedDateTime
        }.also {
            Log.d("DateTimeFormat", "Formatted current time: $it")
        }
    }
}
