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

    fun getTopLevelRoute(): TopLevelRoute = currentTopLevelRoute

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

    /**
     * true, когда открыт корень вкладки, отличной от стартовой. NavDisplay в этом
     * состоянии системный «Назад» не обрабатывает (у него обработчик активен только
     * при стеке > 1), поэтому его перехватывает отдельный BackHandler и возвращает
     * на стартовую вкладку. На корне стартовой вкладки «Назад» никто не перехватывает,
     * и система закрывает приложение.
     */
    val canGoBackToStartRoute: Boolean
        get() = currentTopLevelRoute != startRoute && backStack.size == 1

    fun goBack() {
        val stack = backStack

        if (stack.size > 1) {
            stack.removeLastOrNull()
        } else if (currentTopLevelRoute != startRoute) {
            currentTopLevelRoute = startRoute
        }
    }

    private fun add(route: NavKey) {
        topLevelBackStacks[currentTopLevelRoute]?.add(route)
            ?: error("No backstack for $currentTopLevelRoute")
    }

    private fun switchTopLevelRoute(route: TopLevelRoute) {
        val isExist = topLevelBackStacks[route] != null

        if (!isExist) {
            topLevelBackStacks[route] = mutableStateListOf(route)
        }

        currentTopLevelRoute = route
    }
}