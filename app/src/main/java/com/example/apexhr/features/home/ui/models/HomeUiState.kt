package com.example.apexhr.features.home.ui.models

data class HomeUiState(
    val userName: String,
    val points: String,
    val challenges: List<ChallengeUiModel>,
    val leaderboard: List<LeaderboardEntryUiModel>,
    val achievements: List<AchievementUiModel>,
) {
    companion object {
        val Preview = HomeUiState(
            userName = "Анна",
            points = "1 250",
            challenges = listOf(
                ChallengeUiModel(
                    id = "streak_7",
                    title = "7 дней активности",
                    prizePoints = 300,
                )
            ),
            leaderboard = listOf(
                LeaderboardEntryUiModel(id = "1", rank = 1, name = "Иван П.", points = "12 456"),
                LeaderboardEntryUiModel(id = "2", rank = 2, name = "Мария С.", points = "10 832"),
                LeaderboardEntryUiModel(id = "3", rank = 3, name = "Алексей К.", points = "9 215"),
            ),
            achievements = listOf(
                AchievementUiModel(id = "steps", title = "10K шагов"),
                AchievementUiModel(id = "activity", title = "Активность"),
                AchievementUiModel(id = "goal", title = "Целеустремлённость"),
            ),
        )
    }
}

data class ChallengeUiModel(
    val id: String,
    val title: String,
    val prizePoints: Int,
)

data class LeaderboardEntryUiModel(
    val id: String,
    val rank: Int,
    val name: String,
    val points: String,
)

data class AchievementUiModel(
    val id: String,
    val title: String,
)
