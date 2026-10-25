package com.example.apexhr.features.shop.ui.composables

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.apexhr.features.shop.ui.models.ShopMessage
import kotlinx.coroutines.flow.Flow

/**
 * Показывает сообщения ViewModel в snackbar по одному.
 * @param actionLabel текст кнопки snackbar; null — без кнопки.
 * @param onAction нажатие на кнопку snackbar.
 */
@Composable
fun ShopMessagesEffect(
    messages: Flow<ShopMessage>,
    snackbarHostState: SnackbarHostState,
    actionLabel: (ShopMessage) -> String? = { null },
    onAction: (ShopMessage) -> Unit = {},
) {
    val context = LocalContext.current
    val currentActionLabel = rememberUpdatedState(actionLabel)
    val currentOnAction = rememberUpdatedState(onAction)
    LaunchedEffect(messages, snackbarHostState) {
        messages.collect { message ->
            val text = context.getString(message.textRes, *message.args.toTypedArray())
            val label = currentActionLabel.value(message)
            val result = snackbarHostState.showSnackbar(
                message = text,
                actionLabel = label,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                currentOnAction.value(message)
            }
        }
    }
}
