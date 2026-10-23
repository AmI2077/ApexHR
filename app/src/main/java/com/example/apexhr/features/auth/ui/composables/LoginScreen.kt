package com.example.apexhr.features.auth.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.example.apexhr.features.auth.ui.model.LoginUiState
import com.example.apexhr.features.auth.ui.model.messageRes

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreenContent(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(ApexDimens.spacing20.dp),
        state = LoginUiState(
            login = "prooo.01@mail.ru",
            password = "1234566"
        )
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LoginScreenContent(
        modifier = modifier,
        state = state,
        onLoginChange = onLoginChange,
        onPasswordChange = onPasswordChange,
        onLoginClick = onLoginClick
    )
}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    state: LoginUiState,
    onLoginChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row() {
            Icon(
                modifier = Modifier
                    .size(48.dp),
                tint = primaryBlueColor,
                painter = painterResource(R.drawable.app_logo),
                contentDescription = null,
            )
            Text(
                modifier = Modifier.align(Alignment.Bottom),
                text = "Апекс",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryTextColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(15.dp))
        Text(
            text = "Добро пожаловать!",
            style = MaterialTheme.typography.headlineMedium,
            color = primaryTextColor,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Войдите в свой аккаунт",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.login,
            onValueChange = onLoginChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            singleLine = true,
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null
                )
            },
            isError = state.error != null,
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
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            singleLine = true,
            label = { Text("Пароль") },
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
                        contentDescription = if (isPasswordVisible) {
                            "Скрыть пароль"
                        } else {
                            "Показать пароль"
                        }
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            isError = state.error != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onLoginClick() }
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
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !state.isLoading &&
                    state.login.isNotBlank() &&
                    state.password.isNotBlank(),
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
                    text = "Войти",
                    style = MaterialTheme.typography.titleMedium
                )
            }
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
