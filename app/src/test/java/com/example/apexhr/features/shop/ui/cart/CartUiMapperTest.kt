package com.example.apexhr.features.shop.ui.cart

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.shop.domain.Cart
import com.example.apexhr.features.shop.domain.CartActionError
import com.example.apexhr.features.shop.domain.CartItem
import com.example.apexhr.features.shop.domain.CartSummary
import com.example.apexhr.features.shop.domain.CheckoutResult
import com.example.apexhr.features.shop.ui.cart.models.CartItemUiModel
import com.example.apexhr.features.shop.ui.cart.models.toShopMessage
import com.example.apexhr.features.shop.ui.cart.models.toUiModel
import com.example.apexhr.features.shop.ui.cart.models.toUiState
import com.example.apexhr.features.shop.ui.models.ShopMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CartUiMapperTest {

    private val hoodie = CartItem(
        id = 58,
        productId = 1,
        name = "Худи",
        imageUrl = "https://cdn.example.com/hoodie.png",
        unitPrice = 1990,
        quantity = 2,
        total = 3980,
    )
    private val sticker = CartItem(
        id = 59,
        productId = 2,
        name = null,
        imageUrl = "",
        unitPrice = 50,
        quantity = 1,
        total = 50,
    )

    @Test
    fun `cart item is mapped with formatted amounts`() {
        assertEquals(
            CartItemUiModel(
                id = 58,
                title = "Худи",
                imageUrl = "https://cdn.example.com/hoodie.png",
                unitPrice = "1\u00A0990",
                total = "3\u00A0980",
                quantity = 2,
                canDecrease = true,
                isUpdating = false,
            ),
            hoodie.toUiModel(),
        )
    }

    @Test
    fun `last piece cannot be decreased and empty image is dropped`() {
        val model = sticker.toUiModel(isUpdating = true)

        assertFalse(model.canDecrease)
        assertNull(model.imageUrl)
        assertEquals("", model.title)
        assertTrue(model.isUpdating)
    }

    @Test
    fun `enough balance has no missing amount`() {
        val state = CartSummary(Cart(listOf(hoodie, sticker)), balance = 5000).toUiState()

        assertEquals("4\u00A0030", state.total)
        assertEquals("5\u00A0000", state.balance)
        assertTrue(state.isBalanceEnough)
        assertEquals("0", state.missingAmount)
    }

    @Test
    fun `short balance shows missing amount`() {
        val state = CartSummary(Cart(listOf(sticker)), balance = 21).toUiState()

        assertFalse(state.isBalanceEnough)
        assertEquals("29", state.missingAmount)
    }

    @Test
    fun `updating item ids are marked`() {
        val state = CartSummary(Cart(listOf(hoodie, sticker)), balance = 0)
            .toUiState(updatingItemIds = setOf(59L))

        assertEquals(listOf(false, true), state.items.map { it.isUpdating })
    }

    @Test
    fun `checkout is allowed only for affordable idle cart`() {
        val summary = CartSummary(Cart(listOf(hoodie, sticker)), balance = 5000)

        assertTrue(summary.toUiState().canCheckout)
        assertFalse(summary.toUiState(isCheckoutInProgress = true).canCheckout)
        assertFalse(summary.toUiState(updatingItemIds = setOf(58L)).canCheckout)
        assertFalse(summary.copy(balance = 100).toUiState().canCheckout)
        assertFalse(CartSummary(Cart(emptyList()), balance = 5000).toUiState().canCheckout)
    }

    @Test
    fun `checkout success shows order number`() {
        assertEquals(
            ShopMessage(R.string.cart_checkout_success, listOf(6L)),
            CheckoutResult.Success(orderId = 6).toShopMessage(),
        )
        assertEquals(
            ShopMessage(R.string.cart_checkout_success_no_number),
            CheckoutResult.Success(orderId = null).toShopMessage(),
        )
    }

    @Test
    fun `checkout timeout warns that order may exist`() {
        assertEquals(
            ShopMessage(R.string.cart_checkout_timeout),
            CheckoutResult.Failure(CartActionError.Data(DataError.Timeout)).toShopMessage(),
        )
    }

    @Test
    fun `checkout rejection uses cart error texts`() {
        assertEquals(
            ShopMessage(R.string.cart_error_insufficient_balance),
            CheckoutResult.Failure(CartActionError.InsufficientBalance).toShopMessage(),
        )
        assertEquals(
            ShopMessage(R.string.cart_error_server_message, listOf("Cart is locked.")),
            CheckoutResult.Failure(CartActionError.Rejected("Cart is locked.")).toShopMessage(),
        )
    }
}
