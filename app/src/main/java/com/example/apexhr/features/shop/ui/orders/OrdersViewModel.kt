package com.example.apexhr.features.shop.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.ui.orders.models.OrdersScreenCallbacks
import com.example.apexhr.features.shop.ui.orders.models.OrdersScreenState
import com.example.apexhr.features.shop.ui.orders.models.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** «Мои заказы»: список заказов, новые сверху. «Назад» задаёт навигация. */
class OrdersViewModel(
    private val repository: CartRepository,
) : ViewModel(), OrdersScreenCallbacks {

    private val _state = MutableStateFlow<OrdersScreenState>(OrdersScreenState.Loading)
    val state: StateFlow<OrdersScreenState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = OrdersScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = repository.getOrders()) {
                is DataResult.Success -> OrdersScreenState.Content(result.data.map { it.toUiModel() })
                is DataResult.Failure -> OrdersScreenState.Error(result.error)
            }
        }
    }

    override fun onBackClick() = Unit

    override fun onRetryClick() = load()
}
