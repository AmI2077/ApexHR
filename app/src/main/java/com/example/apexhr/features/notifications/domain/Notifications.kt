package com.example.apexhr.features.notifications.domain

import java.time.Instant

/**
 * Уведомление пользователя.
 * @param title заголовок; null, если сервер его не прислал.
 * @param body текст уведомления.
 */
data class AppNotification(
    val id: Long,
    val title: String?,
    val body: String?,
    val createdAt: Instant?,
    val isRead: Boolean,
)

/** Страница уведомлений: новые сверху, как отдаёт сервер. */
data class NotificationsPage(
    val items: List<AppNotification>,
    val page: Int,
    val hasNextPage: Boolean,
)
