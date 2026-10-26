package com.example.apexhr.features.profile.ui.models

import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.UserProfile

fun Profile.toUiState(selectedTab: ProfileTab): ProfileUiState = ProfileUiState(
    name = user.displayName(),
    department = user.displayDepartment(),
    balance = balance,
    selectedTab = selectedTab,
    transactions = transactions.map { transaction ->
        TransactionUiModel(
            id = transaction.id.toString(),
            title = transaction.description.orEmpty(),
            amount = transaction.amount,
        )
    },
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

/** «Имя Фамилия». Пустая строка, если сервер не прислал ни того, ни другого. */
internal fun UserProfile.displayName(): String =
    listOfNotNull(firstName, lastName).joinToString(" ")

/** «Должность · Отдел»: в API они раздельно, а в UI одна строка. */
internal fun UserProfile.displayDepartment(): List<UserLabel> = userInfoLabels(position, department)
