package com.example.apexhr.core.ui.navigation

import org.junit.Assert.assertEquals
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
}
