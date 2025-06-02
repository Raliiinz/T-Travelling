package ru.itis.travelling.domain.profile.repository

import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.profile.model.ParticipantDto

interface ProfileRepository {
    suspend fun getProfile(): ResultWrapper<ParticipantDto>
}