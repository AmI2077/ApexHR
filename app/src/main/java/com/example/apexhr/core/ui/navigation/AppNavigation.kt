package com.example.apexhr.core.ui.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import com.example.apexhr.features.activities.ui.my.MyActivityScreen
import com.example.apexhr.features.activities.ui.my.MyActivityViewModel
import com.example.apexhr.features.activities.ui.my.models.MyActivityScreenCallbacks
import com.example.apexhr.features.activities.ui.quest.QuestDetailsScreen
import com.example.apexhr.features.activities.ui.quest.QuestDetailsViewModel
import com.example.apexhr.features.activities.ui.quest.models.QuestDetailsScreenCallbacks
import com.example.apexhr.features.activities.ui.quest.proof.QuestStepProofScreen
import com.example.apexhr.features.activities.ui.quest.proof.QuestStepProofViewModel
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofScreenCallbacks
import com.example.apexhr.features.activities.ui.survey.SurveyDetailsScreen
import com.example.apexhr.features.activities.ui.survey.SurveyDetailsViewModel
import com.example.apexhr.features.activities.ui.survey.models.SurveyDetailsScreenCallbacks
import com.example.apexhr.R
import com.example.apexhr.features.auth.ui.ChangePasswordViewModel
import com.example.apexhr.features.auth.ui.composables.ChangePasswordScreen
import com.example.apexhr.features.auth.ui.model.ChangePasswordUiActions
import com.example.apexhr.features.colleague.ui.ColleagueProfileScreen
import com.example.apexhr.features.colleague.ui.ColleagueProfileViewModel
import com.example.apexhr.features.colleague.ui.models.ColleagueProfileScreenCallbacks
import com.example.apexhr.features.home.ui.HomeScreen
import com.example.apexhr.features.home.ui.HomeViewModel
import com.example.apexhr.features.home.ui.models.HomeScreenCallbacks
import com.example.apexhr.features.news.ui.NewsDetailsScreen
import com.example.apexhr.features.news.ui.NewsDetailsViewModel
import com.example.apexhr.features.news.ui.NewsScreen
import com.example.apexhr.features.news.ui.NewsViewModel
import com.example.apexhr.features.news.ui.models.NewsDetailsScreenCallbacks
import com.example.apexhr.features.news.ui.models.NewsScreenCallbacks
import com.example.apexhr.features.notifications.ui.NotificationsBadgeViewModel
import com.example.apexhr.features.notifications.ui.NotificationsScreen
import com.example.apexhr.features.notifications.ui.NotificationsViewModel
import com.example.apexhr.features.notifications.ui.models.NotificationsScreenCallbacks
import com.example.apexhr.features.profile.ui.ProfileScreen
import com.example.apexhr.features.profile.ui.ProfileViewModel
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.settings.domain.toThemeChoice
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
import com.example.apexhr.features.shop.ui.product.ProductDetailsScreen
import com.example.apexhr.features.shop.ui.product.ProductDetailsViewModel
import com.example.apexhr.features.shop.ui.product.models.ProductDetailsScreenCallbacks
import com.example.apexhr.features.support.ui.SupportListScreen
import com.example.apexhr.features.support.ui.SupportListViewModel
import com.example.apexhr.features.support.ui.SupportNewTicketScreen
import com.example.apexhr.features.support.ui.SupportNewTicketViewModel
import com.example.apexhr.features.support.ui.SupportTicketScreen
import com.example.apexhr.features.support.ui.SupportTicketViewModel
import com.example.apexhr.features.support.ui.models.SupportListScreenCallbacks
import com.example.apexhr.features.support.ui.models.SupportNewTicketScreenCallbacks
import com.example.apexhr.features.support.ui.models.SupportTicketScreenCallbacks
import com.example.apexhr.features.thanks.ui.ThanksScreen
import com.example.apexhr.features.thanks.ui.ThanksViewModel
import com.example.apexhr.features.thanks.ui.models.ThanksScreenCallbacks
import java.util.UUID
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
                        override fun onMyActivityClick() = navigator.navigate(MyActivityRoute)
                        override fun onAllNewsClick() = navigator.navigate(NewsRoute)
                        override fun onNewsClick(newsId: Long) = navigator.navigate(NewsDetailsRoute(newsId))
                        override fun onSendThanksClick() = navigator.navigate(ThanksRoute)

                        // Строка без userId (ключ «rank_…») профиль не открывает.
                        override fun onLeaderboardEntryClick(entryId: String) {
                            if (entryId.isUserId()) navigator.navigate(ColleagueProfileRoute(entryId))
                        }
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
            entry<NewsRoute> {
                val viewModel: NewsViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val newsCallbacks = remember(viewModel, navigator) {
                    object : NewsScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                        override fun onNewsClick(id: Long) = navigator.navigate(NewsDetailsRoute(id))
                    }
                }
                NewsScreen(screenState = screenState, callbacks = newsCallbacks)
            }
            entry<NewsDetailsRoute> { key ->
                val viewModel: NewsDetailsViewModel =
                    koinViewModel(parameters = { parametersOf(key.newsId) })
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val newsDetailsCallbacks = remember(viewModel, navigator) {
                    object : NewsDetailsScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                NewsDetailsScreen(screenState = screenState, callbacks = newsDetailsCallbacks)
            }
            entry<MyActivityRoute> {
                val viewModel: MyActivityViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val myActivityCallbacks = remember(viewModel, navigator) {
                    object : MyActivityScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                        override fun onQuestClick(questId: Long) = navigator.navigate(QuestDetailsRoute(questId))
                        override fun onSurveyClick(surveyId: Long) = navigator.navigate(SurveyDetailsRoute(surveyId))
                        override fun onEventClick(eventId: Long) = navigator.navigate(EventDetailsRoute(eventId))
                        override fun onAllActivitiesClick() = navigator.navigate(TopLevelRoute.Activities)
                    }
                }
                MyActivityScreen(
                    screenState = screenState,
                    callbacks = myActivityCallbacks,
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

                        override fun onSupportClick() = navigator.navigate(SupportRoute)
                    }
                }
                SettingsScreen(
                    themeChoice = savedDarkTheme.toThemeChoice(),
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
            entry<SupportRoute> {
                val viewModel: SupportListViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                // Список обновляется каждый раз, когда экран снова виден
                // (после нового обращения или ответа в переписке).
                LaunchedEffect(viewModel) { viewModel.refresh() }
                val supportCallbacks = remember(viewModel, navigator) {
                    object : SupportListScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                        override fun onNewTicketClick() = navigator.navigate(SupportNewTicketRoute)
                        override fun onTicketClick(id: Long) = navigator.navigate(SupportTicketRoute(id))
                    }
                }
                SupportListScreen(
                    screenState = screenState,
                    callbacks = supportCallbacks,
                )
            }
            entry<SupportNewTicketRoute> {
                val viewModel: SupportNewTicketViewModel = koinViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val context = LocalContext.current
                LaunchedEffect(viewModel) {
                    viewModel.createdTicketId.collect { ticketId ->
                        Toast.makeText(context, R.string.support_ticket_created, Toast.LENGTH_SHORT).show()
                        // Форму закрываем и сразу открываем созданное обращение.
                        navigator.goBack()
                        navigator.navigate(SupportTicketRoute(ticketId))
                    }
                }
                val newTicketCallbacks = remember(viewModel, navigator) {
                    object : SupportNewTicketScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                SupportNewTicketScreen(
                    state = state,
                    callbacks = newTicketCallbacks,
                    messages = viewModel.messages,
                )
            }
            entry<SupportTicketRoute> { key ->
                val viewModel: SupportTicketViewModel =
                    koinViewModel(parameters = { parametersOf(key.ticketId) })
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val ticketCallbacks = remember(viewModel, navigator) {
                    object : SupportTicketScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                SupportTicketScreen(
                    screenState = screenState,
                    callbacks = ticketCallbacks,
                    messages = viewModel.messages,
                )
            }
            entry<ThanksRoute> {
                val viewModel: ThanksViewModel = koinViewModel()
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val thanksCallbacks = remember(viewModel, navigator) {
                    object : ThanksScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                        override fun onColleagueProfileClick(colleagueId: String) =
                            navigator.navigate(ColleagueProfileRoute(colleagueId))
                    }
                }
                ThanksScreen(
                    screenState = screenState,
                    callbacks = thanksCallbacks,
                )
            }
            entry<ColleagueProfileRoute> { key ->
                val viewModel: ColleagueProfileViewModel =
                    koinViewModel(parameters = { parametersOf(key.userId) })
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val colleagueCallbacks = remember(viewModel, navigator) {
                    object : ColleagueProfileScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()

                        // Из «Благодарностей» возвращаемся туда же, а не открываем экран второй раз.
                        override fun onThankClick(colleagueId: String) {
                            val stack = navigator.backStack
                            if (stack.size > 1 && stack[stack.lastIndex - 1] == ThanksRoute) {
                                navigator.goBack()
                            } else {
                                navigator.navigate(ThanksRoute)
                            }
                        }

                        override fun onOpenOwnProfileClick() = navigator.navigate(TopLevelRoute.Profile)
                    }
                }
                ColleagueProfileScreen(
                    screenState = screenState,
                    callbacks = colleagueCallbacks,
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
                        override fun onProofClick(stepId: Long) =
                            navigator.navigate(QuestStepProofRoute(key.questId, stepId))
                    }
                }
                // После возврата с экрана подтверждения шаг должен показать новый статус.
                LaunchedEffect(viewModel) { viewModel.refresh() }
                QuestDetailsScreen(
                    screenState = screenState,
                    callbacks = questCallbacks,
                    messages = viewModel.messages,
                )
            }
            entry<QuestStepProofRoute> { key ->
                val viewModel: QuestStepProofViewModel =
                    koinViewModel(parameters = { parametersOf(key.questId, key.stepId) })
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val context = LocalContext.current
                LaunchedEffect(viewModel) {
                    viewModel.sent.collect {
                        Toast.makeText(context, R.string.quest_proof_sent, Toast.LENGTH_SHORT).show()
                        navigator.goBack()
                    }
                }
                val proofCallbacks = remember(viewModel, navigator) {
                    object : QuestStepProofScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                    }
                }
                QuestStepProofScreen(
                    screenState = screenState,
                    callbacks = proofCallbacks,
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
                        override fun onProductClick(productId: String) {
                            productId.toLongOrNull()?.let { navigator.navigate(ProductDetailsRoute(it)) }
                        }
                    }
                }
                ShopScreen(
                    screenState = screenState,
                    callbacks = shopCallbacks,
                    messages = viewModel.messages,
                )
            }
            entry<ProductDetailsRoute> { key ->
                val viewModel: ProductDetailsViewModel =
                    koinViewModel(parameters = { parametersOf(key.productId) })
                val screenState by viewModel.state.collectAsStateWithLifecycle()
                val productCallbacks = remember(viewModel, navigator) {
                    object : ProductDetailsScreenCallbacks by viewModel {
                        override fun onBackClick() = navigator.goBack()
                        override fun onCartClick() = navigator.navigate(CartRoute)
                    }
                }
                ProductDetailsScreen(
                    screenState = screenState,
                    callbacks = productCallbacks,
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

/** id пользователя — UUID; у строк лидерборда без userId ключ вида «rank_…». */
private fun String.isUserId(): Boolean = runCatching { UUID.fromString(this) }.isSuccess

/** Открывает детали активности из списка. */
private fun Navigator.openActivity(activity: ActivityUiModel) {
    when (activity.kind) {
        ActivityKind.Event -> navigate(EventDetailsRoute(activity.id))
        ActivityKind.Quest -> navigate(QuestDetailsRoute(activity.id))
        ActivityKind.Survey -> navigate(SurveyDetailsRoute(activity.id))
        // У достижения нет отдельного экрана: условие и награда уже на карточке.
        ActivityKind.Achievement -> Unit
    }
}
