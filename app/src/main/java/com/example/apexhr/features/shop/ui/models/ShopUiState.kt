package com.example.apexhr.features.shop.ui.models

import com.example.apexhr.core.domain.DataError

data class ShopUiState(
    val searchQuery: String,
    val selectedCategoryId: String,
    val categories: List<ShopCategoryUiModel>,
    val products: List<ShopProductUiModel>,
    /** Список товаров перезагружается (смена категории или повтор). */
    val isProductsLoading: Boolean = false,
    /** Ошибка загрузки товаров; категории при этом остаются на экране. */
    val productsError: DataError? = null,
) {
    companion object {
        /** Вкладка «Все»: товары без фильтра по категории. */
        const val CATEGORY_ALL = "all"

        const val CATEGORY_MERCH = "merch"
        const val CATEGORY_CERTIFICATES = "certificates"
        const val CATEGORY_EXPERIENCE = "experience"

        val Preview = ShopUiState(
            searchQuery = "",
            selectedCategoryId = CATEGORY_MERCH,
            categories = listOf(
                ShopCategoryUiModel(CATEGORY_ALL, title = null),
                ShopCategoryUiModel(CATEGORY_MERCH, title = "Мерч"),
                ShopCategoryUiModel(CATEGORY_CERTIFICATES, title = "Сертификаты"),
                ShopCategoryUiModel(CATEGORY_EXPERIENCE, title = "Опыт"),
            ),
            products = listOf(
                ShopProductUiModel(
                    id = "hoodie",
                    title = "Худи Supabase",
                    subtitle = "Тёмно-зелёное, унисекс",
                    price = "1\u00A0990",
                ),
                ShopProductUiModel(
                    id = "certificate",
                    title = "Сертификат Supabase",
                    subtitle = "На 3 000 ₽",
                    price = "3\u00A0000",
                    isAvailable = false,
                ),
                ShopProductUiModel(
                    id = "hike",
                    title = "Поход выходного дня",
                    subtitle = "Подмосковье, 1 день",
                    price = "1\u00A0500",
                    showMark = true,
                ),
            ),
        )
    }
}

/**
 * @param title название с сервера; null — для вкладки «Все» и категорий
 * без названия (тогда UI подставляет текст из ресурсов).
 */
data class ShopCategoryUiModel(
    val id: String,
    val title: String?,
)

data class ShopProductUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val price: String,
    /** Показывать значок-заглушку вместо картинки. */
    val showMark: Boolean = false,
    /** http(s)-ссылка на картинку; null — показывается заглушка. */
    val imageUrl: String? = null,
    /** false, если товар закончился: кнопка «Получить» неактивна. */
    val isAvailable: Boolean = true,
)

/** Состояние экрана каталога целиком. */
sealed interface ShopScreenState {
    data object Loading : ShopScreenState
    data class Error(val error: DataError) : ShopScreenState
    data class Content(val uiState: ShopUiState) : ShopScreenState
}
