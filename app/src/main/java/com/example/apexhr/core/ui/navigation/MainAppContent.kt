package com.example.apexhr.core.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MainAppContent(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
) {
    val navigator = rememberNavigator(TopLevelRoute.Home)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavBar(
                selectedRoute = navigator.getTopLevelRoute(),
                onClick = { route ->
                    navigator.navigate(route)
                }
            )
        }
    ) { innerPadding ->
        AppNavigation(
            modifier = Modifier.padding(innerPadding),
            navigator = navigator
        )
    }
}