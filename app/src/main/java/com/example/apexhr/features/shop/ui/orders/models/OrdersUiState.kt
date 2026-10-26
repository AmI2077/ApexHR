package com.example.apexhr.features.shop.ui.orders.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError

data class OrderUiModel(
    val id: Long,
    /** Дата и время создания в часовом поясе устройства; null, если сервер не прислал. */
    val createdAt: String?,
    /** Известный приложению статус; null — показываем [rawStatus] как есть. */
    @StringRes val statusRes: Int?,
    val rawStatus: String?,
    /** Сумма заказа в апексах. */
    val total: Long,
    val items: List<OrderItemUiModel>,
) {
    companion object {
        val Preview = listOf(
            OrderUiModel(
                id = 6,
                createdAt = "25.10.2026 23:16",
                statusRes = R.string.order_status_paid,
                rawStatus = "Paid",
                total = 50,
                items = listOf(OrderItemUiModel(title = "Тестовый товар", quantity = 1, total = 50)),
            ),
            OrderUiModel(
                id = 5,
                createdAt = "20.10.2026 10:05",
                statusRes = null,
                rawStatus = "OnHold",
                total = 4_480,
                items = listOf(
                    OrderItemUiModel(title = "Худи Supabase", quantity = 2, total = 3_980),
                    OrderItemUiModel(title = "Сертификат в книжный", quantity = 1, total = 500),
                ),
            ),
        )
    }
}

data class OrderItemUiModel(
    val title: String,
    val quantity: Int,
    /** Сумма строки (цена на момент покупки × количество) в апексах. */
    val total: Long,
)

sealed interface OrdersScreenState {
    data object Loading : OrdersScreenState
    data class Error(val error: DataError) : OrdersScreenState
    data class Content(val orders: List<OrderUiModel>) : OrdersScreenState
}
