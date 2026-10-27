package com.example.apexhr.features.auth.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.auth.domain.ChangePasswordError
import com.example.apexhr.features.auth.domain.ConfirmPasswordError
import com.example.apexhr.features.auth.ui.model.ChangePasswordScreenCallbacks
import com.example.apexhr.features.auth.ui.model.ChangePasswordUiState
import com.example.apexhr.features.auth.ui.model.message
import com.example.apexhr.features.auth.ui.model.messageRes
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ChangePasswordScreen(
            state = ChangePasswordUiState(),
            callbacks = ChangePasswordScreenCallbacks.Empty
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ChangePasswordScreen(
            state = ChangePasswordUiState(
                newPassword = "qwerty",
                confirmPassword = "qwert",
                confirmPasswordError = ConfirmPasswordError.Mismatch,
                error = ChangePasswordError.Rejected("Пароль слишком простой")
            ),
            callbacks = ChangePasswordScreenCallbacks.Empty
        )
    }
}

/** Экран обязательной смены временного пароля при первом входе. */
@Composable
fun ChangePasswordScreen(
    state: ChangePasswordUiState,
    callbacks: ChangePasswordScreenCallbacks,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val submit = {
        focusManager.clearFocus()
        callbacks.onSubmitClick()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(ApexDimens.spacing20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.change_password_title),
            style = MaterialTheme.typography.headlineMedium,
            color = primaryTextColor,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.change_password_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.newPassword,
            onValueChange = callbacks::onNewPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            singleLine = true,
            label = { Text(stringResource(R.string.change_password_new_label)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.newPasswordError != null,
            supportingText = state.newPasswordError?.let { error ->
                { Text(error.message()) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp),
            colors = changePasswordTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = callbacks::onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            singleLine = true,
            label = { Text(stringResource(R.string.change_password_confirm_label)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.confirmPasswordError != null,
            supportingText = state.confirmPasswordError?.let { error ->
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
            colors = changePasswordTextFieldColors()
        )

        if (state.error != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.error.message(),
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
            enabled = !state.isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBlueColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.change_password_save),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = callbacks::onLogoutClick,
            enabled = !state.isLoading
        ) {
            Text(
                text = stringResource(R.string.change_password_logout),
                color = secondaryTextColor
            )
        }
    }
}

@Composable
private fun changePasswordTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryBlueColor,
        unfocusedBorderColor = secondaryBlueColor,
        focusedLabelColor = primaryBlueColor,
        cursorColor = primaryBlueColor,
        focusedTextColor = primaryTextColor,
        unfocusedTextColor = primaryTextColor
    )