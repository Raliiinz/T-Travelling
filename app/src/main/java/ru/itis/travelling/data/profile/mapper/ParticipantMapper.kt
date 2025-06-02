package ru.itis.travelling.data.profile.mapper

import ru.itis.travelling.data.profile.locale.database.entities.ParticipantEntity
import ru.itis.travelling.data.profile.remote.model.ParticipantDtoResponse
import ru.itis.travelling.data.transactions.remote.model.ParticipantResponse
import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.domain.profile.model.ParticipantDto
import javax.inject.Inject

class ParticipantMapper @Inject constructor() {
    fun mapParticipantDto(userDto: ParticipantDtoResponse): ParticipantDto {
        return ParticipantDto(
            phone = userDto.phoneNumber,
            firstName = userDto.firstName,
            lastName = userDto.lastName
        )
    }

    fun mapParticipant(user: ParticipantResponse): Participant {
        return Participant(
            phone = user.phoneNumber,
            firstName = user.firstName,
            lastName = user.lastName,
            shareAmount = user.shareAmount.toString(),
            isRepaid = user.isRepaid
        )
    }

    fun mapToEntity(participant: ParticipantDto): ParticipantEntity {
        return ParticipantEntity(
            phone = participant.phone,
            firstName = participant.firstName!!,
            lastName = participant.lastName!!
        )
    }

    fun mapFromEntity(entity: ParticipantEntity): ParticipantDto {
        return ParticipantDto(
            phone = entity.phone,
            firstName = entity.firstName,
            lastName = entity.lastName
        )
    }
}
