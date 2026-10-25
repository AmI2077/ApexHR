package com.example.apexhr.features.shop.data

import com.example.apexhr.features.shop.data.dto.CartItemDto
import com.example.apexhr.features.shop.data.dto.OrderDto
import com.example.apexhr.features.shop.data.dto.OrderItemDto
import com.example.apexhr.features.shop.domain.CartItem
import com.example.apexhr.features.shop.domain.Order
import com.example.apexhr.features.shop.domain.OrderItem
import com.example.apexhr.features.shop.domain.OrderStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

fun CartItemDto.toDomain(): CartItem = CartItem(
    id = id,
    productId = productId,
    name = productName.trimToNull(),
    imageUrl = imageUrl.trimToNull(),
    unitPrice = unitPrice,
    quantity = quantity,
    total = total ?: (unitPrice * quantity),
)

fun OrderDto.toDomain(): Order {
    val orderItems = items.map { it.toDomain() }
    return Order(
        id = id,
        status = status.toOrderStatus(),
        rawStatus = status.trimToNull(),
        totalAmount = totalAmount ?: orderItems.sumOf { it.price * it.quantity },
        createdAt = createdAt.toInstantOrNull(),
        items = orderItems,
    )
}

fun OrderItemDto.toDomain(): OrderItem = OrderItem(
    id = id,
    productId = productId,
    name = productName.trimToNull(),
    quantity = quantity,
    price = priceAtPurchase,
)

fun String?.toOrderStatus(): OrderStatus =
    when (this?.trim()?.lowercase()) {
        "pending", "new", "created" -> OrderStatus.Pending
        "paid" -> OrderStatus.Paid
        "processing", "inprogress", "in_progress" -> OrderStatus.Processing
        "shipped", "sent", "delivering" -> OrderStatus.Shipped
        "completed", "delivered", "done", "issued" -> OrderStatus.Completed
        "cancelled", "canceled" -> OrderStatus.Cancelled
        "refunded" -> OrderStatus.Refunded
        else -> OrderStatus.Unknown
    }

/** Даты сервера в UTC вида «2026-10-25T16:51:01.452529Z»; на случай без зоны считаем UTC. */
fun String?.toInstantOrNull(): Instant? {
    val value = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return parseOrNull { Instant.parse(value) }
        ?: parseOrNull { OffsetDateTime.parse(value).toInstant() }
        ?: parseOrNull { LocalDateTime.parse(value).toInstant(ZoneOffset.UTC) }
}

private inline fun parseOrNull(parse: () -> Instant): Instant? =
    try {
        parse()
    } catch (e: DateTimeParseException) {
        null
    }

private fun String?.trimToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
