package com.example.apexhr.features.shop.data

import com.example.apexhr.features.shop.data.dto.CategoryDto
import com.example.apexhr.features.shop.data.dto.ProductDto
import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.Product
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopMappersTest {

    // Те же настройки, что в networkModule.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `categories json is parsed and mapped`() {
        val categories = json.decodeFromString<List<CategoryDto>>(
            """[{"id":1,"name":"Мерч","sortOrder":2},{"id":2,"name":" ","sortOrder":1}]"""
        )

        assertEquals(
            listOf(Category(id = 1, name = "Мерч"), Category(id = 2, name = null)),
            categories.map { it.toDomain() },
        )
    }

    @Test
    fun `product json is parsed and mapped`() {
        val product = json.decodeFromString<List<ProductDto>>(
            """
            [{"id":1,"categoryId":3,"name":"Худи","description":"Тёмно-зелёное","price":1990,
              "imageUrl":"https://example.com/hoodie.png","stock":6,"isDigital":false,
              "createdAt":"2026-10-20T23:57:54.753958Z","updatedAt":"2026-10-21T22:10:00.925169Z"}]
            """.trimIndent()
        ).single().toDomain()

        assertEquals(
            Product(
                id = 1,
                categoryId = 3,
                name = "Худи",
                description = "Тёмно-зелёное",
                price = 1990,
                imageUrl = "https://example.com/hoodie.png",
                stock = 6,
            ),
            product,
        )
    }

    @Test
    fun `missing optional fields get defaults`() {
        val product = json.decodeFromString<ProductDto>("""{"id":7,"price":null}""").toDomain()

        assertEquals(0L, product.price)
        assertNull(product.name)
        assertNull(product.stock)
        assertNull(product.categoryId)
    }

    @Test
    fun `availability depends on stock`() {
        assertTrue(ProductDto(id = 1, stock = null).toDomain().isAvailable)
        assertTrue(ProductDto(id = 1, stock = 1).toDomain().isAvailable)
        assertFalse(ProductDto(id = 1, stock = 0).toDomain().isAvailable)
    }
}
