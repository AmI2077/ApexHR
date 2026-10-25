package com.example.apexhr.features.home.domain

import com.example.apexhr.features.profile.domain.EarnedAchievement
import com.example.apexhr.features.profile.domain.UserProfile

data class HomeData(
    val user: UserProfile,
    val balance: Long,
    val quests: List<Quest>,
    val leaderboard: List<LeaderboardEntry>,
    val achievements: List<EarnedAchievement>,
)

data class Quest(
    val id: Long,
    val title: String?,
    val rewardAmount: Long,
)

/** Место в рейтинге. Рейтинг на сервере считается по числу полученных благодарностей. */
data class LeaderboardEntry(
    val rank: Int,
    val userId: String?,
    val name: String?,
    val thanksCount: Long,
)
