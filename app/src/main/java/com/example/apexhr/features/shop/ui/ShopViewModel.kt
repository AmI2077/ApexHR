package com.example.apexhr.features.shop.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.domain.ShopRepository
import com.example.apexhr.features.shop.ui.models.ShopScreenCallbacks
import com.example.apexhr.features.shop.ui.models.ShopScreenState
import com.example.apexhr.features.shop.ui.models.ShopUiState
import com.example.apexhr.features.shop.ui.models.filterByQuery
import com.example.apexhr.features.shop.ui.models.toCategoryIdOrNull
import com.example.apexhr.features.shop.ui.models.toCategoryTabs
import com.example.apexhr.features.shop.ui.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Каталог: категории грузятся один раз вместе с товарами вкладки «Все»,
 * при смене категории перезагружаются только товары. Поиск — локальный.
 */
class ShopViewModel(
    private val repository: ShopRepository,
) : ViewModel(), ShopScreenCallbacks {

    private val _state = MutableStateFlow<ShopScreenState>(ShopScreenState.Loading)
    val state: StateFlow<ShopScreenState> = _state.asStateFlow()

    /** Товары выбранной категории без учёта поиска. */
    private var loadedProducts: List<Product> = emptyList()
    private var loadJob: Job? = null

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        loadJob?.cancel()
        _state.value = ShopScreenState.Loading
        loadJob = viewModelScope.launch {
            val products = async { repository.getProducts(categoryId = null) }
            val categories = repository.getCategories()

            _state.value = when (categories) {
                is DataResult.Failure -> {
                    products.cancel()
                    ShopScreenState.Error(categories.error)
                }

                is DataResult.Success -> {
                    val initial = ShopUiState(
                        searchQuery = "",
                        selectedCategoryId = ShopUiState.CATEGORY_ALL,
                        categories = categories.data.toCategoryTabs(),
                        products = emptyList(),
                    )
                    ShopScreenState.Content(initial.withProducts(products.await()))
                }
            }
        }
    }

    private fun loadProducts(categoryId: String) {
        loadJob?.cancel()
        updateContent { it.copy(isProductsLoading = true, productsError = null, products = emptyList()) }
        loadJob = viewModelScope.launch {
            val result = repository.getProducts(categoryId.toCategoryIdOrNull())
            updateContent { it.withProducts(result) }
        }
    }

    private fun ShopUiState.withProducts(result: DataResult<List<Product>>): ShopUiState =
        when (result) {
            is DataResult.Success -> {
                loadedProducts = result.data
                copy(
                    products = visibleProducts(searchQuery),
                    isProductsLoading = false,
                    productsError = null,
                )
            }

            is DataResult.Failure -> {
                loadedProducts = emptyList()
                copy(
                    products = emptyList(),
                    isProductsLoading = false,
                    productsError = result.error,
                )
            }
        }

    private fun visibleProducts(query: String) =
        loadedProducts.filterByQuery(query).map { it.toUiModel() }

    private inline fun updateContent(transform: (ShopUiState) -> ShopUiState) {
        _state.update { current ->
            if (current is ShopScreenState.Content) {
                current.copy(uiState = transform(current.uiState))
            } else {
                current
            }
        }
    }

    override fun onRetryClick() {
        val current = _state.value
        if (current is ShopScreenState.Content) {
            loadProducts(current.uiState.selectedCategoryId)
        } else {
            loadCatalog()
        }
    }

    override fun onSearchQueryChange(query: String) {
        updateContent { uiState ->
            uiState.copy(
                searchQuery = query,
                products = if (uiState.isProductsLoading || uiState.productsError != null) {
                    uiState.products
                } else {
                    visibleProducts(query)
                },
            )
        }
    }

    override fun onCategoryClick(categoryId: String) {
        val current = (_state.value as? ShopScreenState.Content)?.uiState ?: return
        if (current.selectedCategoryId == categoryId) return
        updateContent { it.copy(selectedCategoryId = categoryId) }
        loadProducts(categoryId)
    }

    // Корзины, оформления заказа и карточки товара пока нет.
    override fun onCartClick() = Unit
    override fun onProductClick(productId: String) = Unit
    override fun onGetProductClick(productId: String) = Unit
}
