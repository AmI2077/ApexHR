package com.example.apexhr.features.shop.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.shop.domain.CartActionResult
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.domain.ShopRepository
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.ShopScreenCallbacks
import com.example.apexhr.features.shop.ui.models.ShopScreenState
import com.example.apexhr.features.shop.ui.models.ShopUiState
import com.example.apexhr.features.shop.ui.models.filterByQuery
import com.example.apexhr.features.shop.ui.models.toCategoryIdOrNull
import com.example.apexhr.features.shop.ui.models.toCategoryTabs
import com.example.apexhr.features.shop.ui.models.toShopMessage
import com.example.apexhr.features.shop.ui.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Каталог: категории грузятся один раз вместе с товарами вкладки «Все»,
 * при смене категории перезагружаются только товары. Поиск — локальный.
 * «Получить» добавляет товар в корзину, результат — сообщением в snackbar.
 */
class ShopViewModel(
    private val repository: ShopRepository,
    private val cartRepository: CartRepository,
) : ViewModel(), ShopScreenCallbacks {

    private val _state = MutableStateFlow<ShopScreenState>(ShopScreenState.Loading)
    val state: StateFlow<ShopScreenState> = _state.asStateFlow()

    private val _messages = Channel<ShopMessage>(Channel.BUFFERED)
    val messages: Flow<ShopMessage> = _messages.receiveAsFlow()

    /** Товары выбранной категории без учёта поиска. */
    private var loadedProducts: List<Product> = emptyList()
    private var loadJob: Job? = null

    /** id товаров, которые сейчас добавляются в корзину. */
    private val addingProductIds = mutableSetOf<String>()

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
        loadedProducts.filterByQuery(query).map {
            it.toUiModel(isAddingToCart = it.id.toString() in addingProductIds)
        }

    private fun refreshAddingState() {
        updateContent { uiState ->
            uiState.copy(
                products = uiState.products.map {
                    it.copy(isAddingToCart = it.id in addingProductIds)
                },
            )
        }
    }

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

    override fun onGetProductClick(productId: String) {
        val id = productId.toLongOrNull() ?: return
        if (!addingProductIds.add(productId)) return
        refreshAddingState()

        viewModelScope.launch {
            val message = when (val result = cartRepository.addToCart(id)) {
                CartActionResult.Success -> ShopMessage(R.string.shop_added_to_cart)
                is CartActionResult.Failure -> result.error.toShopMessage()
            }
            addingProductIds.remove(productId)
            refreshAddingState()
            _messages.send(message)
        }
    }

    // Корзины и карточки товара пока нет.
    override fun onCartClick() = Unit
    override fun onProductClick(productId: String) = Unit
}
