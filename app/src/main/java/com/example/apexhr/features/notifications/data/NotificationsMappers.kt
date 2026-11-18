package com.example.apexhr.features.notifications.data

import com.example.apexhr.features.notifications.data.dto.NotificationDto
import com.example.apexhr.features.notifications.data.dto.NotificationPageDto
import com.example.apexhr.features.notifications.domain.AppNotification
import com.example.apexhr.features.notifications.domain.NotificationsPage
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/** Уведомление без id отметить прочитанным нельзя — такие пропускаем. */
fun NotificationDto.toDomain(): AppNotification? {
    val id = id ?: return null
    return AppNotification(
        id = id,
        title = firstText(title, subject),
        body = firstText(body, message, text),
        createdAt = createdAt.toNotificationInstantOrNull(),
        isRead = isRead ?: read ?: false,
    )
}

fun NotificationPageDto.toDomain(): NotificationsPage = NotificationsPage(
    items = items.orEmpty().mapNotNull { it.toDomain() },
    page = page,
    hasNextPage = hasNextPage ?: (page < totalPages),
)

private fun firstText(vararg values: String?): String? =
    values.firstOrNull { !it.isNullOrBlank() }?.trim()

/** Дата с зоной (…Z) или без неё; без зоны считаем, что это UTC, как и остальные даты сервера. */
fun String?.toNotificationInstantOrNull(): Instant? {
    val value = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value).toInstant(ZoneOffset.UTC) }.getOrNull()
}
