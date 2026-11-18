package com.example.apexhr.features.shop.ui.orders.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.features.shop.domain.Order
import com.example.apexhr.features.shop.domain.OrderItem
import com.example.apexhr.features.shop.domain.OrderStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

/** @param zone часовой пояс, в котором показываем дату (по умолчанию — устройства). */
fun Order.toUiModel(zone: ZoneId = ZoneId.systemDefault()): OrderUiModel =
    OrderUiModel(
        id = id,
        createdAt = createdAt?.atZone(zone)?.format(ORDER_DATE_FORMAT),
        statusRes = status.labelRes(),
        rawStatus = rawStatus?.takeIf { it.isNotBlank() },
        total = totalAmount,
        items = items.map { it.toUiModel() },
    )

fun OrderItem.toUiModel(): OrderItemUiModel =
    OrderItemUiModel(
        title = name.orEmpty(),
        quantity = quantity,
        total = price * quantity,
        variantName = variantName,
    )

@StringRes
fun OrderStatus.labelRes(): Int? = when (this) {
    OrderStatus.Pending -> R.string.order_status_pending
    OrderStatus.Paid -> R.string.order_status_paid
    OrderStatus.Processing -> R.string.order_status_processing
    OrderStatus.Shipped -> R.string.order_status_shipped
    OrderStatus.Completed -> R.string.order_status_completed
    OrderStatus.Cancelled -> R.string.order_status_cancelled
    OrderStatus.Refunded -> R.string.order_status_refunded
    OrderStatus.Unknown -> null
}
