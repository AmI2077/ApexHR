package com.example.apexhr.features.shop.domain

data class Category(
    val id: Long,
    val name: String?,
)

data class Product(
    val id: Long,
    val categoryId: Long?,
    val name: String?,
    val description: String?,
    val price: Long,
    val imageUrl: String?,
    /** Остаток на складе; null — сервер не ограничивает количество. */
    val stock: Int?,
) {
    val isAvailable: Boolean
        get() = stock == null || stock > 0
}
