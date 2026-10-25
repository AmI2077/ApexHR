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
import com.example.apexhr.features.activities.ui.ActivitiesViewModel
import com.example.apexhr.features.activities.ui.event.EventDetailsScreen
import com.example.apexhr.features.activities.ui.event.EventDetailsViewModel
import com.example.apexhr.features.activities.ui.event.models.EventDetailsScreenCallbacks
import com.example.apexhr.features.activities.ui.models.ActivitiesScreenCallbacks
import com.example.apexhr.features.activities.ui.models.ActivityKind
import com.example.apexhr.features.activities.ui.models.ActivityUiModel
import com.example.apexhr.features.activities.ui.quest.QuestDetailsScreen
import com.example.apexhr.features.activities.ui.quest.QuestDetailsViewModel
import com.example.apexhr.features.activities.ui.quest.models.QuestDetailsScreenCallbacks
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
import com.example.apexhr.features.shop.ui.orders.OrdersScreen
import com.example.apexhr.features.shop.ui.orders.OrdersViewModel
import com.example.apexhr.features.shop.ui.orders.models.OrdersScreenCallbacks
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
                val viewModel: ActivitiesViewModel = koinViewModel()
                val uiState by viewModel.state.collectAsStateWithLifecycle()
                val activitiesCallbacks = remember(viewModel, navigator) {
                    object : ActivitiesScreenCallbacks by viewModel {
                        override fun onActivityClick(activity: ActivityUiModel) =
                            navigator.openActivity(activity)

                        override fun onParticipateClick(activity: ActivityUiModel) =
                            navigator.openActivity(activity)
                    }
                }
                ActivitiesScreen(
                    uiState = uiState,
                    callbacks = activitiesCallbacks,
                )
            }
            entry<EventDetailsRoute> { key ->
                val viewModel: EventDetailsViewModel =
                    koinViewModel(parameters = { parametersOf(key.eventId) })
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val eventCallbacks = remember(viewModel, navigator) {
                    object : EventDetailsScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                EventDetailsScreen(
                    screenState = screenState,
                    callbacks = eventCallbacks,
                    messages = viewModel.messages,
                )
            }
            entry<QuestDetailsRoute> { key ->
                val viewModel: QuestDetailsViewModel =
                    koinViewModel(parameters = { parametersOf(key.questId) })
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val questCallbacks = remember(viewModel, navigator) {
                    object : QuestDetailsScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                QuestDetailsScreen(
                    screenState = screenState,
                    callbacks = questCallbacks,
                    messages = viewModel.messages,
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
                        override fun onOrdersClick() = navigator.navigate(OrdersRoute)
                    }
                }
                CartScreen(
                    screenState = screenState,
                    callbacks = cartCallbacks,
                    messages = viewModel.messages,
                )
            }
            entry<OrdersRoute> {
                val viewModel: OrdersViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val ordersCallbacks = remember(viewModel, navigator) {
                    object : OrdersScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                OrdersScreen(
                    screenState = screenState,
                    callbacks = ordersCallbacks,
                )
            }
        }
    )
}

/** Детали активности из списка; экран анкеты появится следующим шагом. */
private fun Navigator.openActivity(activity: ActivityUiModel) {
    when (activity.kind) {
        ActivityKind.Event -> navigate(EventDetailsRoute(activity.id))
        ActivityKind.Quest -> navigate(QuestDetailsRoute(activity.id))
        ActivityKind.Survey -> Unit
    }
}
