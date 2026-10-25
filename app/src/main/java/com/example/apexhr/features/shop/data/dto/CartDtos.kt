package com.example.apexhr.features.shop.data.dto

import kotlinx.serialization.Serializable

/**
 * Элемент ответа GET /api/cart. В Swagger схемы нет, поля сняты с реального ответа:
 * `[{"id":57,"productId":1,"productName":"…","imageUrl":"","unitPrice":50,
 * "quantity":1,"total":50,"addedAt":"2026-10-25T20:13:00.599349Z"}]`.
 * Пустая корзина — пустой массив.
 */
@Serializable
data class CartItemDto(
    val id: Long,
    val productId: Long = 0,
    val productName: String? = null,
    val imageUrl: String? = null,
    val unitPrice: Long = 0,
    val quantity: Int = 0,
    /** Сумма позиции; если сервер её не прислал, считаем сами. */
    val total: Long? = null,
)

/** Тело POST /api/cart/items (AddCartItemRequest). Повторное добавление увеличивает количество. */
@Serializable
data class AddCartItemRequest(
    val productId: Long,
    val quantity: Int,
)

/** Тело PUT /api/cart/items/{id} (UpdateCartItemRequest). Количество должно быть больше 0. */
@Serializable
data class UpdateCartItemRequest(
    val quantity: Int,
)

/**
 * Элемент ответа GET /api/orders и ответ POST /api/orders/checkout (201).
 * В Swagger схем нет, поля сняты с реальных ответов:
 * - GET /api/orders: `{"id":5,"status":"Paid","totalAmount":50,"createdAt":"…Z",
 *   "updatedAt":"…Z","comment":null,"items":[{"id":5,"productId":1,
 *   "productName":"…","quantity":1,"priceAtPurchase":50}]}`;
 * - checkout: `{"id":6,"status":"Paid","totalAmount":50,"createdAt":"…Z",
 *   "items":[{"productId":1,"quantity":1,"priceAtPurchase":50}],"newAchievement":null}` —
 *   без id и названий позиций, поэтому всё, кроме id заказа, необязательно.
 */
@Serializable
data class OrderDto(
    val id: Long,
    val status: String? = null,
    val totalAmount: Long? = null,
    val createdAt: String? = null,
    val comment: String? = null,
    val items: List<OrderItemDto> = emptyList(),
)

@Serializable
data class OrderItemDto(
    val id: Long = 0,
    val productId: Long = 0,
    val productName: String? = null,
    val quantity: Int = 0,
    val priceAtPurchase: Long = 0,
)
