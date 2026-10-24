package com.example.apexhr.features.shop.domain

import com.example.apexhr.core.domain.DataResult

interface ShopRepository {
    /** Категории в порядке sortOrder. */
    suspend fun getCategories(): DataResult<List<Category>>

    /** Товары категории; при [categoryId] == null — весь каталог. */
    suspend fun getProducts(categoryId: Long?): DataResult<List<Product>>
}
