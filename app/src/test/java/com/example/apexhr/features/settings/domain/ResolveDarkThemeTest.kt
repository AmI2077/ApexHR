package com.example.apexhr.features.settings.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveDarkThemeTest {

    @Test
    fun `without saved choice follows system`() {
        assertTrue(resolveDarkTheme(savedDarkTheme = null, isSystemInDarkTheme = true))
        assertFalse(resolveDarkTheme(savedDarkTheme = null, isSystemInDarkTheme = false))
    }

    @Test
    fun `saved choice wins over system`() {
        assertTrue(resolveDarkTheme(savedDarkTheme = true, isSystemInDarkTheme = false))
        assertFalse(resolveDarkTheme(savedDarkTheme = false, isSystemInDarkTheme = true))
    }
}
