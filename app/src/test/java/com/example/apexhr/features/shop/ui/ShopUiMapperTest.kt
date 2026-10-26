package com.example.apexhr.features.shop.ui

import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.ui.models.ShopCategoryUiModel
import com.example.apexhr.features.shop.ui.models.ShopProductUiModel
import com.example.apexhr.features.shop.ui.models.ShopUiState
import com.example.apexhr.features.shop.ui.models.filterByQuery
import com.example.apexhr.features.shop.ui.models.toCategoryIdOrNull
import com.example.apexhr.features.shop.ui.models.toCategoryNames
import com.example.apexhr.features.shop.ui.models.toCategoryTabs
import com.example.apexhr.features.shop.ui.models.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShopUiMapperTest {

    private val hoodie = Product(
        id = 1,
        categoryId = 1,
        name = "Худи Supabase",
        description = "Тёмно-зелёное, унисекс",
        price = 1_990,
        imageUrl = "https://example.com/hoodie.png",
        stock = 3,
    )
    private val hike = Product(
        id = 2,
        categoryId = 2,
        name = "Поход",
        description = "Подмосковье, 1 день",
        price = 1_500,
        imageUrl = "Тестовое изображение товара",
        stock = 0,
    )

    @Test
    fun `tabs start with all category`() {
        val tabs = listOf(Category(id = 4, name = "Мерч")).toCategoryTabs()

        assertEquals(
            listOf(
                ShopCategoryUiModel(ShopUiState.CATEGORY_ALL, title = null),
                ShopCategoryUiModel("4", title = "Мерч"),
            ),
            tabs,
        )
    }

    @Test
    fun `tab id is converted to request category id`() {
        assertNull(ShopUiState.CATEGORY_ALL.toCategoryIdOrNull())
        assertEquals(4L, "4".toCategoryIdOrNull())
    }

    private val categoryNames = mapOf(1L to "Мерч", 2L to "Опыт")

    @Test
    fun `product with image url is mapped`() {
        assertEquals(
            ShopProductUiModel(
                id = "1",
                title = "Худи Supabase",
                subtitle = "Тёмно-зелёное, унисекс",
                price = 1_990,
                category = "Мерч",
                stock = 3,
                showMark = false,
                imageUrl = "https://example.com/hoodie.png",
                isAvailable = true,
            ),
            hoodie.toUiModel(categoryNames),
        )
    }

    @Test
    fun `category names skip blank and missing names`() {
        val names = listOf(
            Category(id = 1, name = " Мерч "),
            Category(id = 2, name = null),
            Category(id = 3, name = "  "),
            Category(id = 4, name = "Опыт"),
        ).toCategoryNames()

        assertEquals(mapOf(1L to "Мерч", 4L to "Опыт"), names)
    }

    @Test
    fun `category is null when product category is unknown or absent`() {
        assertNull(hoodie.copy(categoryId = 99).toUiModel(categoryNames).category)
        assertNull(hoodie.copy(categoryId = null).toUiModel(categoryNames).category)
        assertNull(hoodie.toUiModel().category)
    }

    @Test
    fun `stock is passed as is including unlimited`() {
        assertEquals(0, hike.toUiModel().stock)
        val unlimited = hike.copy(stock = null).toUiModel()
        assertNull(unlimited.stock)
        assertEquals(true, unlimited.isAvailable)
    }

    @Test
    fun `product without valid image shows placeholder mark`() {
        val model = hike.toUiModel()

        assertNull(model.imageUrl)
        assertEquals(true, model.showMark)
        assertEquals(false, model.isAvailable)
    }

    @Test
    fun `adding to cart flag is passed to model`() {
        assertEquals(false, hoodie.toUiModel().isAddingToCart)
        assertEquals(true, hoodie.toUiModel(isAddingToCart = true).isAddingToCart)
    }

    @Test
    fun `search matches name and description ignoring case`() {
        val products = listOf(hoodie, hike)

        assertEquals(listOf(hoodie), products.filterByQuery("худи"))
        assertEquals(listOf(hike), products.filterByQuery("  подмосковье "))
        assertEquals(products, products.filterByQuery(" "))
        assertEquals(emptyList<Product>(), products.filterByQuery("кружка"))
    }
}
