package com.example.apexhr.features.shop.data

import com.example.apexhr.features.shop.data.dto.CategoryDto
import com.example.apexhr.features.shop.data.dto.ProductBadgeDto
import com.example.apexhr.features.shop.data.dto.ProductDto
import com.example.apexhr.features.shop.data.dto.ProductVariantDto
import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.domain.ProductBadge
import com.example.apexhr.features.shop.domain.ProductVariant

/** Фирменный синий веба (#306cc0) — для бейджа без цвета или с неверным цветом. */
const val DEFAULT_BADGE_COLOR: Long = 0xFF306CC0

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name.trimToNull(),
)

fun ProductDto.toDomain(): Product {
    val prices = pricesOf(price, originalPrice, finalPrice)
    return Product(
        id = id,
        categoryId = categoryId,
        name = name.trimToNull(),
        description = description.trimToNull(),
        price = prices.price,
        imageUrl = imageUrl.trimToNull(),
        stock = stock,
        originalPrice = prices.originalPrice,
        discountPercent = discountPercent.takeIf { prices.originalPrice != null && it != null && it > 0 },
        badges = badges.toDomain(),
        isDigital = isDigital,
        isActive = isActive,
    )
}

fun ProductVariantDto.toDomain(): ProductVariant {
    val prices = pricesOf(price, originalPrice, finalPrice)
    return ProductVariant(
        id = id,
        name = name.trimToNull(),
        imageUrl = imageUrl.trimToNull(),
        price = prices.price,
        originalPrice = prices.originalPrice,
        discountPercent = discountPercent.takeIf { prices.originalPrice != null && it != null && it > 0 },
        stock = stock,
        badges = badges.toDomain(),
    )
}

private class Prices(val price: Long, val originalPrice: Long?)

/** Как в вебе: к оплате finalPrice (или прайс), зачёркнутая цена — только если она больше. */
private fun pricesOf(price: Long, originalPrice: Long?, finalPrice: Long?): Prices {
    val list = originalPrice ?: price
    val final = finalPrice?.takeIf { it > 0 } ?: list
    return Prices(price = final, originalPrice = list.takeIf { it > final })
}

fun List<ProductBadgeDto>?.toDomain(): List<ProductBadge> =
    orEmpty()
        .sortedBy { it.sortOrder }
        .mapNotNull { dto ->
            val name = dto.name.trimToNull() ?: return@mapNotNull null
            ProductBadge(name = name, colorArgb = dto.color.toBadgeColorOrNull() ?: DEFAULT_BADGE_COLOR)
        }
        .distinctBy { it.name }

/** «#RGB» или «#RRGGBB» -> ARGB; иначе null. */
fun String?.toBadgeColorOrNull(): Long? {
    val hex = this?.trim()?.removePrefix("#") ?: return null
    if (!hex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
    val full = when (hex.length) {
        3 -> hex.map { "$it$it" }.joinToString("")
        6 -> hex
        else -> return null
    }
    return 0xFF000000 or full.toLong(16)
}

private fun String?.trimToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }