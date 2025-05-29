package ru.itis.travelling.presentation.profile.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.domain.base.usecase.GetCurrentLanguageUseCase
import ru.itis.travelling.domain.base.usecase.SetLanguageUseCase
import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.domain.profile.usecase.GetProfileUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.utils.PhoneNumberUtils.formatPhoneNumber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getProfileUseCase: GetProfileUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    private val navigator: Navigator,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val getCurrentLanguageUseCase: GetCurrentLanguageUseCase
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent


    fun getCurrentLanguage(): String {
        return getCurrentLanguageUseCase()
    }

    fun changeLanguage(language: String) {
        viewModelScope.launch {
            setLanguageUseCase(language)
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.update { ProfileState.Loading }
            delay(2000)

            when (val result = getProfileUseCase()) {
                is ResultWrapper.Success -> {
                    val formattedProfile = formatProfile(result.value)
                    _profileState.update { ProfileState.Success(formattedProfile) }
                }

                is ResultWrapper.GenericError -> {
                    handleError(result.code)
                }

                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
        }
    }

    private fun formatProfile(participant: Participant): Participant {
        return participant.copy(
            phone = formatPhoneNumber(participant.phone)
        )
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.clearAuthData()
                navigator.navigateToAuthorizationFragment()
            } catch (e: Exception) {
                _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_logout))
            }
        }
    }

    private suspend fun handleError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)
        val messageRes = when (reason) {
            ErrorEvent.FailureReason.Unauthorized -> R.string.error_unauthorized_trip
            ErrorEvent.FailureReason.BadRequest -> R.string.error_bad_request_profile
            ErrorEvent.FailureReason.Server -> R.string.error_server
            ErrorEvent.FailureReason.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
    }

    sealed class ProfileState {
        object Loading : ProfileState()
        data class Success(val participant: Participant) : ProfileState()
    }
}

