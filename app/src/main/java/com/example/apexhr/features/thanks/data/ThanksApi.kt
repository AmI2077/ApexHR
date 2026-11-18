package com.example.apexhr.features.thanks.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.thanks.data.dto.CreateManagerBonusRequest
import com.example.apexhr.features.thanks.data.dto.CreateThankRequest
import com.example.apexhr.features.thanks.data.dto.WorkBalanceDto
import com.example.apexhr.features.thanks.data.dto.ThankDto
import com.example.apexhr.features.thanks.data.dto.ThanksUserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Благодарности. Загрузка при ответе не 2xx бросает
 * [com.example.apexhr.core.data.network.ApiStatusException], отправка —
 * [ThanksApiException] с текстом ошибки сервера (например, о недельном лимите).
 * Тело ответа отправки не описано в Swagger и не разбирается.
 */
interface ThanksApi {
    suspend fun getCurrentUser(): ThanksUserDto
    suspend fun getUsers(): List<ThanksUserDto>
    suspend fun getThanksForMe(): List<ThankDto>
    suspend fun getThanksFromMe(): List<ThankDto>
    suspend fun sendThanks(request: CreateThankRequest)
    suspend fun getWorkBalance(): WorkBalanceDto
    suspend fun sendManagerBonus(request: CreateManagerBonusRequest)
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorThanksApi(
    private val client: HttpClient,
) : ThanksApi {
    override suspend fun getCurrentUser(): ThanksUserDto =
        client.get(NetworkConfig.USER_PROFILE_URL).requireSuccess().body()

    override suspend fun getUsers(): List<ThanksUserDto> =
        client.get(NetworkConfig.USERS_URL).requireSuccess().body()

    override suspend fun getThanksForMe(): List<ThankDto> =
        client.get(NetworkConfig.THANKS_FOR_ME_URL).requireSuccess().body()

    override suspend fun getThanksFromMe(): List<ThankDto> =
        client.get(NetworkConfig.THANKS_FROM_ME_URL).requireSuccess().body()

    override suspend fun sendThanks(request: CreateThankRequest) {
        client.post(NetworkConfig.THANKS_URL) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireThanksSuccess()
    }

    override suspend fun getWorkBalance(): WorkBalanceDto =
        client.get(NetworkConfig.WORK_BALANCE_URL).requireSuccess().body()

    override suspend fun sendManagerBonus(request: CreateManagerBonusRequest) {
        client.post(NetworkConfig.THANKS_MANAGER_BONUS_URL) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireThanksSuccess()
    }
}
