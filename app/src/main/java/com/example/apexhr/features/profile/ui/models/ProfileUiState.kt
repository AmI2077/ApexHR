package com.example.apexhr.features.profile.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError

enum class ProfileTab {
    Thanks,
    Achievements,
}

data class ProfileUiState(
    val name: String,
    /** «Должность · Отдел» по частям; служебные английские значения переведены. */
    val department: List<UserLabel>,
    val balance: String,
    @StringRes val balanceHintRes: Int,
    val selectedTab: ProfileTab,
    val transactions: List<TransactionUiModel>,
    val achievements: List<ProfileAchievementUiModel>,
    val avatarUrl: String? = null,
) {
    companion object {
        val Preview = ProfileUiState(
            name = "Olivia Martinez",
            department = listOf(
                UserLabel.Resource(R.string.profile_position_employee),
                UserLabel.Text("Marketing Department"),
            ),
            balance = "1,250",
            balanceHintRes = R.string.profile_balance_hint,
            selectedTab = ProfileTab.Thanks,
            transactions = listOf(
                TransactionUiModel(id = "1", title = "Monthly Bonus", amount = 300),
                TransactionUiModel(id = "2", title = "Cafeteria Lunch", amount = -45),
                TransactionUiModel(id = "3", title = "Peer Recognition", amount = 150),
                TransactionUiModel(id = "4", title = "Wellness Workshop", amount = -75),
                TransactionUiModel(id = "5", title = "Referral Bonus", amount = 200),
                TransactionUiModel(id = "6", title = "Gift Card Redemption", amount = -150),
            ),
            achievements = listOf(
                ProfileAchievementUiModel(id = "steps", title = "10K шагов"),
                ProfileAchievementUiModel(id = "activity", title = "Активность"),
                ProfileAchievementUiModel(
                    id = "goal",
                    title = "Целеустремлённость",
                    description = "Выполните 5 активностей за месяц",
                    isEarned = false,
                ),
            ),
        )
    }
}

data class TransactionUiModel(
    val id: String,
    val title: String,
    val amount: Long,
)

data class ProfileAchievementUiModel(
    val id: String,
    val title: String,
    /** Условие получения; показывается только у неполученного достижения. */
    val description: String? = null,
    val isEarned: Boolean = true,
)

/** Состояние экрана профиля с учётом загрузки данных. */
sealed interface ProfileScreenState {
    data object Loading : ProfileScreenState
    data class Error(val error: DataError) : ProfileScreenState
    data class Content(val uiState: ProfileUiState) : ProfileScreenState
}
