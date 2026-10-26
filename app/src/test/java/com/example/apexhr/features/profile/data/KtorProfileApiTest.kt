package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.features.profile.data.dto.BalanceDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * Проверяет обработку кодов ответа в [KtorProfileApi]. ktor-client-mock в проекте
 * нет, поэтому ответ сервера подставляет OkHttp-интерсептор того же движка OkHttp,
 * что и в приложении: запрос в сеть не уходит.
 */
class KtorProfileApiTest {

    @Test
    fun `balance is read from response`() = runBlocking {
        val api = apiResponding(200, """{"amount":120}""")

        assertEquals(BalanceDto(amount = 120), api.getBalance())
    }

    @Test
    fun `missing balance of new employee is zero`() = runBlocking {
        val api = apiResponding(404, """{"message":"Balance was not found."}""")

        assertEquals(BalanceDto(amount = 0), api.getBalance())
    }

    @Test
    fun `other balance errors are still thrown`() = runBlocking {
        for (code in listOf(401, 500)) {
            val api = apiResponding(code, """{"message":"error"}""")
            try {
                api.getBalance()
                fail("HTTP $code должен бросать ApiStatusException")
            } catch (e: ApiStatusException) {
                assertEquals(code, e.statusCode)
            }
        }
    }

    private fun apiResponding(code: Int, body: String) = KtorProfileApi(
        HttpClient(OkHttp) {
            expectSuccess = false
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addInterceptor { chain ->
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(code)
                        .header("Content-Type", "application/json")
                        .message("HTTP $code")
                        .body(body.toResponseBody("application/json".toMediaType()))
                        .build()
                }
            }
        }
    )
}
