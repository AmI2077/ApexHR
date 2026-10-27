package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Запросы KtorCartApi к заказам; в сеть ничего не уходит. */
class KtorCartApiTest {

    @Test
    fun `orders are loaded from history endpoint`() = runBlocking {
        val http = RecordingHttpClient(body = "[]")

        val orders = KtorCartApi(http.client).getOrders()

        val request = http.requests.single()
        assertEquals("GET", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/orders/history", request.url.toString())
        assertTrue(orders.isEmpty())
    }

    @Test
    fun `orders history response is parsed`() = runBlocking {
        val body = """
            [{"id":6,"status":"Paid","totalAmount":50,"createdAt":"2026-10-25T10:00:00Z",
              "updatedAt":null,"comment":null,
              "items":[{"id":9,"productId":1,"productName":"Худи","quantity":1,"priceAtPurchase":50}]}]
        """.trimIndent()
        val http = RecordingHttpClient(body = body)

        val order = KtorCartApi(http.client).getOrders().single()

        assertEquals(6L, order.id)
        assertEquals("Paid", order.status)
        assertEquals(50L, order.totalAmount)
        assertEquals("Худи", order.items.single().productName)
        assertEquals(1, order.items.single().quantity)
    }

    @Test
    fun `checkout keeps its own endpoint`() = runBlocking {
        val http = RecordingHttpClient(code = 201, body = """{"id":7}""")

        KtorCartApi(http.client).checkout()

        assertEquals("${NetworkConfig.BASE_URL}/api/orders/checkout", http.requests.single().url.toString())
    }

    /** OkHttp без сети: запоминает запросы и отвечает JSON с кодом [code]. */
    private class RecordingHttpClient(code: Int = 200, body: String = "{}") {
        val requests = mutableListOf<Request>()

        val client = HttpClient(OkHttp) {
            expectSuccess = false
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false; coerceInputValues = true })
            }
            engine {
                addInterceptor { chain ->
                    requests += chain.request()
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(code)
                        .message("HTTP $code")
                        .header("Content-Type", "application/json")
                        .body(body.toResponseBody("application/json".toMediaType()))
                        .build()
                }
            }
        }
    }
}