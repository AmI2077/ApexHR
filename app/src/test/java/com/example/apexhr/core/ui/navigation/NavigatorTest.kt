package com.example.apexhr.core.ui.navigation

import kotlinx.serialization.Serializable
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigatorTest {

    @Test
    fun `first navigation to unvisited tab switches to it`() {
        val navigator = Navigator(TopLevelRoute.Home)

        navigator.navigate(TopLevelRoute.Shop)

        assertEquals(TopLevelRoute.Shop, navigator.getTopLevelRoute())
        assertEquals(listOf(TopLevelRoute.Shop), navigator.backStack.toList())
    }

    @Test
    fun `navigation to visited tab switches back to it`() {
        val navigator = Navigator(TopLevelRoute.Home)

        navigator.navigate(TopLevelRoute.Shop)
        navigator.navigate(TopLevelRoute.Home)

        assertEquals(TopLevelRoute.Home, navigator.getTopLevelRoute())
        assertEquals(listOf(TopLevelRoute.Home), navigator.backStack.toList())
    }

    @Test
    fun `back at root of non-start tab returns to start tab`() {
        val navigator = Navigator(TopLevelRoute.Home)
        navigator.navigate(TopLevelRoute.Profile)

        assertTrue(navigator.canGoBackToStartRoute)

        navigator.goBack()

        assertEquals(TopLevelRoute.Home, navigator.getTopLevelRoute())
        assertFalse(navigator.canGoBackToStartRoute)
    }

    @Test
    fun `back at root of start tab is not intercepted`() {
        val navigator = Navigator(TopLevelRoute.Home)

        assertFalse(navigator.canGoBackToStartRoute)

        navigator.goBack()

        assertEquals(TopLevelRoute.Home, navigator.getTopLevelRoute())
        assertEquals(listOf(TopLevelRoute.Home), navigator.backStack.toList())
    }

    @Test
    fun `back inside tab pops nested screen and keeps tab`() {
        val navigator = Navigator(TopLevelRoute.Home)
        navigator.navigate(TopLevelRoute.Shop)
        navigator.navigate(NestedScreen)

        assertFalse(navigator.canGoBackToStartRoute)

        navigator.goBack()

        assertEquals(TopLevelRoute.Shop, navigator.getTopLevelRoute())
        assertEquals(listOf(TopLevelRoute.Shop), navigator.backStack.toList())
        assertTrue(navigator.canGoBackToStartRoute)
    }

    @Serializable
    private data object NestedScreen : NavKey
}
