package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.shop.data.dto.CategoryDto
import com.example.apexhr.features.shop.data.dto.ProductDto
import com.example.apexhr.features.shop.data.dto.ProductVariantDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException

class ShopRepositoryImplTest {

    private val api = FakeShopApi()
    private val repository = ShopRepositoryImpl(api)

    @Test
    fun `categories are sorted by sortOrder then id`() = runBlocking {
        api.categories = listOf(
            CategoryDto(id = 3, name = "Опыт", sortOrder = 2),
            CategoryDto(id = 2, name = "Сертификаты", sortOrder = 1),
            CategoryDto(id = 1, name = "Мерч", sortOrder = 1),
        )

        val result = repository.getCategories() as DataResult.Success

        assertEquals(listOf(1L, 2L, 3L), result.data.map { it.id })
    }

    @Test
    fun `products are requested for given category`() = runBlocking {
        val result = repository.getProducts(categoryId = 5) as DataResult.Success

        assertEquals(listOf(5L), api.requestedCategoryIds)
        assertEquals(listOf(10L), result.data.map { it.id })
    }

    @Test
    fun `all products are requested without category`() = runBlocking {
        repository.getProducts(categoryId = null)

        assertEquals(listOf<Long?>(null), api.requestedCategoryIds)
    }

    @Test
    fun `product details keep only active variants`() = runBlocking {
        api.variants = listOf(
            ProductVariantDto(id = 1, name = "Красный", price = 80),
            ProductVariantDto(id = 9, name = "Снят", price = 80, isActive = false),
            ProductVariantDto(id = 17, name = "Синее", price = 80, finalPrice = 72, originalPrice = 80, discountPercent = 10),
        )

        val details = (repository.getProductDetails(2) as DataResult.Success).data

        assertEquals("Худи", details.product.name)
        assertEquals(listOf(1L, 17L), details.variants.map { it.id })
        assertEquals(72L, details.variants[1].price)
        assertEquals(80L, details.variants[1].originalPrice)
    }

    @Test
    fun `product details are shown without variants when they fail`() = runBlocking {
        api.variantsError = ApiStatusException(500)

        val details = (repository.getProductDetails(2) as DataResult.Success).data

        assertEquals(emptyList<Long>(), details.variants.map { it.id })
    }

    @Test
    fun `product details fail when product fails`() = runBlocking {
        api.error = ApiStatusException(503)

        assertEquals(DataResult.Failure(DataError.Server), repository.getProductDetails(2))
    }

    @Test
    fun `server error is mapped`() = runBlocking {
        api.error = ApiStatusException(503)

        assertEquals(DataResult.Failure(DataError.Server), repository.getCategories())
    }

    @Test
    fun `timeout is mapped`() = runBlocking {
        api.error = SocketTimeoutException("timeout")

        assertEquals(DataResult.Failure(DataError.Timeout), repository.getProducts(categoryId = null))
    }

    @Test
    fun `401 is mapped to unauthorized`() = runBlocking {
        api.error = ApiStatusException(401)

        assertEquals(DataResult.Failure(DataError.Unauthorized), repository.getProducts(categoryId = 1))
    }
}

private class FakeShopApi : ShopApi {
    var error: Throwable? = null
    var categories: List<CategoryDto> = emptyList()
    val requestedCategoryIds = mutableListOf<Long?>()

    override suspend fun getCategories(): List<CategoryDto> {
        error?.let { throw it }
        return categories
    }

    override suspend fun getProducts(categoryId: Long?): List<ProductDto> {
        error?.let { throw it }
        requestedCategoryIds += categoryId
        return listOf(
            ProductDto(id = 10, categoryId = categoryId, name = "Худи", price = 100),
            ProductDto(id = 11, categoryId = categoryId, name = "Снят с продажи", price = 100, isActive = false),
        )
    }

    var product = ProductDto(id = 2, name = "Худи", price = 80, imageUrl = "https://x/blue.jpg")
    var variants: List<ProductVariantDto> = emptyList()
    var variantsError: Throwable? = null

    override suspend fun getProduct(productId: Long): ProductDto {
        error?.let { throw it }
        return product
    }

    override suspend fun getVariants(productId: Long): List<ProductVariantDto> {
        variantsError?.let { throw it }
        return variants
    }
}
