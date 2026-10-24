package com.example.apexhr.features.shop.data.dto

import kotlinx.serialization.Serializable

/** Элемент ответа GET /api/categories. */
@Serializable
data class CategoryDto(
    val id: Long,
    val name: String? = null,
    val sortOrder: Int = 0,
)

/** Элемент ответа GET /api/products. Даты и служебные поля не нужны и пропускаются. */
@Serializable
data class ProductDto(
    val id: Long,
    val categoryId: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Long = 0,
    val imageUrl: String? = null,
    val stock: Int? = null,
    val isDigital: Boolean = false,
)
