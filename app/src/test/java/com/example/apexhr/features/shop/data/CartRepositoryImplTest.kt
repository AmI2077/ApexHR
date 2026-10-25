package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import com.example.apexhr.features.shop.data.dto.CartItemDto
import com.example.apexhr.features.shop.data.dto.OrderDto
import com.example.apexhr.features.shop.domain.CartActionError
import com.example.apexhr.features.shop.domain.CartActionResult
import com.example.apexhr.features.shop.domain.CartSummary
import com.example.apexhr.features.shop.domain.CheckoutResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class CartRepositoryImplTest {

    private val cartApi = FakeCartApi()
    private val profileApi = FakeBalanceApi()
    private val repository = CartRepositoryImpl(cartApi, profileApi)

    private suspend fun loadSummary(): CartSummary =
        (repository.loadCart() as DataResult.Success).data

    @Test
    fun `cart is loaded with balance`() = runBlocking {
        cartApi.cart = listOf(
            CartItemDto(id = 1, productId = 10, unitPrice = 50, quantity = 2, total = 100),
            CartItemDto(id = 2, productId = 11, unitPrice = 20, quantity = 1, total = 20),
        )
        profileApi.balance = 100

        val summary = loadSummary()

        assertEquals(listOf(1L, 2L), summary.cart.items.map { it.id })
        assertEquals(120L, summary.cart.total)
        assertEquals(100L, summary.balance)
        assertFalse(summary.isBalanceEnough)
        assertEquals(20L, summary.missingAmount)
    }

    @Test
    fun `enough balance has nothing missing`() = runBlocking {
        cartApi.cart = listOf(CartItemDto(id = 1, unitPrice = 50, quantity = 1, total = 50))
        profileApi.balance = 71

        val summary = loadSummary()

        assertTrue(summary.isBalanceEnough)
        assertEquals(0L, summary.missingAmount)
    }

    @Test
    fun `empty cart is loaded`() = runBlocking {
        val summary = loadSummary()

        assertTrue(summary.cart.items.isEmpty())
        assertEquals(0L, summary.cart.total)
        assertTrue(summary.isBalanceEnough)
    }

    @Test
    fun `balance failure fails cart loading`() = runBlocking {
        profileApi.balanceError = ApiStatusException(500)

        assertEquals(DataResult.Failure(DataError.Server), repository.loadCart())
    }

    @Test
    fun `cart failure is mapped`() = runBlocking {
        cartApi.cartError = UnknownHostException("no network")

        assertEquals(DataResult.Failure(DataError.Network), repository.loadCart())
    }

    @Test
    fun `add to cart passes product and quantity`() = runBlocking {
        val result = repository.addToCart(productId = 7)

        assertEquals(CartActionResult.Success, result)
        assertEquals(7L to 1, cartApi.added)
    }

    @Test
    fun `update and remove use cart item id`() = runBlocking {
        assertEquals(CartActionResult.Success, repository.updateQuantity(itemId = 3, quantity = 4))
        assertEquals(CartActionResult.Success, repository.removeItem(itemId = 5))

        assertEquals(3L to 4, cartApi.updated)
        assertEquals(5L, cartApi.removed)
    }

    @Test
    fun `server rejection is mapped`() = runBlocking {
        cartApi.actionError = CartApiException(400, "Requested quantity exceeds available stock.")

        assertEquals(
            CartActionResult.Failure(CartActionError.OutOfStock),
            repository.updateQuantity(itemId = 1, quantity = 10),
        )
    }

    @Test
    fun `network error of action is mapped`() = runBlocking {
        cartApi.actionError = UnknownHostException("no network")

        assertEquals(
            CartActionResult.Failure(CartActionError.Data(DataError.Network)),
            repository.addToCart(productId = 1),
        )
    }

    @Test
    fun `checkout returns order id`() = runBlocking {
        cartApi.checkoutOrder = OrderDto(id = 6, status = "Paid", totalAmount = 50)

        assertEquals(CheckoutResult.Success(orderId = 6), repository.checkout())
        assertEquals(1, cartApi.checkoutCalls)
    }

    @Test
    fun `checkout without parsed body is still success`() = runBlocking {
        cartApi.checkoutOrder = null

        assertEquals(CheckoutResult.Success(orderId = null), repository.checkout())
    }

    @Test
    fun `checkout rejection is mapped`() = runBlocking {
        cartApi.actionError = CartApiException(400, "Insufficient balance.")

        assertEquals(
            CheckoutResult.Failure(CartActionError.InsufficientBalance),
            repository.checkout(),
        )
    }

    @Test
    fun `orders are sorted newest first`() = runBlocking {
        cartApi.orders = listOf(OrderDto(id = 4), OrderDto(id = 6), OrderDto(id = 5))

        val orders = (repository.getOrders() as DataResult.Success).data

        assertEquals(listOf(6L, 5L, 4L), orders.map { it.id })
    }

    @Test
    fun `orders failure is mapped`() = runBlocking {
        cartApi.ordersError = ApiStatusException(401)

        assertEquals(DataResult.Failure(DataError.Unauthorized), repository.getOrders())
    }
}

private class FakeCartApi : CartApi {
    var cart = emptyList<CartItemDto>()
    var orders = emptyList<OrderDto>()
    var checkoutOrder: OrderDto? = OrderDto(id = 1)
    var cartError: Throwable? = null
    var ordersError: Throwable? = null
    var actionError: Throwable? = null

    var added: Pair<Long, Int>? = null
    var updated: Pair<Long, Int>? = null
    var removed: Long? = null
    var checkoutCalls = 0

    override suspend fun getCart(): List<CartItemDto> {
        cartError?.let { throw it }
        return cart
    }

    override suspend fun addItem(productId: Long, quantity: Int) {
        actionError?.let { throw it }
        added = productId to quantity
    }

    override suspend fun updateItem(itemId: Long, quantity: Int) {
        actionError?.let { throw it }
        updated = itemId to quantity
    }

    override suspend fun removeItem(itemId: Long) {
        actionError?.let { throw it }
        removed = itemId
    }

    override suspend fun checkout(): OrderDto? {
        actionError?.let { throw it }
        checkoutCalls++
        return checkoutOrder
    }

    override suspend fun getOrders(): List<OrderDto> {
        ordersError?.let { throw it }
        return orders
    }
}

private class FakeBalanceApi : ProfileApi {
    var balance = 0L
    var balanceError: Throwable? = null

    override suspend fun getBalance(): BalanceDto {
        balanceError?.let { throw it }
        return BalanceDto(amount = balance)
    }

    override suspend fun getCurrentUser(): CurrentUserProfileDto = error("not used")

    override suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto> = error("not used")

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> = error("not used")
}
