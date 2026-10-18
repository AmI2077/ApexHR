package com.example.apexhr.core.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    selectedRoute: TopLevelRoute,
    onClick: (route: TopLevelRoute) -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = backgroundColor
    ) {
        topLevelRoutes.forEach { route ->
            val isSelected = route == selectedRoute

            NavigationBarItem(
                selected = isSelected,
                icon = route.toComposableIcon(),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryBlueColor,
                    unselectedIconColor = secondaryBlueColor,
                    indicatorColor = secondaryBlueColor,
                ),
                alwaysShowLabel = false,
                onClick = {
                    onClick(route)
                }
            )
        }
    }
}