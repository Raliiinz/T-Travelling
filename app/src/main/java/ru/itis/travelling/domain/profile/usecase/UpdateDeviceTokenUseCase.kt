package ru.itis.travelling.domain.profile.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class UpdateDeviceTokenUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(token: String): ResultWrapper<Unit> {
        return withContext(dispatcher) {
            profileRepository.updateDeviceToken(token)
        }
    }
}