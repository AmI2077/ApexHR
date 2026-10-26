package com.example.apexhr.features.home.ui.models

import com.example.apexhr.core.domain.DataError

data class HomeUiState(
    val userName: String,
    /** Баланс в апексах. */
    val balance: Long,
    val challenges: List<ChallengeUiModel>,
    val leaderboard: List<LeaderboardEntryUiModel>,
    val achievements: List<AchievementUiModel>,
) {
    companion object {
        val Preview = HomeUiState(
            userName = "Анна",
            balance = 1_250,
            challenges = listOf(
                ChallengeUiModel(
                    id = "streak_7",
                    title = "7 дней активности",
                    prizeAmount = 300,
                )
            ),
            leaderboard = listOf(
                LeaderboardEntryUiModel(id = "1", rank = 1, name = "Иван П.", thanksCount = "12 456"),
                LeaderboardEntryUiModel(id = "2", rank = 2, name = "Мария С.", thanksCount = "10 832"),
                LeaderboardEntryUiModel(id = "3", rank = 3, name = "Алексей К.", thanksCount = "9 215"),
                LeaderboardEntryUiModel(id = "4", rank = 4, name = "Ольга Н.", thanksCount = "5"),
                LeaderboardEntryUiModel(id = "5", rank = 5, name = "Пётр В.", thanksCount = "1"),
                LeaderboardEntryUiModel(id = "6", rank = 6, name = "Светлана Д.", thanksCount = "0"),
                LeaderboardEntryUiModel(id = "7", rank = 7, name = "Юрий Т.", thanksCount = "0"),
            ),
            achievements = listOf(
                AchievementUiModel(id = "1", title = "Первая благодарность", description = null, isEarned = true),
                AchievementUiModel(id = "2", title = "Активность", description = null, isEarned = true),
                AchievementUiModel(
                    id = "3",
                    title = "Целеустремлённость",
                    description = "Завершите 5 квестов",
                    isEarned = false,
                ),
            ),
        )
    }
}

data class ChallengeUiModel(
    val id: String,
    val title: String,
    /** Приз за вызов в апексах. */
    val prizeAmount: Long,
)

/** @param thanksCount число полученных благодарностей (по нему сервер строит рейтинг), уже отформатированное. */
data class LeaderboardEntryUiModel(
    val id: String,
    val rank: Int,
    val name: String,
    val thanksCount: String,
)

/**
 * @param description описание из каталога; показывается у ещё не полученных.
 * @param isEarned получено ли достижение: залитая синяя звезда или светлый контур.
 */
data class AchievementUiModel(
    val id: String,
    val title: String,
    val description: String?,
    val isEarned: Boolean,
)

/** Состояние главной целиком. */
sealed interface HomeScreenState {
    data object Loading : HomeScreenState
    data class Error(val error: DataError) : HomeScreenState
    data class Content(val uiState: HomeUiState) : HomeScreenState
}
