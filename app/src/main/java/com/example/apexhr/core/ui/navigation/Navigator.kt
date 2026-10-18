package com.example.apexhr.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

@Composable
fun rememberNavigator(startRoute: TopLevelRoute): Navigator {
    return remember(startRoute) {
        Navigator(startRoute)
    }
}

class Navigator(
    private val startRoute: TopLevelRoute
) {
    val backStack: SnapshotStateList<NavKey>
        get() = topLevelBackStacks[currentTopLevelRoute]
            ?: error("No backstack for $currentTopLevelRoute")

    private var currentTopLevelRoute by mutableStateOf(startRoute)

    private var topLevelBackStacks: MutableMap<TopLevelRoute, SnapshotStateList<NavKey>> =
        mutableMapOf(
            currentTopLevelRoute to mutableStateListOf(startRoute as NavKey)
        )

    fun getCurrentTopLevelRoute(): TopLevelRoute = currentTopLevelRoute

    fun navigate(route: NavKey) {
        val isTopLevelRoute = route is TopLevelRoute

        when (isTopLevelRoute) {
            true -> {
                switchTopLevelRoute(route)
            }

            false -> {
                add(route)
            }
        }
    }

    fun goBack() {
        val size = topLevelBackStacks[currentTopLevelRoute]?.size
            ?: error("No backstack for $currentTopLevelRoute")

        if (size < 2) {
            currentTopLevelRoute = startRoute
        } else {
            topLevelBackStacks[currentTopLevelRoute]?.removeLastOrNull()
        }

    }

    private fun add(route: NavKey) {
        topLevelBackStacks[currentTopLevelRoute]?.add(route)
            ?: error("No backstack for $currentTopLevelRoute")
    }

    private fun switchTopLevelRoute(route: TopLevelRoute) {
        val isExist = topLevelBackStacks[currentTopLevelRoute] != null

        if (isExist) {
            currentTopLevelRoute = route
        } else {
            topLevelBackStacks[route] = mutableStateListOf(route)
        }
    }
}