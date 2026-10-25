package com.example.apexhr.features.shop.domain

import com.example.apexhr.core.domain.DataResult

interface CartRepository {
    /** Корзина и баланс пользователя, запрашиваются параллельно. */
    suspend fun loadCart(): DataResult<CartSummary>

    /** Добавляет товар; если он уже в корзине, сервер увеличивает количество. */
    suspend fun addToCart(productId: Long, quantity: Int = 1): CartActionResult

    /** Новое количество позиции, больше 0. */
    suspend fun updateQuantity(itemId: Long, quantity: Int): CartActionResult

    suspend fun removeItem(itemId: Long): CartActionResult

    /**
     * Оформляет заказ из всей корзины. Успех определяется по коду ответа,
     * тело нужно только для номера заказа и может не разобраться.
     */
    suspend fun checkout(): CheckoutResult

    /** Заказы пользователя, новые сверху. */
    suspend fun getOrders(): DataResult<List<Order>>
}
