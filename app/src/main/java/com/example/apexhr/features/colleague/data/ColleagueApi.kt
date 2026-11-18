package com.example.apexhr.features.colleague.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.colleague.data.dto.ColleagueLeaderboardEntryDto
import com.example.apexhr.features.colleague.data.dto.ColleagueUserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Достижения коллеги берутся из ProfileApi.getUserAchievements, благодарности — из ThanksRepository. */
interface ColleagueApi {
    suspend fun getUser(userId: String): ColleagueUserDto
    suspend fun getLeaderboard(days: Int, limit: Int): List<ColleagueLeaderboardEntryDto>
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorColleagueApi(
    private val client: HttpClient,
) : ColleagueApi {
    override suspend fun getUser(userId: String): ColleagueUserDto =
        client.get(NetworkConfig.userUrl(userId)).requireSuccess().body()

    override suspend fun getLeaderboard(days: Int, limit: Int): List<ColleagueLeaderboardEntryDto> =
        client.get(NetworkConfig.LEADERBOARD_URL) {
            parameter("days", days)
            parameter("limit", limit)
        }.requireSuccess().body()
}