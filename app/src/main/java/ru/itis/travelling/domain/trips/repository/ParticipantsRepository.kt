package ru.itis.travelling.domain.trips.repository

import ru.itis.travelling.domain.profile.model.Participant

interface ParticipantsRepository {
    suspend fun getParticipants(): List<Participant>
    fun hasContactsPermission(): Boolean
}