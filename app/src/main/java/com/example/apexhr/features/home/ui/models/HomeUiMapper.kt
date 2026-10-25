package com.example.apexhr.features.home.ui.models

import com.example.apexhr.core.ui.model.formatAmount
import com.example.apexhr.features.home.domain.HomeData

fun HomeData.toUiState(): HomeUiState = HomeUiState(
    // В приветствии только имя; если его нет, UI подставит запасной текст.
    userName = user.firstName.orEmpty(),
    points = formatAmount(balance),
    challenges = quests.map { quest ->
        ChallengeUiModel(
            id = quest.id.toString(),
            title = quest.title.orEmpty(),
            prizePoints = quest.rewardAmount,
        )
    },
    leaderboard = leaderboard.mapIndexed { index, entry ->
        LeaderboardEntryUiModel(
            // userId может не прийти — тогда ключом служит позиция.
            id = entry.userId ?: "rank_${entry.rank}_$index",
            rank = entry.rank,
            name = entry.name.orEmpty(),
            points = formatAmount(entry.thanksCount),
        )
    },
    achievements = achievements.map { achievement ->
        AchievementUiModel(
            id = achievement.id.toString(),
            title = achievement.title.orEmpty(),
        )
    },
)
