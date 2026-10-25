package com.example.apexhr.features.shop.domain

import java.time.Instant

data class CartItem(
    /** id позиции корзины (не товара): по нему меняют количество и удаляют. */
    val id: Long,
    val productId: Long,
    val name: String?,
    val imageUrl: String?,
    val unitPrice: Long,
    val quantity: Int,
    val total: Long,
)

data class Cart(
    val items: List<CartItem>,
) {
    val total: Long
        get() = items.sumOf { it.total }
}

/** Корзина вместе с балансом: на экране корзины они нужны одновременно. */
data class CartSummary(
    val cart: Cart,
    val balance: Long,
) {
    val isBalanceEnough: Boolean
        get() = balance >= cart.total

    /** Сколько апексов не хватает на оформление; 0, если хватает. */
    val missingAmount: Long
        get() = (cart.total - balance).coerceAtLeast(0)
}

data class Order(
    val id: Long,
    val status: OrderStatus,
    /** Статус как прислал сервер: показывается, если он неизвестен приложению. */
    val rawStatus: String?,
    val totalAmount: Long,
    val createdAt: Instant?,
    val items: List<OrderItem>,
)

data class OrderItem(
    val id: Long,
    val productId: Long,
    val name: String?,
    val quantity: Int,
    val price: Long,
)

/**
 * Статус заказа. В Swagger перечисления нет, в реальных ответах встречался
 * только «Paid», остальные значения — предположение по названиям.
 */
enum class OrderStatus {
    Pending,
    Paid,
    Processing,
    Shipped,
    Completed,
    Cancelled,
    Refunded,
    Unknown,
}
