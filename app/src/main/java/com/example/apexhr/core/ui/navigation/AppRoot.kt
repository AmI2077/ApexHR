package com.example.apexhr.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apexhr.features.auth.ui.LoginViewModel
import com.example.apexhr.features.auth.ui.SessionViewModel
import com.example.apexhr.features.auth.ui.composables.AuthorizationErrorScreen
import com.example.apexhr.features.auth.ui.composables.LoadingScreen
import com.example.apexhr.features.auth.ui.composables.LoginScreen
import com.example.apexhr.features.auth.ui.model.AuthState
import com.example.apexhr.features.auth.ui.model.LoginUiActions
import com.example.apexhr.features.auth.ui.model.messageRes
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppRoot(
    sessionViewModel: SessionViewModel = koinViewModel(),
    loginViewModel: LoginViewModel = koinViewModel(),
) {
    val authState by sessionViewModel.authState.collectAsStateWithLifecycle()
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(loginViewModel) {
        loginViewModel.actions.collect { action ->
            when (action) {
                LoginUiActions.Success -> sessionViewModel.onLoginSuccess()
            }
        }
    }

    when (val state = authState) {
        AuthState.Checking -> {
            LoadingScreen()
        }

        AuthState.Authorized -> {
            MainAppContent(
                onLogout = sessionViewModel::logout
            )
        }

        is AuthState.Unauthorized -> {
            LoginScreen(
                state = loginState,
                isSessionExpired = state.isSessionExpired,
                onLoginChange = loginViewModel::onLoginChange,
                onPasswordChange = loginViewModel::onPasswordChange,
                onLoginClick = loginViewModel::login
            )
        }

        is AuthState.Error -> {
            AuthorizationErrorScreen(
                message = stringResource(state.error.messageRes()),
                onRetry = sessionViewModel::checkSession
            )
        }
    }
}
