package com.example.apexhr.features.notifications.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.notifications.domain.NotificationsPage
import com.example.apexhr.features.notifications.domain.NotificationsRepository

class NotificationsRepositoryImpl(
    private val api: NotificationsApi,
) : NotificationsRepository {

    override suspend fun getNotifications(page: Int, pageSize: Int): DataResult<NotificationsPage> =
        safeApiCall { api.getNotifications(page, pageSize).toDomain() }

    override suspend fun getUnreadCount(): DataResult<Int> =
        safeApiCall { api.getUnreadCount().count.coerceAtLeast(0) }

    override suspend fun markRead(id: Long): DataResult<Unit> =
        safeApiCall { api.markRead(id) }

    override suspend fun markAllRead(): DataResult<Unit> =
        safeApiCall { api.markAllRead() }
}
