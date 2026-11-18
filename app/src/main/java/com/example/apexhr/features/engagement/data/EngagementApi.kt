package com.example.apexhr.features.engagement.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.engagement.data.dto.EngagementDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface EngagementApi {
    suspend fun getMyEngagement(): EngagementDto
}

/** @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT). */
class KtorEngagementApi(
    private val client: HttpClient,
) : EngagementApi {
    override suspend fun getMyEngagement(): EngagementDto =
        client.get(NetworkConfig.MY_ENGAGEMENT_URL).requireSuccess().body()
}
