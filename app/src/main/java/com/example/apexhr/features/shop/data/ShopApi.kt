package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.shop.data.dto.CategoryDto
import com.example.apexhr.features.shop.data.dto.ProductDto
import com.example.apexhr.features.shop.data.dto.ProductVariantDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface ShopApi {
    suspend fun getCategories(): List<CategoryDto>
    suspend fun getProducts(categoryId: Long?): List<ProductDto>
    suspend fun getProduct(productId: Long): ProductDto
    suspend fun getVariants(productId: Long): List<ProductVariantDto>
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorShopApi(
    private val client: HttpClient,
) : ShopApi {
    override suspend fun getCategories(): List<CategoryDto> =
        client.get(NetworkConfig.CATEGORIES_URL).requireSuccess().body()

    override suspend fun getProducts(categoryId: Long?): List<ProductDto> =
        client.get(NetworkConfig.PRODUCTS_URL) {
            // Без параметра сервер отдаёт весь каталог.
            categoryId?.let { parameter("categoryId", it) }
        }.requireSuccess().body()

    override suspend fun getProduct(productId: Long): ProductDto =
        client.get(NetworkConfig.productUrl(productId)).requireSuccess().body()

    override suspend fun getVariants(productId: Long): List<ProductVariantDto> =
        client.get(NetworkConfig.productVariantsUrl(productId)).requireSuccess().body()
}
