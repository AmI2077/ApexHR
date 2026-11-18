package com.example.apexhr.features.notifications.data.dto

import kotlinx.serialization.Serializable

/**
 * Элемент GET /api/notifications. В Swagger схемы элемента нет, поэтому
 * принимаем те же варианты полей, что и веб: title/subject,
 * body/message/text, isRead/read.
 */
@Serializable
data class NotificationDto(
    val id: Long? = null,
    val title: String? = null,
    val subject: String? = null,
    val body: String? = null,
    val message: String? = null,
    val text: String? = null,
    val createdAt: String? = null,
    val isRead: Boolean? = null,
    val read: Boolean? = null,
)

/** Ответ GET /api/notifications: {items, page, totalPages, hasNextPage, …}. */
@Serializable
data class NotificationPageDto(
    val items: List<NotificationDto>? = null,
    val page: Int = 1,
    val totalPages: Int = 0,
    val hasNextPage: Boolean? = null,
)

/** Ответ GET /api/notifications/unread-count: {"count": N}. */
@Serializable
data class UnreadCountDto(
    val count: Int = 0,
)
