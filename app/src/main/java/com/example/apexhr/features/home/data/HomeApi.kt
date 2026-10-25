package com.example.apexhr.features.home.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.home.data.dto.LeaderboardEntryDto
import com.example.apexhr.features.home.data.dto.QuestPageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Запросы, нужные только главной. Профиль, баланс и достижения берутся из ProfileApi. */
interface HomeApi {
    suspend fun getQuests(page: Int, pageSize: Int): QuestPageDto
    suspend fun getLeaderboard(days: Int, limit: Int): List<LeaderboardEntryDto>
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorHomeApi(
    private val client: HttpClient,
) : HomeApi {
    override suspend fun getQuests(page: Int, pageSize: Int): QuestPageDto =
        client.get(NetworkConfig.QUESTS_URL) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.requireSuccess().body()

    override suspend fun getLeaderboard(days: Int, limit: Int): List<LeaderboardEntryDto> =
        client.get(NetworkConfig.LEADERBOARD_URL) {
            parameter("days", days)
            parameter("limit", limit)
        }.requireSuccess().body()
}
