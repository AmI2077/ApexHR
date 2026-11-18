package com.example.apexhr.core.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.apexhr.features.activities.ui.survey.SurveyDetailsScreen
import com.example.apexhr.features.activities.ui.survey.SurveyDetailsViewModel
import com.example.apexhr.features.activities.ui.survey.models.SurveyDetailsScreenCallbacks
import com.example.apexhr.R
import com.example.apexhr.features.auth.ui.ChangePasswordViewModel
import com.example.apexhr.features.auth.ui.composables.ChangePasswordScreen
import com.example.apexhr.features.auth.ui.model.ChangePasswordUiActions
import com.example.apexhr.features.home.ui.HomeScreen
import com.example.apexhr.features.home.ui.HomeViewModel
import com.example.apexhr.features.home.ui.models.HomeScreenCallbacks
import com.example.apexhr.features.notifications.ui.NotificationsBadgeViewModel
import com.example.apexhr.features.notifications.ui.NotificationsScreen
import com.example.apexhr.features.notifications.ui.NotificationsViewModel
import com.example.apexhr.features.notifications.ui.models.NotificationsScreenCallbacks
import com.example.apexhr.features.profile.ui.ProfileScreen
import com.example.apexhr.features.profile.ui.ProfileViewModel
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.settings.domain.resolveDarkTheme
import com.example.apexhr.features.settings.ui.SettingsScreen
import com.example.apexhr.features.settings.ui.SettingsViewModel
import com.example.apexhr.features.settings.ui.models.SettingsScreenCallbacks
import com.example.apexhr.features.shop.ui.ShopScreen
import com.example.apexhr.features.shop.ui.ShopViewModel
import com.example.apexhr.features.shop.ui.cart.CartScreen
import com.example.apexhr.features.shop.ui.cart.CartViewModel
import com.example.apexhr.features.shop.ui.cart.models.CartScreenCallbacks
import com.example.apexhr.features.shop.ui.models.ShopScreenCallbacks
import com.example.apexhr.features.shop.ui.orders.OrdersScreen
import com.example.apexhr.features.shop.ui.orders.OrdersViewModel
import com.example.apexhr.features.shop.ui.orders.models.OrdersScreenCallbacks
import com.example.apexhr.features.thanks.ui.ThanksScreen
import com.example.apexhr.features.thanks.ui.ThanksViewModel
import com.example.apexhr.features.thanks.ui.models.ThanksScreenCallbacks
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
                val homeCallbacks = remember(viewModel, navigator) {
                    object : HomeScreenCallbacks by viewModel {
                        // «Активные вызовы» на Главной — это квесты, открываем их экран из Активностей.
                        override fun onChallengeClick(challengeId: String) {
                            challengeId.toLongOrNull()?.let { navigator.navigate(QuestDetailsRoute(it)) }
                        }

                        override fun onNotificationsClick() = navigator.navigate(NotificationsRoute)
                    }
                }
                // Бейдж обновляется каждый раз, когда главная снова на экране.
                val badgeViewModel: NotificationsBadgeViewModel = koinViewModel()
                val unreadNotifications by badgeViewModel.unreadCount.collectAsStateWithLifecycle()
                LaunchedEffect(badgeViewModel) { badgeViewModel.refresh() }
                HomeScreen(
                    screenState = screenState,
                    callbacks = homeCallbacks,
                    unreadNotifications = unreadNotifications,
                )
            }
            entry<NotificationsRoute> {
                val viewModel: NotificationsViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val notificationsCallbacks = remember(viewModel, navigator) {
                    object : NotificationsScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                NotificationsScreen(
                    screenState = screenState,
                    callbacks = notificationsCallbacks,
                    messages = viewModel.messages,
                )
            }
            entry<TopLevelRoute.Profile> {
                val viewModel: ProfileViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val profileCallbacks = remember(viewModel, navigator) {
                    object : ProfileScreenCallbacks by viewModel {
                        override fun onSettingsClick() = navigator.navigate(SettingsRoute)
                        override fun onSendThanksClick() = navigator.navigate(ThanksRoute)
                    }
                }
                ProfileScreen(
                    screenState = screenState,
                    callbacks = profileCallbacks,
                )
            }
            entry<SettingsRoute> {
                val viewModel: SettingsViewModel = koinViewModel()
                val savedDarkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
                val settingsCallbacks = remember(viewModel, navigator, onLogout) {
                    object : SettingsScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                        override fun onLogoutClick() = onLogout()
                        override fun onChangePasswordClick() =
                            navigator.navigate(SettingsChangePasswordRoute)
                    }
                }
                SettingsScreen(
                    darkTheme = resolveDarkTheme(savedDarkTheme, isSystemInDarkTheme()),
                    callbacks = settingsCallbacks,
                )
            }
            entry<SettingsChangePasswordRoute> {
                val viewModel: ChangePasswordViewModel = koinViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current
                LaunchedEffect(viewModel) {
                    viewModel.actions.collect { action ->
                        when (action) {
                            ChangePasswordUiActions.Success -> {
                                Toast.makeText(
                                    context,
                                    R.string.change_password_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigator.goBack()
                            }
                            // Кнопки выхода на этом экране нет.
                            ChangePasswordUiActions.Logout -> Unit
                        }
                    }
                }
                ChangePasswordScreen(
                    state = state,
                    callbacks = viewModel,
                    onBackClick = { navigator.goBack() },
                )
            }
            entry<ThanksRoute> {
                val viewModel: ThanksViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val thanksCallbacks = remember(viewModel, navigator) {
                    object : ThanksScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                ThanksScreen(
                    screenState = screenState,
                    callbacks = thanksCallbacks,
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
            entry<SurveyDetailsRoute> { key ->
                val viewModel: SurveyDetailsViewModel =
                    koinViewModel(parameters = { parametersOf(key.surveyId) })
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val surveyCallbacks = remember(viewModel, navigator) {
                    object : SurveyDetailsScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                SurveyDetailsScreen(
                    screenState = screenState,
                    callbacks = surveyCallbacks,
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
                        // Корзина открывается из каталога, поэтому «В каталог» — шаг назад.
                        override fun onCatalogClick() = navigator.goBack()
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

/** Открывает детали активности из списка. */
private fun Navigator.openActivity(activity: ActivityUiModel) {
    when (activity.kind) {
        ActivityKind.Event -> navigate(EventDetailsRoute(activity.id))
        ActivityKind.Quest -> navigate(QuestDetailsRoute(activity.id))
        ActivityKind.Survey -> navigate(SurveyDetailsRoute(activity.id))
    }
}
