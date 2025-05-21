package ru.itis.travelling.data.authregister.handler

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.itis.travelling.data.authregister.local.storage.TokenStorage
import ru.itis.travelling.domain.authregister.handler.LogoutHandler
import ru.itis.travelling.presentation.base.navigation.Navigator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutHandlerImpl @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val navigator: Navigator,
    @ApplicationContext private val context: Context
) : LogoutHandler {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override suspend fun logout() {
        tokenStorage.clearTokens()
//        navigator.navigateToLogin(clearStack = true)
    }

    override fun logoutAsync() {
        coroutineScope.launch {
            logout()
        }
    }
}