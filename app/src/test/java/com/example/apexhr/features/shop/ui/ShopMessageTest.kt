package com.example.apexhr.features.shop.ui

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.shop.domain.CartActionError
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.toShopMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class ShopMessageTest {

    @Test
    fun `business errors have own texts`() {
        assertEquals(
            ShopMessage(R.string.cart_error_insufficient_balance),
            CartActionError.InsufficientBalance.toShopMessage(),
        )
        assertEquals(ShopMessage(R.string.cart_error_out_of_stock), CartActionError.OutOfStock.toShopMessage())
        assertEquals(ShopMessage(R.string.cart_error_empty), CartActionError.EmptyCart.toShopMessage())
        assertEquals(ShopMessage(R.string.cart_error_not_found), CartActionError.NotFound.toShopMessage())
        assertEquals(ShopMessage(R.string.cart_error_forbidden), CartActionError.Forbidden.toShopMessage())
        assertEquals(ShopMessage(R.string.cart_error_conflict), CartActionError.Conflict.toShopMessage())
    }

    @Test
    fun `server text is shown for other rejections`() {
        assertEquals(
            ShopMessage(R.string.cart_error_server_message, listOf("Invalid item ID or quantity.")),
            CartActionError.Rejected("Invalid item ID or quantity.").toShopMessage(),
        )
        assertEquals(ShopMessage(R.string.cart_error_action), CartActionError.Rejected(" ").toShopMessage())
        assertEquals(ShopMessage(R.string.cart_error_action), CartActionError.Rejected(null).toShopMessage())
    }

    @Test
    fun `data errors reuse common texts`() {
        assertEquals(
            ShopMessage(R.string.data_error_network),
            CartActionError.Data(DataError.Network).toShopMessage(),
        )
        assertEquals(
            ShopMessage(R.string.auth_session_expired),
            CartActionError.Data(DataError.Unauthorized).toShopMessage(),
        )
        assertEquals(
            ShopMessage(R.string.cart_error_action),
            CartActionError.Data(DataError.Unknown).toShopMessage(),
        )
    }
}
