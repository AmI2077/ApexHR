package com.example.apexhr.core.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageUrlTest {

    @Test
    fun `http and https urls are kept`() {
        assertEquals("https://cdn.example.com/a.png", "https://cdn.example.com/a.png".takeIfHttpUrl())
        assertEquals("HTTP://example.com/a.png", "HTTP://example.com/a.png".takeIfHttpUrl())
    }

    @Test
    fun `other values are dropped`() {
        assertNull(null.takeIfHttpUrl())
        assertNull("Тестовое изображение товара".takeIfHttpUrl())
        assertNull("/uploads/a.png".takeIfHttpUrl())
    }
}
