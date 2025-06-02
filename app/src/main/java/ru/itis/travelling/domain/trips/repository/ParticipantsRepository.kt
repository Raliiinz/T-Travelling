package ru.itis.travelling.domain.trips.repository

import ru.itis.travelling.domain.profile.model.ParticipantDto

interface ParticipantsRepository {
    suspend fun getParticipants(): List<ParticipantDto>
    fun hasContactsPermission(): Boolean
}