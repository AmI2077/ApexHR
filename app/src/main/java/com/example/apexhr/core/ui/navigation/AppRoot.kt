package com.example.apexhr.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apexhr.features.auth.ui.LoginViewModel
import com.example.apexhr.features.auth.ui.composables.AuthorizationErrorScreen
import com.example.apexhr.features.auth.ui.composables.LoadingScreen
import com.example.apexhr.features.auth.ui.composables.LoginScreen
import com.example.apexhr.features.auth.ui.model.AuthState

@Composable
fun AppRoot(
    viewModel: LoginViewModel
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val loginState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = authState) {
        AuthState.Checking -> {
            LoadingScreen()
        }

        AuthState.Authorized -> {
            MainAppContent(
                onLogout = viewModel::logout
            )
        }

        AuthState.Unauthorized -> {
            LoginScreen(
                state = loginState,
                onLoginChange = viewModel::onLoginChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = viewModel::login
            )
        }

        is AuthState.Error -> {
            AuthorizationErrorScreen(
                message = state.message,
                onRetry = viewModel::retryAuthorizationCheck
            )
        }
    }
}