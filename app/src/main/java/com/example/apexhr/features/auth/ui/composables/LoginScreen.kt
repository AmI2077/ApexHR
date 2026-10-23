package com.example.apexhr.features.auth.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.auth.domain.AuthError
import com.example.apexhr.features.auth.domain.EmailError
import com.example.apexhr.features.auth.ui.model.LoginScreenCallbacks
import com.example.apexhr.features.auth.ui.model.LoginUiState
import com.example.apexhr.features.auth.ui.model.messageRes

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        state = LoginUiState(),
        isSessionExpired = false,
        callbacks = LoginScreenCallbacks.Empty
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    LoginScreenContent(
        modifier = Modifier
            .background(backgroundColor)
            .padding(ApexDimens.spacing20.dp),
        state = LoginUiState(email = "user@example.com", password = "password", isLoading = true),
        showWakeUpHintInitially = true
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenErrorPreview() {
    LoginScreen(
        state = LoginUiState(
            email = "user@example",
            emailError = EmailError.InvalidFormat,
            error = AuthError.Timeout
        ),
        isSessionExpired = true,
        callbacks = LoginScreenCallbacks.Empty
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    isSessionExpired: Boolean,
    callbacks: LoginScreenCallbacks,
    modifier: Modifier = Modifier
) {
    LoginScreenContent(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(ApexDimens.spacing20.dp),
        state = state,
        isSessionExpired = isSessionExpired,
        callbacks = callbacks
    )
}

@Composable
fun LoginScreenContent(
    state: LoginUiState,
    modifier: Modifier = Modifier,
    isSessionExpired: Boolean = false,
    callbacks: LoginScreenCallbacks = LoginScreenCallbacks.Empty,
    showWakeUpHintInitially: Boolean = false,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // Если вход идёт дольше WAKE_UP_HINT_DELAY_MS, скорее всего сервер просыпается:
    // показываем подсказку под кнопкой, пока запрос не завершится.
    var isWakeUpHintVisible by remember { mutableStateOf(showWakeUpHintInitially) }
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            delay(WAKE_UP_HINT_DELAY_MS)
            isWakeUpHintVisible = true
        } else {
            isWakeUpHintVisible = false
        }
    }
    val focusManager = LocalFocusManager.current

    val submit = {
        focusManager.clearFocus()
        callbacks.onLoginClick()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Icon(
                modifier = Modifier
                    .size(48.dp),
                tint = primaryBlueColor,
                painter = painterResource(R.drawable.app_logo),
                contentDescription = null,
            )
            Text(
                modifier = Modifier.align(Alignment.Bottom),
                text = stringResource(R.string.auth_brand),
                style = MaterialTheme.typography.headlineMedium,
                color = primaryTextColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(15.dp))
        Text(
            text = stringResource(R.string.auth_welcome),
            style = MaterialTheme.typography.headlineMedium,
            color = primaryTextColor,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.auth_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor
        )

        if (isSessionExpired) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.auth_session_expired),
                style = MaterialTheme.typography.bodyMedium,
                color = negativeRedColor,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = callbacks::onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.EmailAddress },
            enabled = !state.isLoading,
            singleLine = true,
            label = { Text(stringResource(R.string.auth_email_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null
                )
            },
            isError = state.emailError != null || state.error == AuthError.InvalidCredentials,
            supportingText = state.emailError?.let { error ->
                { Text(stringResource(error.messageRes())) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp),
            colors = loginTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = callbacks::onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.Password },
            enabled = !state.isLoading,
            singleLine = true,
            label = { Text(stringResource(R.string.auth_password_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = stringResource(
                            if (isPasswordVisible) {
                                R.string.auth_hide_password
                            } else {
                                R.string.auth_show_password
                            }
                        )
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = state.passwordError != null || state.error == AuthError.InvalidCredentials,
            supportingText = state.passwordError?.let { error ->
                { Text(stringResource(error.messageRes())) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { submit() }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = loginTextFieldColors()
        )

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(state.error.messageRes()),
                modifier = Modifier.fillMaxWidth(),
                color = negativeRedColor,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = submit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            // Кнопка активна всегда, кроме загрузки: при пустых полях нажатие
            // покажет подсказки валидации.
            enabled = !state.isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBlueColor,
                contentColor = Color.White
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.auth_login_button),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (state.isLoading && isWakeUpHintVisible) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.auth_server_waking_up),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor
            )
        }
    }
}

@Composable
private fun loginTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryBlueColor,
        unfocusedBorderColor = secondaryBlueColor,
        focusedLabelColor = primaryBlueColor,
        cursorColor = primaryBlueColor,
        focusedLeadingIconColor = primaryBlueColor,
        unfocusedLeadingIconColor = secondaryTextColor,
        focusedTrailingIconColor = primaryBlueColor,
        unfocusedTrailingIconColor = secondaryTextColor,
        focusedTextColor = primaryTextColor,
        unfocusedTextColor = primaryTextColor
    )

private const val WAKE_UP_HINT_DELAY_MS = 3_000L
