package com.example.apexhr.features.shop.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.testing.JsonRecordingHttpClient
import com.example.apexhr.core.testing.bodyText
import com.example.apexhr.features.shop.data.dto.CartItemDto
import com.example.apexhr.features.shop.data.dto.OrderDto
import com.example.apexhr.features.shop.data.dto.ProductBadgeDto
import com.example.apexhr.features.shop.data.dto.ProductDto
import com.example.apexhr.features.shop.domain.ProductBadge
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Скидки, бейджи и варианты в каталоге; JSON — в виде живых ответов сервера. */
class CatalogParityTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test
    fun `discounted product uses final price and keeps old price`() {
        val product = json.decodeFromString<ProductDto>(
            """{"id":7,"categoryId":4,"name":"Дополнительный выходной","price":200,"originalPrice":200,
              |"finalPrice":180,"discountPercent":10,"stock":3,"isActive":true,"isDigital":false,
              |"badges":[{"id":5,"code":"SALE","name":"Акция","color":"#0EA5E9","sortOrder":5,"productVariantId":null}]}""".trimMargin(),
        ).toDomain()

        assertEquals(180L, product.price)
        assertEquals(200L, product.originalPrice)
        assertEquals(10, product.discountPercent)
        assertEquals(listOf(ProductBadge("Акция", 0xFF0EA5E9)), product.badges)
    }

    @Test
    fun `product without discount has no old price`() {
        val product = ProductDto(id = 2, price = 80, originalPrice = 80, finalPrice = 80, discountPercent = null).toDomain()

        assertEquals(80L, product.price)
        assertNull(product.originalPrice)
        assertNull(product.discountPercent)
    }

    @Test
    fun `badge colors like in web`() {
        assertEquals(0xFF112233, "#123".toBadgeColorOrNull())
        assertEquals(0xFF0EA5E9, "#0ea5e9".toBadgeColorOrNull())
        assertNull("red".toBadgeColorOrNull())
        assertEquals(
            listOf(ProductBadge("Новинка", DEFAULT_BADGE_COLOR), ProductBadge("Акция", 0xFF0EA5E9)),
            listOf(
                ProductBadgeDto(name = "Акция", color = "#0EA5E9", sortOrder = 5),
                ProductBadgeDto(name = " ", color = "#000"),
                ProductBadgeDto(name = "Новинка", color = "blue", sortOrder = 1),
            ).toDomain(),
        )
    }

    @Test
    fun `product and its variants are requested`() = runBlocking {
        val http = JsonRecordingHttpClient(
            body = """[{"id":1,"productId":2,"name":"Красный","categoryId":1,"imageUrl":"https://x/r.jpg","price":80,
                |"originalPrice":80,"finalPrice":80,"discountPercent":null,"stock":3,"isActive":true,"badges":[]}]""".trimMargin(),
        )

        val variants = KtorShopApi(http.client).getVariants(2)

        assertEquals("${NetworkConfig.BASE_URL}/api/products/2/variants", http.requests.single().url.toString())
        assertEquals("Красный", variants.single().toDomain().name)
        assertEquals(3, variants.single().toDomain().stock)

        val one = JsonRecordingHttpClient(body = """{"id":2,"name":"Худи","price":80}""")
        KtorShopApi(one.client).getProduct(2)
        assertEquals("${NetworkConfig.BASE_URL}/api/products/2", one.requests.single().url.toString())
    }

    @Test
    fun `variant is sent to cart only when chosen`() = runBlocking {
        val http = JsonRecordingHttpClient()
        val api = KtorCartApi(http.client)

        api.addItem(productId = 2, quantity = 1, variantId = 17)
        api.addItem(productId = 6, quantity = 1, variantId = null)

        assertEquals("""{"productId":2,"quantity":1,"variantId":17}""", http.requests[0].bodyText())
        assertEquals("""{"productId":6,"quantity":1}""", http.requests[1].bodyText())
    }

    @Test
    fun `cart and order lines show variant`() {
        val cartItem = CartItemDto(
            id = 1,
            productId = 2,
            productName = "Худи",
            imageUrl = "https://x/product.jpg",
            unitPrice = 80,
            quantity = 1,
            productVariantId = 17,
            variantName = "Синее",
            variantImageUrl = "https://x/blue.jpg",
        ).toDomain()
        assertEquals(17L, cartItem.variantId)
        assertEquals("Синее", cartItem.variantName)
        assertEquals("https://x/blue.jpg", cartItem.imageUrl)

        val order = json.decodeFromString<OrderDto>(
            """{"id":21,"status":"Paid","totalAmount":60,"items":[{"id":27,"productId":6,"productName":"Сертификат",
              |"productVariantId":17,"variantName":"Синее","variantImageUrl":null,"quantity":1,"priceAtPurchase":60}]}""".trimMargin(),
        ).toDomain()
        assertEquals("Синее", order.items.single().variantName)
    }
}