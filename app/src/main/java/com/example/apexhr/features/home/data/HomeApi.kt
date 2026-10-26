package com.example.apexhr.features.home.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.home.data.dto.HomeAchievementDto
import com.example.apexhr.features.home.data.dto.HomeUserDto
import com.example.apexhr.features.home.data.dto.LeaderboardEntryDto
import com.example.apexhr.features.home.data.dto.QuestPageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Запросы, нужные только главной. Профиль, баланс и полученные пользователем
 * достижения берутся из ProfileApi.
 */
interface HomeApi {
    suspend fun getQuests(page: Int, pageSize: Int): QuestPageDto
    suspend fun getLeaderboard(days: Int, limit: Int): List<LeaderboardEntryDto>

    /** Все сотрудники: нужны, чтобы показать в лидерборде и тех, у кого 0 благодарностей. */
    suspend fun getUsers(): List<HomeUserDto>

    /** Каталог всех достижений — чтобы показать и ещё не полученные. */
    suspend fun getAchievements(): List<HomeAchievementDto>
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

    override suspend fun getUsers(): List<HomeUserDto> =
        client.get(NetworkConfig.USERS_URL).requireSuccess().body()

    override suspend fun getAchievements(): List<HomeAchievementDto> =
        client.get(ACHIEVEMENTS_URL).requireSuccess().body()

    private companion object {
        /** В NetworkConfig адреса каталога нет, а core эта задача не трогает. */
        const val ACHIEVEMENTS_URL = "${NetworkConfig.BASE_URL}/api/achievements"
    }
}
