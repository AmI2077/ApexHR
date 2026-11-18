package com.example.apexhr.features.shop.ui.product.models

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.shop.domain.ProductDetails
import com.example.apexhr.features.shop.ui.models.ShopBadgeUiModel
import com.example.apexhr.features.shop.ui.models.toUiModels

data class ProductVariantUiModel(
    val id: Long,
    val name: String?,
)

/**
 * Карточка товара. Цена, остаток, картинка и бейджи — выбранного варианта, если он есть.
 * @param stock остаток; null — сервер не ограничивает количество.
 */
data class ProductDetailsUiState(
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val price: Long,
    val originalPrice: Long?,
    val discountPercent: Int?,
    val badges: List<ShopBadgeUiModel>,
    val stock: Int?,
    val isAvailable: Boolean,
    val isDigital: Boolean,
    val variants: List<ProductVariantUiModel>,
    val selectedVariantId: Long?,
    val isAddingToCart: Boolean = false,
) {
    val canAddToCart: Boolean
        get() = isAvailable && !isAddingToCart

    companion object {
        val Preview = ProductDetailsUiState(
            title = "Худи «Апекс»",
            description = "Тёплое худи с логотипом компании.",
            imageUrl = null,
            price = 72,
            originalPrice = 80,
            discountPercent = 10,
            badges = listOf(ShopBadgeUiModel("Акция", 0xFF0EA5E9)),
            stock = 7,
            isAvailable = true,
            isDigital = false,
            variants = listOf(ProductVariantUiModel(1, "Красный"), ProductVariantUiModel(17, "Синее")),
            selectedVariantId = 1,
        )
    }
}

sealed interface ProductDetailsScreenState {
    data object Loading : ProductDetailsScreenState
    data class Error(val error: DataError) : ProductDetailsScreenState
    data class Content(val uiState: ProductDetailsUiState) : ProductDetailsScreenState
}

interface ProductDetailsScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()
    fun onVariantClick(variantId: Long)
    fun onAddToCartClick()
    fun onCartClick()

    companion object {
        val Empty = object : ProductDetailsScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onVariantClick(variantId: Long) = Unit
            override fun onAddToCartClick() = Unit
            override fun onCartClick() = Unit
        }
    }
}

/** Как в вебе: сначала вариант с той же картинкой, что у товара, иначе первый. */
fun ProductDetails.defaultVariantId(): Long? {
    val image = product.imageUrl
    return (variants.firstOrNull { image != null && it.imageUrl == image } ?: variants.firstOrNull())?.id
}

fun ProductDetails.toUiState(selectedVariantId: Long?, isAddingToCart: Boolean = false): ProductDetailsUiState {
    val variant = variants.firstOrNull { it.id == selectedVariantId }
    val stock = if (variant != null) variant.stock else product.stock
    return ProductDetailsUiState(
        title = product.name.orEmpty(),
        description = product.description,
        imageUrl = (variant?.imageUrl ?: product.imageUrl).takeIfHttpUrl() ?: product.imageUrl.takeIfHttpUrl(),
        price = variant?.price ?: product.price,
        originalPrice = if (variant != null) variant.originalPrice else product.originalPrice,
        discountPercent = if (variant != null) variant.discountPercent else product.discountPercent,
        badges = (variant?.badges?.takeIf { it.isNotEmpty() } ?: product.badges).toUiModels(),
        stock = stock,
        isAvailable = product.isActive && (stock == null || stock > 0),
        isDigital = product.isDigital,
        variants = variants.map { ProductVariantUiModel(it.id, it.name) },
        selectedVariantId = variant?.id,
        isAddingToCart = isAddingToCart,
    )
}