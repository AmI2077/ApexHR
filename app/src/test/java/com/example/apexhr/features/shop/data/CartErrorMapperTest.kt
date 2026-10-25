package com.example.apexhr.features.shop.data

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.shop.domain.CartActionError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.UnknownHostException

class CartErrorMapperTest {

    private fun error(code: Int, message: String? = null) =
        CartApiException(code, message).toCartActionError()

    @Test
    fun `server message is read from message detail or title`() {
        assertEquals("Item added to cart.", parseServerMessage("""{"message":"Item added to cart."}"""))
        assertEquals("Подробно", parseServerMessage("""{"title":"Кратко","detail":"Подробно"}"""))
        assertEquals("Кратко", parseServerMessage("""{"title":"Кратко","detail":"  "}"""))
    }

    @Test
    fun `non json body has no message`() {
        assertNull(parseServerMessage(""))
        assertNull(parseServerMessage("<html>Bad gateway</html>"))
        assertNull(parseServerMessage("""["a"]"""))
        assertNull(parseServerMessage("""{"message":42}"""))
    }

    @Test
    fun `real stock errors are out of stock`() {
        assertEquals(CartActionError.OutOfStock, error(400, "Requested quantity exceeds available stock."))
        assertEquals(CartActionError.OutOfStock, error(400, "Product is unavailable."))
        assertEquals(CartActionError.OutOfStock, error(409, "Insufficient stock."))
        assertEquals(CartActionError.OutOfStock, error(400, "Недостаточно товара на складе"))
    }

    @Test
    fun `balance errors are insufficient balance`() {
        assertEquals(CartActionError.InsufficientBalance, error(400, "Insufficient balance."))
        assertEquals(CartActionError.InsufficientBalance, error(409, "Недостаточно средств"))
    }

    @Test
    fun `empty cart error`() {
        assertEquals(CartActionError.EmptyCart, error(400, "Cart is empty."))
    }

    @Test
    fun `status codes without known message`() {
        assertEquals(CartActionError.Forbidden, error(403))
        assertEquals(CartActionError.NotFound, error(404, "Cart item was not found."))
        assertEquals(CartActionError.Conflict, error(409, "Version mismatch"))
    }

    @Test
    fun `other rejections keep server message`() {
        assertEquals(
            CartActionError.Rejected("Invalid item ID or quantity."),
            error(400, "Invalid item ID or quantity."),
        )
        assertEquals(CartActionError.Rejected(null), error(422))
    }

    @Test
    fun `transport errors are data errors`() {
        assertEquals(CartActionError.Data(DataError.Unauthorized), error(401))
        assertEquals(CartActionError.Data(DataError.Server), error(503, "Insufficient balance."))
        assertEquals(CartActionError.Data(DataError.Timeout), error(408))
        assertEquals(
            CartActionError.Data(DataError.Network),
            UnknownHostException("no network").toCartActionError(),
        )
    }
}
