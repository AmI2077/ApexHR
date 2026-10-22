package com.example.apexhr.features.shop.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R

data class ShopUiState(
    val searchQuery: String,
    val selectedCategoryId: String,
    val categories: List<ShopCategoryUiModel>,
    val products: List<ShopProductUiModel>,
) {
    companion object {
        const val CATEGORY_MERCH = "merch"
        const val CATEGORY_CERTIFICATES = "certificates"
        const val CATEGORY_EXPERIENCE = "experience"

        val Preview = ShopUiState(
            searchQuery = "",
            selectedCategoryId = CATEGORY_MERCH,
            categories = listOf(
                ShopCategoryUiModel(CATEGORY_MERCH, R.string.shop_category_merch),
                ShopCategoryUiModel(CATEGORY_CERTIFICATES, R.string.shop_category_certificates),
                ShopCategoryUiModel(CATEGORY_EXPERIENCE, R.string.shop_category_experience),
            ),
            products = listOf(
                ShopProductUiModel(
                    id = "hoodie",
                    title = "Худи Supabase",
                    subtitle = "Тёмно-зелёное, унисекс",
                    price = "1990",
                ),
                ShopProductUiModel(
                    id = "certificate",
                    title = "Сертификат Supabase",
                    subtitle = "На 3 000 ₽",
                    price = "3 000",
                ),
                ShopProductUiModel(
                    id = "hike",
                    title = "Поход выходного дня",
                    subtitle = "Подмосковье, 1 день",
                    price = "1 500",
                    showMark = true,
                ),
            ),
        )
    }
}

data class ShopCategoryUiModel(
    val id: String,
    @StringRes val titleRes: Int,
)

data class ShopProductUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val price: String,
    val showMark: Boolean = false,
)
