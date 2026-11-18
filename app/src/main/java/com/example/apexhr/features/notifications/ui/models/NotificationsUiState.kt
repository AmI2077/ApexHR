package com.example.apexhr.features.notifications.ui.models

import com.example.apexhr.core.domain.DataError

/**
 * @param createdAt дата и время в часовом поясе устройства; null, если сервер не прислал.
 */
data class NotificationUiModel(
    val id: Long,
    val title: String?,
    val body: String?,
    val createdAt: String?,
    val isRead: Boolean,
)

/**
 * @param hasNextPage на сервере есть ещё страницы: показываем «Показать ещё».
 * @param isLoadingMore догружается следующая страница.
 * @param isMarkingAll выполняется «Прочитать все».
 */
data class NotificationsUiState(
    val items: List<NotificationUiModel>,
    val hasNextPage: Boolean,
    val isLoadingMore: Boolean = false,
    val isMarkingAll: Boolean = false,
) {
    val hasUnread: Boolean
        get() = items.any { !it.isRead }

    companion object {
        val Preview = NotificationsUiState(
            items = listOf(
                NotificationUiModel(
                    id = 2,
                    title = "Новая благодарность",
                    body = "Анна поблагодарила вас: «Спасибо за помощь с релизом!»",
                    createdAt = "18.11.2026 14:05",
                    isRead = false,
                ),
                NotificationUiModel(
                    id = 1,
                    title = "Заказ оформлен",
                    body = "Заказ №6 оплачен апексами",
                    createdAt = "17.11.2026 09:30",
                    isRead = true,
                ),
            ),
            hasNextPage = true,
        )
    }
}

sealed interface NotificationsScreenState {
    data object Loading : NotificationsScreenState
    data class Error(val error: DataError) : NotificationsScreenState
    data class Content(val uiState: NotificationsUiState) : NotificationsScreenState
}
