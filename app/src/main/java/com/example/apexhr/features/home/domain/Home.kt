package com.example.apexhr.features.home.domain

import com.example.apexhr.features.engagement.domain.ActivityLevel
import com.example.apexhr.features.news.domain.CompanyNews
import java.time.Instant
import com.example.apexhr.features.profile.domain.UserProfile

data class HomeData(
    val user: UserProfile,
    val balance: Long,
    val quests: List<Quest>,
    val leaderboard: List<LeaderboardEntry>,
    val achievements: List<HomeAchievement>,
    /** Мой уровень активности для строки лидерборда; null — не загрузился. */
    val activityLevel: ActivityLevel? = null,
    /** Последние новости компании; пусто — новостей нет или они не загрузились. */
    val news: List<CompanyNews> = emptyList(),
    /** Лента благодарностей с моим участием, новые сверху. */
    val feed: List<FeedItem> = emptyList(),
)

/**
 * Запись ленты благодарностей, как FeedItem в вебе: благодарность (полученная или
 * отправленная) или поощрение руководителя.
 * @param id «thank_<id>» или «bonus_<id транзакции>».
 * @param fromName имя отправителя; null — сотрудник не найден.
 */
data class FeedItem(
    val id: String,
    val fromId: String?,
    val toId: String?,
    val fromName: String?,
    val toName: String?,
    val amount: Long,
    val message: String?,
    val isManagerBonus: Boolean,
    val createdAt: Instant?,
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

/** Достижение из каталога и получено ли оно текущим пользователем. */
data class HomeAchievement(
    val id: Int,
    val title: String?,
    val description: String?,
    val isEarned: Boolean,
)