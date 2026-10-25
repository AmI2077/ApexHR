package com.example.apexhr.features.shop.data

import com.example.apexhr.features.shop.data.dto.CartItemDto
import com.example.apexhr.features.shop.data.dto.OrderDto
import com.example.apexhr.features.shop.data.dto.OrderItemDto
import com.example.apexhr.features.shop.domain.OrderStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class CartMappersTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `cart item is mapped`() {
        val item = CartItemDto(
            id = 57,
            productId = 1,
            productName = "  Худи  ",
            imageUrl = "",
            unitPrice = 50,
            quantity = 2,
            total = 100,
        ).toDomain()

        assertEquals(57L, item.id)
        assertEquals(1L, item.productId)
        assertEquals("Худи", item.name)
        assertNull(item.imageUrl)
        assertEquals(100L, item.total)
    }

    @Test
    fun `missing item total is calculated`() {
        val item = CartItemDto(id = 1, unitPrice = 30, quantity = 3).toDomain()

        assertEquals(90L, item.total)
    }

    @Test
    fun `real cart response is parsed`() {
        val body = """[{"id":57,"productId":1,"productName":"Тестовый товар","imageUrl":"",
            "unitPrice":50,"quantity":1,"total":50,"addedAt":"2026-10-25T20:13:00.599349Z"}]"""

        val items = json.decodeFromString<List<CartItemDto>>(body).map { it.toDomain() }

        assertEquals(1, items.size)
        assertEquals("Тестовый товар", items[0].name)
        assertEquals(50L, items[0].total)
    }

    @Test
    fun `real orders response is parsed`() {
        val body = """[{"id":5,"status":"Paid","totalAmount":50,
            "createdAt":"2026-10-25T16:51:01.452529Z","updatedAt":"2026-10-25T16:51:01.452529Z",
            "comment":null,"items":[{"id":5,"productId":1,"productName":"Тестовый товар",
            "quantity":1,"priceAtPurchase":50}]}]"""

        val order = json.decodeFromString<List<OrderDto>>(body).single().toDomain()

        assertEquals(5L, order.id)
        assertEquals(OrderStatus.Paid, order.status)
        assertEquals(50L, order.totalAmount)
        assertEquals(Instant.parse("2026-10-25T16:51:01.452529Z"), order.createdAt)
        assertEquals("Тестовый товар", order.items.single().name)
        assertEquals(50L, order.items.single().price)
    }

    @Test
    fun `real checkout response is parsed`() {
        val body = """{"id":6,"status":"Paid","totalAmount":50,
            "createdAt":"2026-10-25T20:16:44.773847Z",
            "items":[{"productId":1,"quantity":1,"priceAtPurchase":50}],"newAchievement":null}"""

        val order = json.decodeFromString<OrderDto>(body).toDomain()

        assertEquals(6L, order.id)
        assertEquals(1L, order.items.single().productId)
        assertNull(order.items.single().name)
    }

    @Test
    fun `missing order total is summed from items`() {
        val order = OrderDto(
            id = 1,
            items = listOf(
                OrderItemDto(quantity = 2, priceAtPurchase = 30),
                OrderItemDto(quantity = 1, priceAtPurchase = 15),
            ),
        ).toDomain()

        assertEquals(75L, order.totalAmount)
    }

    @Test
    fun `order statuses are mapped ignoring case`() {
        assertEquals(OrderStatus.Paid, "PAID".toOrderStatus())
        assertEquals(OrderStatus.Pending, "New".toOrderStatus())
        assertEquals(OrderStatus.Cancelled, "canceled".toOrderStatus())
        assertEquals(OrderStatus.Completed, " Delivered ".toOrderStatus())
        assertEquals(OrderStatus.Unknown, "Archived".toOrderStatus())
        assertEquals(OrderStatus.Unknown, null.toOrderStatus())
    }

    @Test
    fun `unknown status keeps raw value`() {
        val order = OrderDto(id = 1, status = " Archived ").toDomain()

        assertEquals(OrderStatus.Unknown, order.status)
        assertEquals("Archived", order.rawStatus)
    }

    @Test
    fun `dates with and without zone are parsed`() {
        val expected = Instant.parse("2026-10-25T16:51:01Z")

        assertEquals(expected, "2026-10-25T16:51:01Z".toInstantOrNull())
        assertEquals(expected, "2026-10-25T19:51:01+03:00".toInstantOrNull())
        assertEquals(expected, "2026-10-25T16:51:01".toInstantOrNull())
        assertNull("вчера".toInstantOrNull())
        assertNull("".toInstantOrNull())
        assertNull(null.toInstantOrNull())
    }
}
