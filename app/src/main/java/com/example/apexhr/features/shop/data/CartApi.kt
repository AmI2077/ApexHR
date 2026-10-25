package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.shop.data.dto.AddCartItemRequest
import com.example.apexhr.features.shop.data.dto.CartItemDto
import com.example.apexhr.features.shop.data.dto.OrderDto
import com.example.apexhr.features.shop.data.dto.UpdateCartItemRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException

/**
 * Корзина и заказы. Действия (добавить, изменить, удалить, оформить) при ответе
 * не 2xx бросают [CartApiException] с текстом ошибки сервера, загрузка — как
 * остальные запросы, [com.example.apexhr.core.data.network.ApiStatusException].
 */
interface CartApi {
    suspend fun getCart(): List<CartItemDto>
    suspend fun addItem(productId: Long, quantity: Int)
    suspend fun updateItem(itemId: Long, quantity: Int)
    suspend fun removeItem(itemId: Long)

    /** @return созданный заказ или null, если тело ответа не удалось разобрать. */
    suspend fun checkout(): OrderDto?
    suspend fun getOrders(): List<OrderDto>
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorCartApi(
    private val client: HttpClient,
) : CartApi {
    override suspend fun getCart(): List<CartItemDto> =
        client.get(NetworkConfig.CART_URL).requireSuccess().body()

    override suspend fun addItem(productId: Long, quantity: Int) {
        client.post(NetworkConfig.CART_ITEMS_URL) {
            contentType(ContentType.Application.Json)
            setBody(AddCartItemRequest(productId = productId, quantity = quantity))
        }.requireActionSuccess()
    }

    override suspend fun updateItem(itemId: Long, quantity: Int) {
        client.put(NetworkConfig.cartItemUrl(itemId)) {
            contentType(ContentType.Application.Json)
            setBody(UpdateCartItemRequest(quantity = quantity))
        }.requireActionSuccess()
    }

    override suspend fun removeItem(itemId: Long) {
        client.delete(NetworkConfig.cartItemUrl(itemId)).requireActionSuccess()
    }

    override suspend fun checkout(): OrderDto? {
        val response = client.post(NetworkConfig.ORDERS_CHECKOUT_URL).requireActionSuccess()
        // Заказ уже создан: даже если тело неожиданное, это успех.
        return try {
            response.body<OrderDto>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getOrders(): List<OrderDto> =
        client.get(NetworkConfig.ORDERS_URL).requireSuccess().body()
}
