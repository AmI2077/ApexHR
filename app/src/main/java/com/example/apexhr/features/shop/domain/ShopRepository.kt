package com.example.apexhr.features.shop.domain

import com.example.apexhr.core.domain.DataResult

interface ShopRepository {
    /** Категории в порядке sortOrder. */
    suspend fun getCategories(): DataResult<List<Category>>

    /** Активные товары категории в порядке сервера; при [categoryId] == null — весь каталог. */
    suspend fun getProducts(categoryId: Long?): DataResult<List<Product>>

    /**
     * Товар и его активные варианты. Варианты необязательны: если их список
     * не загрузился, карточка показывается без выбора варианта, как в вебе.
     */
    suspend fun getProductDetails(productId: Long): DataResult<ProductDetails>
}