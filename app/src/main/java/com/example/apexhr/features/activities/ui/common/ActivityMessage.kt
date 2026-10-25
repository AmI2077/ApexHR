package com.example.apexhr.features.activities.ui.common

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.activities.domain.ActivityActionError

/**
 * Разовое сообщение экрана деталей активности (показывается в snackbar).
 * @param args аргументы строки [textRes].
 */
data class ActivityMessage(
    @StringRes val textRes: Int,
    val args: List<Any> = emptyList(),
)

/** Текст ошибки действия (запись, вступление, ответ) для пользователя. */
fun ActivityActionError.toActivityMessage(): ActivityMessage = when (this) {
    ActivityActionError.NotFound -> ActivityMessage(R.string.activities_error_not_found)
    ActivityActionError.Forbidden -> ActivityMessage(R.string.activities_error_forbidden)
    is ActivityActionError.Rejected -> if (serverMessage.isNullOrBlank()) {
        ActivityMessage(R.string.activities_error_action)
    } else {
        // Текст сервера (по-английски) показываем как есть: другого объяснения нет.
        ActivityMessage(R.string.activities_error_server_message, listOf(serverMessage))
    }
    // «Не удалось загрузить данные» не подходит к действию — свой общий текст.
    is ActivityActionError.Data -> if (error == DataError.Unknown) {
        ActivityMessage(R.string.activities_error_action)
    } else {
        ActivityMessage(error.messageRes())
    }
}
