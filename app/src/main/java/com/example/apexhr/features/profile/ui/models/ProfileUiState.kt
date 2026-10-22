package com.example.apexhr.features.profile.ui.models

enum class ProfileTab {
    Thanks,
    Achievements,
}

data class ProfileUiState(
    val name: String,
    val department: String,
    val balance: String,
    val balanceHint: String,
    val selectedTab: ProfileTab,
    val transactions: List<TransactionUiModel>,
    val achievements: List<ProfileAchievementUiModel>,
) {
    companion object {
        val Preview = ProfileUiState(
            name = "Olivia Martinez",
            department = "Marketing Department",
            balance = "1,250",
            balanceHint = "Great balance! Keep earning rewards.",
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
                ProfileAchievementUiModel(id = "goal", title = "Целеустремлённость"),
            ),
        )
    }
}

data class TransactionUiModel(
    val id: String,
    val title: String,
    val amount: Int,
)

data class ProfileAchievementUiModel(
    val id: String,
    val title: String,
)
