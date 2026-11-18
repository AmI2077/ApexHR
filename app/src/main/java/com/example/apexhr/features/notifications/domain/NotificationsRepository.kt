package com.example.apexhr.features.notifications.domain

import com.example.apexhr.core.domain.DataResult

interface NotificationsRepository {
    /** GET /api/notifications?page&pageSize. Страницы считаются с 1. */
    suspend fun getNotifications(page: Int, pageSize: Int = PAGE_SIZE): DataResult<NotificationsPage>

    /** GET /api/notifications/unread-count: число непрочитанных для бейджа. */
    suspend fun getUnreadCount(): DataResult<Int>

    /** PATCH /api/notifications/{id}/read. */
    suspend fun markRead(id: Long): DataResult<Unit>

    /** POST /api/notifications/read-all. */
    suspend fun markAllRead(): DataResult<Unit>

    companion object {
        const val PAGE_SIZE = 20
    }
}
