package com.example.apexhr.features.notifications.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.notifications.data.dto.NotificationPageDto
import com.example.apexhr.features.notifications.data.dto.UnreadCountDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post

interface NotificationsApi {
    suspend fun getNotifications(page: Int, pageSize: Int): NotificationPageDto
    suspend fun getUnreadCount(): UnreadCountDto
    suspend fun markRead(id: Long)
    suspend fun markAllRead()
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorNotificationsApi(
    private val client: HttpClient,
) : NotificationsApi {
    override suspend fun getNotifications(page: Int, pageSize: Int): NotificationPageDto =
        client.get(NetworkConfig.NOTIFICATIONS_URL) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.requireSuccess().body()

    override suspend fun getUnreadCount(): UnreadCountDto =
        client.get(NetworkConfig.NOTIFICATIONS_UNREAD_COUNT_URL).requireSuccess().body()

    override suspend fun markRead(id: Long) {
        client.patch(NetworkConfig.notificationReadUrl(id)).requireSuccess()
    }

    override suspend fun markAllRead() {
        client.post(NetworkConfig.NOTIFICATIONS_READ_ALL_URL).requireSuccess()
    }
}
