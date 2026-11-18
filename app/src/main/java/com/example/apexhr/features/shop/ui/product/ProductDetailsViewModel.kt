package com.example.apexhr.features.shop.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.shop.domain.CartActionResult
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.domain.ProductDetails
import com.example.apexhr.features.shop.domain.ShopRepository
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.ShopMessageAction
import com.example.apexhr.features.shop.ui.models.toShopMessage
import com.example.apexhr.features.shop.ui.product.models.ProductDetailsScreenCallbacks
import com.example.apexhr.features.shop.ui.product.models.ProductDetailsScreenState
import com.example.apexhr.features.shop.ui.product.models.defaultVariantId
import com.example.apexhr.features.shop.ui.product.models.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Карточка товара: товар с вариантами, выбор варианта и «В корзину»
 * с выбранным вариантом. Результат добавления — сообщением в snackbar.
 */
class ProductDetailsViewModel(
    private val productId: Long,
    private val shopRepository: ShopRepository,
    private val cartRepository: CartRepository,
) : ViewModel(), ProductDetailsScreenCallbacks {

    private val _state = MutableStateFlow<ProductDetailsScreenState>(ProductDetailsScreenState.Loading)
    val state: StateFlow<ProductDetailsScreenState> = _state.asStateFlow()

    private val _messages = Channel<ShopMessage>(Channel.BUFFERED)
    val messages: Flow<ShopMessage> = _messages.receiveAsFlow()

    private var details: ProductDetails? = null
    private var selectedVariantId: Long? = null
    private var isAdding = false
    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = ProductDetailsScreenState.Loading
        loadJob = viewModelScope.launch {
            when (val result = shopRepository.getProductDetails(productId)) {
                is DataResult.Success -> {
                    details = result.data
                    selectedVariantId = result.data.defaultVariantId()
                    publish()
                }

                is DataResult.Failure -> _state.value = ProductDetailsScreenState.Error(result.error)
            }
        }
    }

    private fun publish() {
        val current = details ?: return
        _state.value = ProductDetailsScreenState.Content(current.toUiState(selectedVariantId, isAdding))
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()

    override fun onVariantClick(variantId: Long) {
        if (isAdding || details?.variants?.any { it.id == variantId } != true) return
        selectedVariantId = variantId
        publish()
    }

    override fun onAddToCartClick() {
        val content = (_state.value as? ProductDetailsScreenState.Content)?.uiState ?: return
        if (!content.canAddToCart) return
        isAdding = true
        publish()
        viewModelScope.launch {
            val message = when (val result = cartRepository.addToCart(productId, 1, content.selectedVariantId)) {
                CartActionResult.Success -> ShopMessage(
                    textRes = R.string.shop_added_to_cart,
                    action = ShopMessageAction.OpenCart,
                )

                is CartActionResult.Failure -> result.error.toShopMessage()
            }
            isAdding = false
            publish()
            _messages.send(message)
        }
    }

    /** Переход в корзину задаёт навигация. */
    override fun onCartClick() = Unit
}