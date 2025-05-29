package ru.itis.travelling.data.profile.repository

import ru.itis.travelling.data.network.ApiHelper
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.data.profile.mapper.ParticipantMapper
import ru.itis.travelling.data.profile.remote.api.ProfileApi
import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val participantMapper: ParticipantMapper,
    private val apiHelper: ApiHelper
) : ProfileRepository {

    override suspend fun getProfile(): ResultWrapper<Participant> {
        return apiHelper.safeApiCall {
            val response = profileApi.getProfile()
            val body = apiHelper.handleResponse(response)
            participantMapper.mapParticipant(body)
        }
    }
}