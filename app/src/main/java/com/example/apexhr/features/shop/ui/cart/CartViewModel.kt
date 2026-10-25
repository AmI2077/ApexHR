package com.example.apexhr.features.shop.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.shop.domain.CartActionResult
import com.example.apexhr.features.shop.domain.CartItem
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.domain.CartSummary
import com.example.apexhr.features.shop.ui.cart.models.CartScreenCallbacks
import com.example.apexhr.features.shop.ui.cart.models.CartScreenState
import com.example.apexhr.features.shop.ui.cart.models.toShopMessage
import com.example.apexhr.features.shop.ui.cart.models.toUiState
import com.example.apexhr.features.shop.ui.models.ShopMessage
import com.example.apexhr.features.shop.ui.models.toShopMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Корзина с балансом и оформление заказа. После каждого изменения и оформления
 * корзина перечитывается с сервера без полноэкранного лоадера: суммы, остатки
 * и списание считает сервер.
 * Переход «Назад» задаёт навигация (AppNavigation).
 */
class CartViewModel(
    private val repository: CartRepository,
) : ViewModel(), CartScreenCallbacks {

    private val _state = MutableStateFlow<CartScreenState>(CartScreenState.Loading)
    val state: StateFlow<CartScreenState> = _state.asStateFlow()

    private val _messages = Channel<ShopMessage>(Channel.BUFFERED)
    val messages: Flow<ShopMessage> = _messages.receiveAsFlow()

    /** Последние загруженные корзина и баланс. */
    private var summary: CartSummary? = null
    private var loadJob: Job? = null

    /** Позиции, по которым сейчас идёт запрос. */
    private val updatingItemIds = mutableSetOf<Long>()
    private var isCheckoutInProgress = false

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = CartScreenState.Loading
        loadJob = viewModelScope.launch {
            when (val result = repository.loadCart()) {
                is DataResult.Success -> {
                    summary = result.data
                    render()
                }

                is DataResult.Failure -> _state.value = CartScreenState.Error(result.error)
            }
        }
    }

    private fun render() {
        val current = summary ?: return
        _state.value = CartScreenState.Content(
            current.toUiState(updatingItemIds, isCheckoutInProgress),
        )
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()

    override fun onIncreaseClick(itemId: Long) = changeItem(itemId) { item ->
        repository.updateQuantity(item.id, item.quantity + 1)
    }

    override fun onDecreaseClick(itemId: Long) = changeItem(itemId) { item ->
        // Количество 0 сервер не принимает: последнюю штуку только удаляют.
        if (item.quantity > 1) repository.updateQuantity(item.id, item.quantity - 1) else null
    }

    override fun onRemoveClick(itemId: Long) = changeItem(itemId) { item ->
        repository.removeItem(item.id)
    }

    /** @param action запрос к серверу; null — действие недоступно, ничего не делаем. */
    private fun changeItem(itemId: Long, action: suspend (CartItem) -> CartActionResult?) {
        if (isCheckoutInProgress) return
        val item = summary?.cart?.items?.firstOrNull { it.id == itemId } ?: return
        if (!updatingItemIds.add(itemId)) return
        render()

        viewModelScope.launch {
            val result = action(item)
            if (result is CartActionResult.Failure) {
                _messages.send(result.error.toShopMessage())
            }
            // Перечитываем и после ошибки: позицию могли удалить или изменить остаток.
            val refreshed = if (result != null) repository.loadCart() else null
            updatingItemIds.remove(itemId)
            when (refreshed) {
                is DataResult.Success -> summary = refreshed.data
                is DataResult.Failure -> if (result is CartActionResult.Success) {
                    _messages.send(ShopMessage(refreshed.error.messageRes()))
                }
                null -> Unit
            }
            render()
        }
    }

    override fun onCheckoutClick() {
        val current = (_state.value as? CartScreenState.Content)?.uiState ?: return
        if (!current.canCheckout) return
        isCheckoutInProgress = true
        render()

        viewModelScope.launch {
            val result = repository.checkout()
            _messages.send(result.toShopMessage())
            // И после успеха, и после отказа корзина и баланс на сервере могли измениться.
            val refreshed = repository.loadCart()
            isCheckoutInProgress = false
            if (refreshed is DataResult.Success) summary = refreshed.data
            render()
        }
    }
}
