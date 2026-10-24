package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Запросы профиля. Все идут от имени пользователя, с bearer-токеном. */
interface ProfileApi {
    suspend fun getCurrentUser(): CurrentUserProfileDto
    suspend fun getBalance(): BalanceDto
    suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto>
    suspend fun getUserAchievements(userId: String): List<UserAchievementDto>
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorProfileApi(
    private val client: HttpClient,
) : ProfileApi {
    override suspend fun getCurrentUser(): CurrentUserProfileDto =
        client.get(NetworkConfig.USER_PROFILE_URL).requireSuccess().body()

    override suspend fun getBalance(): BalanceDto =
        client.get(NetworkConfig.BALANCE_URL).requireSuccess().body()

    override suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto> =
        client.get(NetworkConfig.BALANCE_TRANSACTIONS_URL) {
            parameter("skip", skip)
            parameter("take", take)
        }.requireSuccess().body()

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> =
        client.get(NetworkConfig.userAchievementsUrl(userId)).requireSuccess().body()
}
