package com.example.apexhr.features.shop.ui.models

import com.example.apexhr.core.ui.model.formatAmount
import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.Product

/** Вкладки категорий: первой идёт «Все», дальше категории с сервера. */
fun List<Category>.toCategoryTabs(): List<ShopCategoryUiModel> =
    listOf(ShopCategoryUiModel(ShopUiState.CATEGORY_ALL, title = null)) +
        map { ShopCategoryUiModel(id = it.id.toString(), title = it.name) }

/** id вкладки -> параметр categoryId запроса; null для «Все». */
fun String.toCategoryIdOrNull(): Long? =
    if (this == ShopUiState.CATEGORY_ALL) null else toLongOrNull()

fun Product.toUiModel(): ShopProductUiModel {
    val image = imageUrl.takeIfHttpUrl()
    return ShopProductUiModel(
        id = id.toString(),
        title = name.orEmpty(),
        subtitle = description.orEmpty(),
        price = formatAmount(price),
        showMark = image == null,
        imageUrl = image,
        isAvailable = isAvailable,
    )
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
