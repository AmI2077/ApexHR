package com.example.apexhr.features.notifications.ui.models

import com.example.apexhr.features.notifications.domain.AppNotification
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val NOTIFICATION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

/** @param zone часовой пояс, в котором показываем дату (по умолчанию — устройства). */
fun AppNotification.toUiModel(zone: ZoneId = ZoneId.systemDefault()): NotificationUiModel =
    NotificationUiModel(
        id = id,
        title = title,
        body = body,
        createdAt = createdAt?.atZone(zone)?.format(NOTIFICATION_DATE_FORMAT),
        isRead = isRead,
    )

/** Добавляет следующую страницу, не дублируя уведомления, сдвинувшиеся между страницами. */
fun List<NotificationUiModel>.appendPage(next: List<NotificationUiModel>): List<NotificationUiModel> {
    val known = mapTo(HashSet()) { it.id }
    return this + next.filter { it.id !in known }
}

fun List<NotificationUiModel>.markRead(id: Long): List<NotificationUiModel> =
    map { if (it.id == id) it.copy(isRead = true) else it }

fun List<NotificationUiModel>.markUnread(id: Long): List<NotificationUiModel> =
    map { if (it.id == id) it.copy(isRead = false) else it }

fun List<NotificationUiModel>.markAllRead(): List<NotificationUiModel> =
    map { if (it.isRead) it else it.copy(isRead = true) }
