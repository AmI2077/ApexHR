package com.example.apexhr.features.shop.ui.orders.models

interface OrdersScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()

    /** «Назад» / «Дальше» под списком заказов. */
    fun onPageChange(page: Int) = Unit

    companion object {
        val Empty = object : OrdersScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
        }
    }
}
