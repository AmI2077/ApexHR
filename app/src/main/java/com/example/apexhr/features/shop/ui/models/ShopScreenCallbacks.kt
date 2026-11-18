package com.example.apexhr.features.shop.ui.models

interface ShopScreenCallbacks {
    fun onCartClick()
    fun onSearchQueryChange(query: String)
    fun onCategoryClick(categoryId: String)
    fun onSortClick(sort: ShopSort)
    fun onProductClick(productId: String)
    fun onGetProductClick(productId: String)
    fun onRetryClick() = Unit

    companion object {
        val Empty = object : ShopScreenCallbacks {
            override fun onCartClick() = Unit
            override fun onSearchQueryChange(query: String) = Unit
            override fun onCategoryClick(categoryId: String) = Unit
            override fun onSortClick(sort: ShopSort) = Unit
            override fun onProductClick(productId: String) = Unit
            override fun onGetProductClick(productId: String) = Unit
        }
    }
}
