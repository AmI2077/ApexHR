package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.shop.domain.Cart
import com.example.apexhr.features.shop.domain.CartActionResult
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.domain.CartSummary
import com.example.apexhr.features.shop.domain.CheckoutResult
import com.example.apexhr.features.shop.domain.Order
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Баланс — тот же запрос GET /api/balances/me, что у профиля и главной,
 * поэтому берётся из ProfileApi.
 */
class CartRepositoryImpl(
    private val cartApi: CartApi,
    private val profileApi: ProfileApi,
) : CartRepository {

    override suspend fun loadCart(): DataResult<CartSummary> = safeApiCall {
        coroutineScope {
            val balance = async { profileApi.getBalance() }
            val items = cartApi.getCart().map { it.toDomain() }
            CartSummary(cart = Cart(items), balance = balance.await().amount)
        }
    }

    override suspend fun addToCart(productId: Long, quantity: Int, variantId: Long?): CartActionResult =
        action { cartApi.addItem(productId, quantity, variantId) }

    override suspend fun updateQuantity(itemId: Long, quantity: Int): CartActionResult =
        action { cartApi.updateItem(itemId, quantity) }

    override suspend fun removeItem(itemId: Long): CartActionResult =
        action { cartApi.removeItem(itemId) }

    override suspend fun checkout(): CheckoutResult =
        safeCartAction { cartApi.checkout() }.fold(
            onSuccess = { order -> CheckoutResult.Success(orderId = order?.id) },
            onFailure = { CheckoutResult.Failure(it.toCartActionError()) },
        )

    override suspend fun getOrders(): DataResult<List<Order>> = safeApiCall {
        cartApi.getOrders()
            .sortedByDescending { it.id }
            .map { it.toDomain() }
    }

    private suspend fun action(block: suspend () -> Unit): CartActionResult =
        safeCartAction(block).fold(
            onSuccess = { CartActionResult.Success },
            onFailure = { CartActionResult.Failure(it.toCartActionError()) },
        )
}
