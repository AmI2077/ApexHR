package com.example.apexhr.features.shop.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.PageSlice
import com.example.apexhr.core.ui.model.pageSlice

data class ShopUiState(
    val searchQuery: String,
    val selectedCategoryId: String,
    val categories: List<ShopCategoryUiModel>,
    val products: List<ShopProductUiModel>,
    /** Список товаров перезагружается (смена категории или повтор). */
    val isProductsLoading: Boolean = false,
    /** Ошибка загрузки товаров; категории при этом остаются на экране. */
    val productsError: DataError? = null,
    val sort: ShopSort = ShopSort.Popular,
    /** Страница каталога, как в вебе: по [PAGE_SIZE] товаров; сбрасывается при смене фильтра. */
    val page: Int = 1,
) {
    /** Товары текущей страницы. */
    val pageSlice: PageSlice<ShopProductUiModel> get() = products.pageSlice(page, PAGE_SIZE)

    companion object {
        /** Товаров на странице каталога, как в вебе. */
        const val PAGE_SIZE = 6

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
                    price = 1_990,
                    category = "Мерч",
                    stock = 12,
                ),
                ShopProductUiModel(
                    id = "certificate",
                    title = "Сертификат Supabase",
                    subtitle = "На 3 000 ₽",
                    price = 3_000,
                    category = "Сертификаты",
                    stock = 0,
                    isAvailable = false,
                ),
                ShopProductUiModel(
                    id = "hike",
                    title = "Поход выходного дня",
                    subtitle = "Подмосковье, 1 день. Сбор у офиса в 9:00, вернёмся к вечеру",
                    price = 1_500,
                    showMark = true,
                    category = "Опыт",
                    stock = null,
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
    /** Цена в апексах. */
    val price: Long,
    /** Название категории; null — категория неизвестна или без названия (строка не показывается). */
    val category: String? = null,
    /** Остаток на складе; null — сервер не ограничивает количество («В наличии» без числа). */
    val stock: Int? = null,
    /** Показывать значок-заглушку вместо картинки. */
    val showMark: Boolean = false,
    /** http(s)-ссылка на картинку; null — показывается заглушка. */
    val imageUrl: String? = null,
    /** false, если товар закончился: кнопка «В корзину» неактивна. */
    val isAvailable: Boolean = true,
    /** Цена без скидки (зачёркнутая); null — скидки нет. */
    val originalPrice: Long? = null,
    val discountPercent: Int? = null,
    val badges: List<ShopBadgeUiModel> = emptyList(),
    /** Товар добавляется в корзину: на кнопке индикатор, повторно нажать нельзя. */
    val isAddingToCart: Boolean = false,
)

/** Бейдж товара; [colorArgb] — цвет пилюли. */
data class ShopBadgeUiModel(
    val name: String,
    val colorArgb: Long,
)

/** Сортировка каталога, как в вебе: «популярные» — порядок сервера. */
enum class ShopSort(@StringRes val labelRes: Int) {
    Popular(R.string.shop_sort_popular),
    Cheaper(R.string.shop_sort_cheaper),
    Expensive(R.string.shop_sort_expensive),
}

/** Состояние экрана каталога целиком. */
sealed interface ShopScreenState {
    data object Loading : ShopScreenState
    data class Error(val error: DataError) : ShopScreenState
    data class Content(val uiState: ShopUiState) : ShopScreenState
}
