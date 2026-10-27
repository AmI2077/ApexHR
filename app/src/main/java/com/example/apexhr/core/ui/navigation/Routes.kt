package com.example.apexhr.core.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavKey
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.transparent
import kotlinx.serialization.Serializable

val topLevelRoutes =
    listOf(TopLevelRoute.Home, TopLevelRoute.Activities, TopLevelRoute.Shop, TopLevelRoute.Profile)

interface TopLevelRoute : NavKey {
    @get:DrawableRes
    val iconResId: Int

    @get:StringRes
    val labelResId: Int

    @Serializable
    data object Home : TopLevelRoute {
        override val iconResId: Int
            get() = R.drawable.ic_home_24
        override val labelResId: Int
            get() = R.string.home
    }

    @Serializable
    data object Profile : TopLevelRoute {
        override val iconResId: Int
            get() = R.drawable.ic_profile_24
        override val labelResId: Int
            get() = R.string.profile
    }

    @Serializable
    data object Activities : TopLevelRoute {
        override val iconResId: Int
            get() = R.drawable.ic_activities_24
        override val labelResId: Int
            get() = R.string.activities
    }

    @Serializable
    data object Shop : TopLevelRoute {
        override val iconResId: Int
            get() = R.drawable.ic_shop_24
        override val labelResId: Int
            get() = R.string.shop
    }
}

/** Корзина каталога: открывается поверх вкладки «Каталог». */
@Serializable
data object CartRoute : NavKey

/** «Мои заказы»: открываются из корзины. */
@Serializable
data object OrdersRoute : NavKey

/** Мероприятие: открывается из списка «Активности». */
@Serializable
data class EventDetailsRoute(val eventId: Long) : NavKey

/** Квест: открывается из списка «Активности» (вкладка «Квесты»). */
@Serializable
data class QuestDetailsRoute(val questId: Long) : NavKey

/** Опрос: открывается из списка «Активности». */
@Serializable
data class SurveyDetailsRoute(val surveyId: Long) : NavKey

/** Отправка благодарности коллеге: открывается из профиля. */
@Serializable
data object ThanksRoute : NavKey

/** Настройки (тёмная тема, выход из аккаунта): открываются из профиля. */
@Serializable
data object SettingsRoute : NavKey

/** Смена пароля: открывается из настроек. */
@Serializable
data object SettingsChangePasswordRoute : NavKey

@Composable
fun TopLevelRoute.toComposableIcon(): @Composable (() -> Unit) {
    return {
        Icon(
            painter = painterResource(this.iconResId),
            contentDescription = null,
        )
    }
}