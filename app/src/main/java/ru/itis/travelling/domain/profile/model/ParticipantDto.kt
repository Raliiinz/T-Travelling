package ru.itis.travelling.domain.profile.model

data class ParticipantDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String
)

fun ParticipantDto.toParticipant() = Participant(
    firstName = firstName,
    lastName = lastName,
    phone = phone,
    shareAmount = "0.0",
    isRepaid = false
)
