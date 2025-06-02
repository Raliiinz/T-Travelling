package ru.itis.travelling.data.profile.repository

import ru.itis.travelling.data.network.ApiHelper
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.data.profile.locale.database.dao.ParticipantDao
import ru.itis.travelling.data.profile.mapper.ParticipantMapper
import ru.itis.travelling.data.profile.remote.api.ProfileApi
import ru.itis.travelling.domain.profile.model.ParticipantDto
import ru.itis.travelling.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApi: ProfileApi,
    private val participantMapper: ParticipantMapper,
    private val apiHelper: ApiHelper,
    private val participantDao: ParticipantDao
) : ProfileRepository {

    override suspend fun getProfile(): ResultWrapper<ParticipantDto> {
        return when (val result = apiHelper.safeApiCall {
            val response = profileApi.getProfile()
            val body = apiHelper.handleResponse(response)
            participantMapper.mapParticipantDto(body)
        }) {
            is ResultWrapper.Success -> {
                val participant = result.value
                participantDao.insert(participantMapper.mapToEntity(participant))
                ResultWrapper.Success(participant)
            }
            is ResultWrapper.NetworkError -> {
                val cached = participantDao.getParticipant()
                if (cached != null) {
                    ResultWrapper.Success(participantMapper.mapFromEntity(cached))
                } else {
                    ResultWrapper.NetworkError
                }
            }
            is ResultWrapper.GenericError -> result
        }
    }
}