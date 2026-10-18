package com.example.apexhr.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navigator: Navigator,
) {
    NavDisplay(
        modifier = modifier,
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            entry<TopLevelRoute.Home> { }
            entry<TopLevelRoute.Profile> { }
            entry<TopLevelRoute.Activities> { }
            entry<TopLevelRoute.Shop> { }
        }
    )
}
