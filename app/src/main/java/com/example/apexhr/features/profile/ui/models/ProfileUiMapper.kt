package com.example.apexhr.features.profile.ui.models

import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.UserProfile
import com.example.apexhr.features.thanks.domain.Thank
import com.example.apexhr.features.thanks.domain.ThanksHistory
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val THANK_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun Profile.toUiState(
    selectedTab: ProfileTab,
    thanks: ThanksHistory,
    zone: ZoneId = ZoneId.systemDefault(),
): ProfileUiState = ProfileUiState(
    name = user.displayName(),
    department = user.displayDepartment(),
    activityLevel = activityLevel?.title,
    balance = balance,
    selectedTab = selectedTab,
    received = thanks.received.map { it.toUiModel(zone) },
    sent = thanks.sent.map { it.toUiModel(zone) },
    achievements = achievements.map { achievement ->
        ProfileAchievementUiModel(
            id = achievement.id.toString(),
            title = achievement.title.orEmpty(),
            description = achievement.description,
            // Профиль загружает только полученные достижения пользователя.
            isEarned = true,
        )
    },
    avatarUrl = user.avatarUrl.takeIfHttpUrl(),
)

fun Thank.toUiModel(zone: ZoneId = ZoneId.systemDefault()) = ThankUiModel(
    id = id.toString(),
    name = counterpartName,
    amount = amount,
    message = message,
    date = createdAt?.atZone(zone)?.format(THANK_DATE_FORMAT),
)

/** «Имя Фамилия». Пустая строка, если сервер не прислал ни того, ни другого. */
internal fun UserProfile.displayName(): String =
    listOfNotNull(firstName, lastName).joinToString(" ")

/** «Должность · Отдел»: в API они раздельно, а в UI одна строка. */
internal fun UserProfile.displayDepartment(): List<UserLabel> = userInfoLabels(position, department)
