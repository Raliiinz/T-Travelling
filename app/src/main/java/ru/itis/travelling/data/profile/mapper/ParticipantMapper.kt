package ru.itis.travelling.data.profile.mapper

import ru.itis.travelling.data.profile.locale.database.entities.ParticipantEntity
import ru.itis.travelling.data.profile.remote.model.ParticipantResponse
import ru.itis.travelling.domain.profile.model.Participant
import javax.inject.Inject

class ParticipantMapper @Inject constructor() {
    fun mapParticipant(userDto: ParticipantResponse): Participant {
        return Participant(
            phone = userDto.phoneNumber,
            firstName = userDto.firstName,
            lastName = userDto.lastName
        )
    }

    fun mapToEntity(participant: Participant): ParticipantEntity {
        return ParticipantEntity(
            phone = participant.phone,
            firstName = participant.firstName!!,
            lastName = participant.lastName!!
        )
    }

    fun mapFromEntity(entity: ParticipantEntity): Participant {
        return Participant(
            phone = entity.phone,
            firstName = entity.firstName,
            lastName = entity.lastName
        )
    }
}
