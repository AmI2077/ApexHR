package com.example.apexhr.features.shop.ui.models

import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.domain.ProductBadge

/** Вкладки категорий: первой идёт «Все», дальше категории с сервера. */
fun List<Category>.toCategoryTabs(): List<ShopCategoryUiModel> =
    listOf(ShopCategoryUiModel(ShopUiState.CATEGORY_ALL, title = null)) +
        map { ShopCategoryUiModel(id = it.id.toString(), title = it.name) }

/**
 * id категории -> название для подписи на карточке товара.
 * Категории без названия пропускаются: на карточке для них подписи нет.
 */
fun List<Category>.toCategoryNames(): Map<Long, String> =
    mapNotNull { category ->
        category.name?.trim()?.takeIf { it.isNotEmpty() }?.let { category.id to it }
    }.toMap()

/** id вкладки -> параметр categoryId запроса; null для «Все». */
fun String.toCategoryIdOrNull(): Long? =
    if (this == ShopUiState.CATEGORY_ALL) null else toLongOrNull()

/**
 * @param categoryNames названия категорий по id (см. [toCategoryNames]); подпись категории
 * на карточке берётся отсюда по [Product.categoryId].
 */
fun Product.toUiModel(
    categoryNames: Map<Long, String> = emptyMap(),
    isAddingToCart: Boolean = false,
): ShopProductUiModel {
    val image = imageUrl.takeIfHttpUrl()
    return ShopProductUiModel(
        id = id.toString(),
        title = name.orEmpty(),
        subtitle = description.orEmpty(),
        price = price,
        category = categoryId?.let(categoryNames::get),
        stock = stock,
        showMark = image == null,
        imageUrl = image,
        isAvailable = isAvailable,
        isAddingToCart = isAddingToCart,
        originalPrice = originalPrice,
        discountPercent = discountPercent,
        badges = badges.toUiModels(),
    )
}

fun List<ProductBadge>.toUiModels(): List<ShopBadgeUiModel> =
    map { ShopBadgeUiModel(name = it.name, colorArgb = it.colorArgb) }

/** Порядок по выбранной сортировке; при равной цене сохраняется порядок сервера. */
fun List<Product>.sortedBy(sort: ShopSort): List<Product> = when (sort) {
    ShopSort.Popular -> this
    ShopSort.Cheaper -> sortedBy { it.price }
    ShopSort.Expensive -> sortedByDescending { it.price }
}

/**
 * Поиск по названию и описанию без учёта регистра. В API поиска нет,
 * поэтому фильтруем уже загруженные товары выбранной категории.
 */
fun List<Product>.filterByQuery(query: String): List<Product> {
    val needle = query.trim()
    if (needle.isEmpty()) return this
    return filter { product ->
        product.name.orEmpty().contains(needle, ignoreCase = true) ||
            product.description.orEmpty().contains(needle, ignoreCase = true)
    }
}
