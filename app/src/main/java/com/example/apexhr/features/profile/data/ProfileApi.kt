package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

/** Запросы профиля. Все идут от имени пользователя, с bearer-токеном. */
interface ProfileApi {
    suspend fun getCurrentUser(): CurrentUserProfileDto
    suspend fun getBalance(): BalanceDto
    suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto>
    suspend fun getUserAchievements(userId: String): List<UserAchievementDto>

    /** PATCH /api/users/me. Ответ не разбираем: после сохранения профиль перечитывается. */
    suspend fun updateOwnProfile(request: UpdateOwnProfileRequest)
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

    /**
     * У нового сотрудника записи баланса ещё нет, и сервер отвечает
     * 404 «Balance was not found.» — это нулевой баланс, а не ошибка.
     * Остальные ошибки (в том числе 401) пробрасываются как раньше.
     */
    override suspend fun getBalance(): BalanceDto {
        val response = client.get(NetworkConfig.BALANCE_URL)
        if (response.status == HttpStatusCode.NotFound) return BalanceDto(amount = 0)
        return response.requireSuccess().body()
    }

    override suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto> =
        client.get(NetworkConfig.BALANCE_TRANSACTIONS_URL) {
            parameter("skip", skip)
            parameter("take", take)
        }.requireSuccess().body()

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> =
        client.get(NetworkConfig.userAchievementsUrl(userId)).requireSuccess().body()

    override suspend fun updateOwnProfile(request: UpdateOwnProfileRequest) {
        client.patch(NetworkConfig.USER_PROFILE_URL) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireSuccess()
    }
}
