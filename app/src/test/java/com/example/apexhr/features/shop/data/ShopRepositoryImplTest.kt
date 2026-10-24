package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.shop.data.dto.CategoryDto
import com.example.apexhr.features.shop.data.dto.ProductDto
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
        return listOf(ProductDto(id = 10, categoryId = categoryId, name = "Худи", price = 100))
    }
}
