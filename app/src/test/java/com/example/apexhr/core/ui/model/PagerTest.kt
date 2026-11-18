package com.example.apexhr.core.ui.model

import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.ui.models.ProfileUiState
import com.example.apexhr.features.profile.ui.models.ThankUiModel
import com.example.apexhr.features.shop.domain.CartActionResult
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.domain.CartSummary
import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.CheckoutResult
import com.example.apexhr.features.shop.domain.Order
import com.example.apexhr.features.shop.domain.OrderStatus
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.domain.ProductDetails
import com.example.apexhr.features.shop.domain.ShopRepository
import com.example.apexhr.features.shop.ui.ShopViewModel
import com.example.apexhr.features.shop.ui.models.ShopScreenState
import com.example.apexhr.features.shop.ui.models.ShopSort
import com.example.apexhr.features.shop.ui.orders.OrdersViewModel
import com.example.apexhr.features.shop.ui.orders.models.OrdersScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Перелистывание страниц, как usePage/Pager в вебе: заказы 6, каталог 6, благодарности 5. */
@OptIn(ExperimentalCoroutinesApi::class)
class PagerTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `page slice works like usePage on the web`() {
        val items = (1..13).toList()

        val first = items.pageSlice(page = 1, size = 6)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), first.items)
        assertEquals(3, first.pages)
        assertEquals(13, first.total)
        assertFalse(first.hasPrevious)
        assertTrue(first.hasNext)

        val last = items.pageSlice(page = 3, size = 6)
        assertEquals(listOf(13), last.items)
        assertFalse(last.hasNext)

        // Номер вне диапазона ограничивается.
        assertEquals(3, items.pageSlice(page = 10, size = 6).page)
        assertEquals(1, items.pageSlice(page = 0, size = 6).page)

        val empty = emptyList<Int>().pageSlice(page = 2, size = 6)
        assertEquals(1, empty.page)
        assertEquals(1, empty.pages)
        assertFalse(empty.isPagerVisible)
        assertFalse((1..6).toList().pageSlice(1, 6).isPagerVisible)
    }

    @Test
    fun `orders are paged by 6`() {
        val orders = (1L..8L).map { Order(it, OrderStatus.Paid, "Paid", 10, null, emptyList()) }
        val viewModel = OrdersViewModel(PagerCartRepository(orders))
        fun content() = viewModel.state.value as OrdersScreenState.Content

        assertEquals(6, content().pageSlice.items.size)
        viewModel.onPageChange(2)
        assertEquals(listOf(7L, 8L), content().pageSlice.items.map { it.id })
        viewModel.onPageChange(5)
        assertEquals(2, content().page)
    }

    @Test
    fun `catalog is paged by 6 and filters reset the page`() {
        val products = (1L..14L).map { Product(it, null, "Товар $it", null, it * 10, null, null) }
        val viewModel = ShopViewModel(PagerShopRepository(products), PagerCartRepository(emptyList()))
        fun ui() = (viewModel.state.value as ShopScreenState.Content).uiState

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L), ui().pageSlice.items.map { it.id.toLong() })
        assertEquals(3, ui().pageSlice.pages)
        viewModel.onPageChange(3)
        assertEquals(listOf("13", "14"), ui().pageSlice.items.map { it.id })

        viewModel.onSortClick(ShopSort.Expensive)
        assertEquals(1, ui().page)

        viewModel.onPageChange(2)
        viewModel.onSearchQueryChange("Товар 1")
        assertEquals(1, ui().page)
        assertEquals(6, ui().pageSlice.total)

        viewModel.onPageChange(2)
        viewModel.onCategoryClick("5")
        assertEquals(1, ui().page)
    }

    @Test
    fun `profile thanks are paged by 5 separately`() {
        fun thanks(count: Int) = (1..count).map { ThankUiModel(id = "$it", name = null, amount = 25, message = null, date = null) }
        val state = ProfileUiState.Preview.copy(received = thanks(12), sent = thanks(3), receivedPage = 3, sentPage = 2)

        assertEquals(listOf("11", "12"), state.receivedSlice.items.map { it.id })
        assertEquals(3, state.receivedSlice.pages)
        assertEquals(1, state.sentSlice.page)
        assertFalse(state.sentSlice.isPagerVisible)
    }

    private class PagerShopRepository(private val products: List<Product>) : ShopRepository {
        override suspend fun getCategories(): DataResult<List<Category>> = DataResult.Success(emptyList())
        override suspend fun getProducts(categoryId: Long?): DataResult<List<Product>> = DataResult.Success(products)
        override suspend fun getProductDetails(productId: Long): DataResult<ProductDetails> = error("не нужен")
    }

    private class PagerCartRepository(private val orders: List<Order>) : CartRepository {
        override suspend fun loadCart(): DataResult<CartSummary> = error("не нужен")
        override suspend fun addToCart(productId: Long, quantity: Int, variantId: Long?): CartActionResult = error("не нужен")
        override suspend fun updateQuantity(itemId: Long, quantity: Int): CartActionResult = error("не нужен")
        override suspend fun removeItem(itemId: Long): CartActionResult = error("не нужен")
        override suspend fun checkout(): CheckoutResult = error("не нужен")
        override suspend fun getOrders(): DataResult<List<Order>> = DataResult.Success(orders)
    }
}
