package com.example.apexhr.features.shop.ui.cart.models

interface CartScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()
    fun onIncreaseClick(itemId: Long)
    fun onDecreaseClick(itemId: Long)
    fun onRemoveClick(itemId: Long)
    fun onCheckoutClick()
    fun onOrdersClick()

    /** «В каталог» в пустой корзине. */
    fun onCatalogClick()

    companion object {
        val Empty = object : CartScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onIncreaseClick(itemId: Long) = Unit
            override fun onDecreaseClick(itemId: Long) = Unit
            override fun onRemoveClick(itemId: Long) = Unit
            override fun onCheckoutClick() = Unit
            override fun onOrdersClick() = Unit
            override fun onCatalogClick() = Unit
        }
    }
}
