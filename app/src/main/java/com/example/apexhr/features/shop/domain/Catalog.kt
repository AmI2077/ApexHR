package com.example.apexhr.features.shop.domain

data class Category(
    val id: Long,
    val name: String?,
)

/**
 * @param price цена к оплате (со скидкой, если она есть).
 * @param originalPrice цена без скидки; null — скидки нет.
 */
data class Product(
    val id: Long,
    val categoryId: Long?,
    val name: String?,
    val description: String?,
    val price: Long,
    val imageUrl: String?,
    /** Остаток на складе; null — сервер не ограничивает количество. */
    val stock: Int?,
    val originalPrice: Long? = null,
    val discountPercent: Int? = null,
    val badges: List<ProductBadge> = emptyList(),
    val isDigital: Boolean = false,
    val isActive: Boolean = true,
) {
    val isAvailable: Boolean
        get() = stock == null || stock > 0
}

/** @param colorArgb цвет бейджа (ARGB); при неверном цвете с сервера — фирменный синий, как в вебе. */
data class ProductBadge(
    val name: String,
    val colorArgb: Long,
)

/** Вариант товара (цвет, размер…): своя цена, остаток и картинка. */
data class ProductVariant(
    val id: Long,
    val name: String?,
    val imageUrl: String?,
    val price: Long,
    val originalPrice: Long? = null,
    val discountPercent: Int? = null,
    val stock: Int?,
    val badges: List<ProductBadge> = emptyList(),
) {
    val isAvailable: Boolean
        get() = stock == null || stock > 0
}

/** Карточка товара: сам товар и его активные варианты (может не быть ни одного). */
data class ProductDetails(
    val product: Product,
    val variants: List<ProductVariant>,
)