package com.example.apexhr.features.shop.data.dto

import kotlinx.serialization.Serializable

/** Элемент ответа GET /api/categories. */
@Serializable
data class CategoryDto(
    val id: Long,
    val name: String? = null,
    val sortOrder: Int = 0,
)

/**
 * Элемент ответа GET /api/products и ответ GET /api/products/{id}. Поля сняты с живого ответа:
 * `{"id":7,"categoryId":4,"name":"…","price":200,"originalPrice":200,"finalPrice":180,
 * "discountPercent":10,"imageUrl":"…","stock":3,"isActive":true,"isDigital":false,
 * "badges":[{"id":5,"code":"SALE","name":"Акция","color":"#0EA5E9",…}]}`.
 * Как в вебе: цена к оплате — finalPrice, зачёркнутая — originalPrice (или price).
 */
@Serializable
data class ProductDto(
    val id: Long,
    val categoryId: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Long = 0,
    val originalPrice: Long? = null,
    val finalPrice: Long? = null,
    val discountPercent: Int? = null,
    val imageUrl: String? = null,
    val stock: Int? = null,
    val isActive: Boolean = true,
    val isDigital: Boolean = false,
    val badges: List<ProductBadgeDto>? = null,
)

/** Бейдж товара или варианта («Акция», «Новинка»…); цвет — «#RRGGBB». */
@Serializable
data class ProductBadgeDto(
    val name: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
)

/** Элемент ответа GET /api/products/{id}/variants. */
@Serializable
data class ProductVariantDto(
    val id: Long,
    val name: String? = null,
    val imageUrl: String? = null,
    val price: Long = 0,
    val originalPrice: Long? = null,
    val finalPrice: Long? = null,
    val discountPercent: Int? = null,
    val stock: Int? = null,
    val isActive: Boolean = true,
    val badges: List<ProductBadgeDto>? = null,
)
