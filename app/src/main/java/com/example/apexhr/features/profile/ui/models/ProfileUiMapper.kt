package com.example.apexhr.features.profile.ui.models

import com.example.apexhr.R
import com.example.apexhr.core.ui.model.formatAmount
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.UserProfile

fun Profile.toUiState(selectedTab: ProfileTab): ProfileUiState = ProfileUiState(
    name = user.displayName(),
    department = user.displayDepartment(),
    balance = formatAmount(balance),
    balanceHintRes = if (balance > 0) {
        R.string.profile_balance_hint
    } else {
        R.string.profile_balance_hint_empty
    },
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
        )
    },
    avatarUrl = user.avatarUrl?.takeIf { it.isHttpUrl() },
)

/** «Имя Фамилия». Пустая строка, если сервер не прислал ни того, ни другого. */
internal fun UserProfile.displayName(): String =
    listOfNotNull(firstName, lastName).joinToString(" ")

/** «Должность · Отдел»: в API они раздельно, а в UI одна строка. */
internal fun UserProfile.displayDepartment(): String =
    listOfNotNull(position, department).joinToString(" · ")

internal fun String.isHttpUrl(): Boolean =
    startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true)
