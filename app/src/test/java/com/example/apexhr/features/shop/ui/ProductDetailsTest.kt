package com.example.apexhr.features.shop.ui

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.shop.domain.CartActionError
import com.example.apexhr.features.shop.domain.CartActionResult
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.domain.CartSummary
import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.CheckoutResult
import com.example.apexhr.features.shop.domain.Order
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.domain.ProductBadge
import com.example.apexhr.features.shop.domain.ProductDetails
import com.example.apexhr.features.shop.domain.ProductVariant
import com.example.apexhr.features.shop.domain.ShopRepository
import com.example.apexhr.features.shop.ui.models.ShopBadgeUiModel
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.ShopMessageAction
import com.example.apexhr.features.shop.ui.models.ShopSort
import com.example.apexhr.features.shop.ui.models.sortedBy
import com.example.apexhr.features.shop.ui.product.ProductDetailsViewModel
import com.example.apexhr.features.shop.ui.product.models.ProductDetailsScreenState
import com.example.apexhr.features.shop.ui.product.models.ProductDetailsUiState
import com.example.apexhr.features.shop.ui.product.models.defaultVariantId
import com.example.apexhr.features.shop.ui.product.models.toUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailsTest {

    private val hoodie = Product(
        id = 2,
        categoryId = 1,
        name = "Худи",
        description = null,
        price = 80,
        imageUrl = "https://x/blue.jpg",
        stock = 7,
        badges = listOf(ProductBadge("Новинка", 0xFF306CC0)),
    )
    private val red = ProductVariant(id = 1, name = "Красный", imageUrl = "https://x/red.jpg", price = 80, stock = 0)
    private val blue = ProductVariant(
        id = 17,
        name = "Синее",
        imageUrl = "https://x/blue.jpg",
        price = 72,
        originalPrice = 80,
        discountPercent = 10,
        stock = 8,
        badges = listOf(ProductBadge("Акция", 0xFF0EA5E9)),
    )

    private val shop = FakeShopRepository()
    private val cart = FakeCartRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun ProductDetailsViewModel.content(): ProductDetailsUiState =
        (state.value as ProductDetailsScreenState.Content).uiState

    @Test
    fun `default variant has product image or is first`() {
        assertEquals(17L, ProductDetails(hoodie, listOf(red, blue)).defaultVariantId())
        assertEquals(1L, ProductDetails(hoodie.copy(imageUrl = null), listOf(red, blue)).defaultVariantId())
        assertNull(ProductDetails(hoodie, emptyList()).defaultVariantId())
    }

    @Test
    fun `selected variant sets price stock image and badges`() {
        val details = ProductDetails(hoodie, listOf(red, blue))

        val blueState = details.toUiState(17)
        assertEquals(72L, blueState.price)
        assertEquals(80L, blueState.originalPrice)
        assertEquals(10, blueState.discountPercent)
        assertEquals(8, blueState.stock)
        assertEquals(listOf(ShopBadgeUiModel("Акция", 0xFF0EA5E9)), blueState.badges)
        assertFalse(blueState.isDigital)

        val redState = details.toUiState(1)
        assertEquals("https://x/red.jpg", redState.imageUrl)
        assertNull(redState.originalPrice)
        assertEquals(listOf(ShopBadgeUiModel("Новинка", 0xFF306CC0)), redState.badges)
        assertFalse(redState.canAddToCart)

        val plain = ProductDetails(hoodie.copy(isDigital = true), emptyList()).toUiState(null)
        assertEquals(80L, plain.price)
        assertTrue(plain.isDigital)
        assertTrue(plain.canAddToCart)
    }

    @Test
    fun `chosen variant is added to cart`() {
        shop.details = DataResult.Success(ProductDetails(hoodie, listOf(red, blue)))
        val viewModel = ProductDetailsViewModel(2, shop, cart)
        val messages = mutableListOf<ShopMessage>()
        viewModel.messages.onEach { messages += it }.launchIn(CoroutineScope(Dispatchers.Main))

        assertEquals(17L, viewModel.content().selectedVariantId)
        viewModel.onVariantClick(1)
        assertEquals(1L, viewModel.content().selectedVariantId)
        viewModel.onAddToCartClick()
        assertTrue(cart.added.isEmpty())

        viewModel.onVariantClick(17)
        viewModel.onAddToCartClick()

        assertEquals(listOf(Triple(2L, 1, 17L)), cart.added)
        assertEquals(listOf(ShopMessage(R.string.shop_added_to_cart, action = ShopMessageAction.OpenCart)), messages)
        assertFalse(viewModel.content().isAddingToCart)
    }

    @Test
    fun `cart error is shown and load error can be retried`() {
        shop.details = DataResult.Failure(DataError.Network)
        val viewModel = ProductDetailsViewModel(2, shop, cart)
        assertEquals(ProductDetailsScreenState.Error(DataError.Network), viewModel.state.value)

        shop.details = DataResult.Success(ProductDetails(hoodie, emptyList()))
        viewModel.onRetryClick()
        cart.result = CartActionResult.Failure(CartActionError.OutOfStock)
        val messages = mutableListOf<ShopMessage>()
        viewModel.messages.onEach { messages += it }.launchIn(CoroutineScope(Dispatchers.Main))

        viewModel.onAddToCartClick()

        assertEquals(listOf(Triple(2L, 1, null)), cart.added)
        assertEquals(listOf(ShopMessage(R.string.cart_error_out_of_stock)), messages)
    }

    @Test
    fun `catalog sort like in web`() {
        val products = listOf(hoodie.copy(id = 1, price = 80), hoodie.copy(id = 2, price = 40), hoodie.copy(id = 3, price = 200))

        assertEquals(listOf(1L, 2L, 3L), products.sortedBy(ShopSort.Popular).map { it.id })
        assertEquals(listOf(2L, 1L, 3L), products.sortedBy(ShopSort.Cheaper).map { it.id })
        assertEquals(listOf(3L, 1L, 2L), products.sortedBy(ShopSort.Expensive).map { it.id })
    }
}

private class FakeShopRepository : ShopRepository {
    var details: DataResult<ProductDetails> = DataResult.Failure(DataError.Unknown)

    override suspend fun getCategories(): DataResult<List<Category>> = DataResult.Success(emptyList())
    override suspend fun getProducts(categoryId: Long?): DataResult<List<Product>> = DataResult.Success(emptyList())
    override suspend fun getProductDetails(productId: Long): DataResult<ProductDetails> = details
}

private class FakeCartRepository : CartRepository {
    val added = mutableListOf<Triple<Long, Int, Long?>>()
    var result: CartActionResult = CartActionResult.Success

    override suspend fun addToCart(productId: Long, quantity: Int, variantId: Long?): CartActionResult {
        added += Triple(productId, quantity, variantId)
        return result
    }

    override suspend fun loadCart(): DataResult<CartSummary> = error("не нужен")
    override suspend fun updateQuantity(itemId: Long, quantity: Int): CartActionResult = error("не нужен")
    override suspend fun removeItem(itemId: Long): CartActionResult = error("не нужен")
    override suspend fun checkout(): CheckoutResult = error("не нужен")
    override suspend fun getOrders(): DataResult<List<Order>> = error("не нужен")
}