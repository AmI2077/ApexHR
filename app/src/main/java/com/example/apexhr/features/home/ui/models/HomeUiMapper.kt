package com.example.apexhr.features.home.ui.models

import com.example.apexhr.core.ui.model.formatAmount
import com.example.apexhr.features.engagement.ui.toUiModel
import com.example.apexhr.features.news.ui.models.toUiModel
import com.example.apexhr.features.home.domain.HomeData
import com.example.apexhr.features.home.domain.LeaderboardEntry

/** Сколько первых мест видно в карточке лидерборда без прокрутки (как топ-5 в вебе). */
const val LEADERBOARD_TOP_SIZE = 5

/** В ленте благодарностей на главной — пять последних записей, как в вебе. */
const val HOME_FEED_SIZE = 5

private fun LeaderboardEntry.isUser(userId: String): Boolean = this.userId.equals(userId, ignoreCase = true)

/**
 * Как в вебе: если меня нет в топе, под списком отдельная строка с моим местом.
 * Если меня нет и в рейтинге вообще — место после последнего, ноль благодарностей.
 */
internal fun HomeData.myLeaderboardEntry(): LeaderboardEntryUiModel? {
    val index = leaderboard.indexOfFirst { it.isUser(user.id) }
    if (index in 0 until LEADERBOARD_TOP_SIZE) return null
    val entry = leaderboard.getOrNull(index)
    return LeaderboardEntryUiModel(
        id = user.id,
        rank = entry?.rank ?: (leaderboard.size + 1),
        name = entry?.name ?: listOfNotNull(user.firstName, user.lastName).joinToString(" "),
        thanksCount = formatAmount(entry?.thanksCount ?: 0),
        isMine = true,
        activityLevel = activityLevel?.title,
    )
}

fun HomeData.toUiState(): HomeUiState = HomeUiState(
    // В приветствии только имя; если его нет, UI подставит запасной текст.
    userName = user.firstName.orEmpty(),
    balance = balance,
    challenges = quests.map { quest ->
        ChallengeUiModel(
            id = quest.id.toString(),
            title = quest.title.orEmpty(),
            prizeAmount = quest.rewardAmount,
        )
    },
    leaderboard = leaderboard.mapIndexed { index, entry ->
        LeaderboardEntryUiModel(
            // userId может не прийти — тогда ключом служит позиция.
            id = entry.userId ?: "rank_${entry.rank}_$index",
            rank = entry.rank,
            name = entry.name.orEmpty(),
            thanksCount = formatAmount(entry.thanksCount),
            isMine = entry.isUser(user.id),
            activityLevel = activityLevel?.title?.takeIf { entry.isUser(user.id) },
        )
    },
    myLeaderboardEntry = myLeaderboardEntry(),
    activityLevel = activityLevel?.toUiModel(),
    news = news.map { it.toUiModel() },
    feed = feed.take(HOME_FEED_SIZE).map {
        FeedItemUiModel(
            id = it.id,
            fromName = it.fromName,
            toName = it.toName,
            message = it.message,
            amount = it.amount,
            isManagerBonus = it.isManagerBonus,
        )
    },
    achievements = achievements.map { achievement ->
        AchievementUiModel(
            id = achievement.id.toString(),
            title = achievement.title.orEmpty(),
            description = achievement.description,
            isEarned = achievement.isEarned,
        )
    },
)
