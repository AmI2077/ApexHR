package com.example.apexhr.features.shop.ui

import com.example.apexhr.features.shop.domain.Category
import com.example.apexhr.features.shop.domain.Product
import com.example.apexhr.features.shop.ui.models.ShopCategoryUiModel
import com.example.apexhr.features.shop.ui.models.ShopProductUiModel
import com.example.apexhr.features.shop.ui.models.ShopUiState
import com.example.apexhr.features.shop.ui.models.filterByQuery
import com.example.apexhr.features.shop.ui.models.toCategoryIdOrNull
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

    @Test
    fun `product with image url is mapped`() {
        assertEquals(
            ShopProductUiModel(
                id = "1",
                title = "Худи Supabase",
                subtitle = "Тёмно-зелёное, унисекс",
                price = "1\u00A0990",
                showMark = false,
                imageUrl = "https://example.com/hoodie.png",
                isAvailable = true,
            ),
            hoodie.toUiModel(),
        )
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
