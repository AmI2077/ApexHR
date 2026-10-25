package com.example.apexhr.core.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.apexhr.features.activities.ui.ActivitiesScreen
import com.example.apexhr.features.activities.ui.models.ActivitiesScreenCallbacks
import com.example.apexhr.features.activities.ui.models.ActivitiesUiState
import com.example.apexhr.features.home.ui.HomeScreen
import com.example.apexhr.features.home.ui.HomeViewModel
import com.example.apexhr.features.profile.ui.ProfileScreen
import com.example.apexhr.features.profile.ui.ProfileViewModel
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.shop.ui.ShopScreen
import com.example.apexhr.features.shop.ui.ShopViewModel
import com.example.apexhr.features.shop.ui.cart.CartScreen
import com.example.apexhr.features.shop.ui.cart.CartViewModel
import com.example.apexhr.features.shop.ui.cart.models.CartScreenCallbacks
import com.example.apexhr.features.shop.ui.models.ShopScreenCallbacks
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    onLogout: () -> Unit,
) {
    // На корне нестартовой вкладки возвращаемся на стартовую, а не закрываем приложение.
    BackHandler(enabled = navigator.canGoBackToStartRoute) {
        navigator.goBack()
    }

    NavDisplay(
        modifier = modifier,
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        // ViewModel экрана живёт, пока его запись есть в стеке, а не всё время
        // жизни Activity: иначе после выхода и входа под другим пользователем
        // на экране остались бы чужие данные.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<TopLevelRoute.Home> {
                val viewModel: HomeViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                HomeScreen(
                    screenState = screenState,
                    callbacks = viewModel,
                )
            }
            entry<TopLevelRoute.Profile> {
                val viewModel: ProfileViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val profileCallbacks = remember(viewModel, onLogout) {
                    object : ProfileScreenCallbacks by viewModel {
                        override fun onLogoutClick() = onLogout()
                    }
                }
                ProfileScreen(
                    screenState = screenState,
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
                val viewModel: ShopViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val shopCallbacks = remember(viewModel, navigator) {
                    object : ShopScreenCallbacks by viewModel {
                        override fun onCartClick() = navigator.navigate(CartRoute)
                    }
                }
                ShopScreen(
                    screenState = screenState,
                    callbacks = shopCallbacks,
                    messages = viewModel.messages,
                )
            }
            entry<CartRoute> {
                val viewModel: CartViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val cartCallbacks = remember(viewModel, navigator) {
                    object : CartScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                CartScreen(
                    screenState = screenState,
                    callbacks = cartCallbacks,
                    messages = viewModel.messages,
                )
            }
        }
    )
}
