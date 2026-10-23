package com.example.apexhr.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.apexhr.features.activities.ui.ActivitiesScreen
import com.example.apexhr.features.activities.ui.models.ActivitiesScreenCallbacks
import com.example.apexhr.features.activities.ui.models.ActivitiesUiState
import com.example.apexhr.features.home.ui.HomeScreen
import com.example.apexhr.features.home.ui.models.HomeScreenCallbacks
import com.example.apexhr.features.home.ui.models.HomeUiState
import com.example.apexhr.features.profile.ui.ProfileScreen
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.profile.ui.models.ProfileUiState
import com.example.apexhr.features.shop.ui.ShopScreen
import com.example.apexhr.features.shop.ui.models.ShopScreenCallbacks
import com.example.apexhr.features.shop.ui.models.ShopUiState

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    onLogout: () -> Unit,
) {
    val profileCallbacks = remember(onLogout) {
        object : ProfileScreenCallbacks by ProfileScreenCallbacks.Empty {
            override fun onLogoutClick() = onLogout()
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            entry<TopLevelRoute.Home> {
                HomeScreen(
                    uiState = HomeUiState.Preview,
                    callbacks = HomeScreenCallbacks.Empty,
                )
            }
            entry<TopLevelRoute.Profile> {
                ProfileScreen(
                    uiState = ProfileUiState.Preview,
                    callbacks = profileCallbacks,
                )
            }
            entry<TopLevelRoute.Activities> {
                ActivitiesScreen(
                    uiState = ActivitiesUiState.Preview,
                    callbacks = ActivitiesScreenCallbacks.Empty,
                )
            }
            entry<TopLevelRoute.Shop> {
                ShopScreen(
                    uiState = ShopUiState.Preview,
                    callbacks = ShopScreenCallbacks.Empty,
                )
            }
        }
    )
}
