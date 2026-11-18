package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.activities.data.dto.AchievementCatalogDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface AchievementsApi {
    suspend fun getAchievements(): List<AchievementCatalogDto>
}

/** @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT). */
class KtorAchievementsApi(
    private val client: HttpClient,
) : AchievementsApi {
    override suspend fun getAchievements(): List<AchievementCatalogDto> =
        client.get(NetworkConfig.ACHIEVEMENTS_URL).requireSuccess().body()
}