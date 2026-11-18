package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.domain.ProductDetails
import com.example.apexhr.features.shop.domain.ShopRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ShopRepositoryImpl(
    private val api: ShopApi,
) : ShopRepository {

    override suspend fun getCategories(): DataResult<List<Category>> = safeApiCall {
        api.getCategories()
            .sortedWith(compareBy({ it.sortOrder }, { it.id }))
            .map { it.toDomain() }
    }

    override suspend fun getProducts(categoryId: Long?): DataResult<List<Product>> = safeApiCall {
        api.getProducts(categoryId)
            .filter { it.isActive }
            .map { it.toDomain() }
    }

    override suspend fun getProductDetails(productId: Long): DataResult<ProductDetails> = safeApiCall {
        coroutineScope {
            val variants = async { safeApiCall { api.getVariants(productId) } }
            val product = api.getProduct(productId).toDomain()
            ProductDetails(
                product = product,
                variants = (variants.await() as? DataResult.Success)?.data.orEmpty()
                    .filter { it.isActive }
                    .map { it.toDomain() },
            )
        }
    }
}