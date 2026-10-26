package com.example.apexhr.features.shop.ui.orders

import com.example.apexhr.R
import com.example.apexhr.features.shop.domain.Order
import com.example.apexhr.features.shop.domain.OrderItem
import com.example.apexhr.features.shop.domain.OrderStatus
import com.example.apexhr.features.shop.ui.orders.models.OrderItemUiModel
import com.example.apexhr.features.shop.ui.orders.models.OrderUiModel
import com.example.apexhr.features.shop.ui.orders.models.labelRes
import com.example.apexhr.features.shop.ui.orders.models.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class OrdersUiMapperTest {

    private val moscow = ZoneId.of("Europe/Moscow")

    private val paidOrder = Order(
        id = 6,
        status = OrderStatus.Paid,
        rawStatus = "Paid",
        totalAmount = 1050,
        createdAt = Instant.parse("2026-10-25T20:16:44.773847Z"),
        items = listOf(
            OrderItem(id = 1, productId = 1, name = "Тестовый товар", quantity = 1, price = 50),
            OrderItem(id = 2, productId = 2, name = null, quantity = 2, price = 500),
        ),
    )

    @Test
    fun `order is mapped with local date and status`() {
        assertEquals(
            OrderUiModel(
                id = 6,
                createdAt = "25.10.2026 23:16",
                statusRes = R.string.order_status_paid,
                rawStatus = "Paid",
                total = 1050,
                items = listOf(
                    OrderItemUiModel(title = "Тестовый товар", quantity = 1, total = 50),
                    OrderItemUiModel(title = "", quantity = 2, total = 1000),
                ),
            ),
            paidOrder.toUiModel(moscow),
        )
    }

    @Test
    fun `order line total is price times quantity`() {
        val item = OrderItem(id = 3, productId = 3, name = "Кружка", quantity = 3, price = 1_250)

        assertEquals(OrderItemUiModel(title = "Кружка", quantity = 3, total = 3_750), item.toUiModel())
    }

    @Test
    fun `unknown status keeps server value and missing date stays empty`() {
        val model = paidOrder
            .copy(status = OrderStatus.Unknown, rawStatus = "OnHold", createdAt = null)
            .toUiModel(moscow)

        assertNull(model.statusRes)
        assertEquals("OnHold", model.rawStatus)
        assertNull(model.createdAt)
    }

    @Test
    fun `blank raw status is dropped`() {
        val model = paidOrder.copy(status = OrderStatus.Unknown, rawStatus = " ").toUiModel(moscow)

        assertNull(model.rawStatus)
    }

    @Test
    fun `every known status has label`() {
        OrderStatus.entries
            .filter { it != OrderStatus.Unknown }
            .forEach { status -> assertEquals(true, status.labelRes() != null) }
        assertNull(OrderStatus.Unknown.labelRes())
    }
}
