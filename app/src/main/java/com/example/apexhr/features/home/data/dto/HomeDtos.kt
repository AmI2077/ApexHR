package com.example.apexhr.features.home.data.dto

import kotlinx.serialization.Serializable

/** Ответ GET /api/quests: страница квестов. Поля пагинации не нужны. */
@Serializable
data class QuestPageDto(
    val items: List<QuestListItemDto> = emptyList(),
)

@Serializable
data class QuestListItemDto(
    val id: Long,
    val title: String? = null,
    val rewardAmount: Long = 0,
    /** Статус участия текущего пользователя: null, in_progress, completed, cancelled. */
    val participationStatus: String? = null,
)

/**
 * Элемент ответа GET /api/users (на сервере это CurrentUserProfileDto).
 * Главной нужны только id, имя и признак активности — для нулевых строк лидерборда.
 */
@Serializable
data class HomeUserDto(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val isActive: Boolean = true,
)

/** Элемент ответа GET /api/leaderboard. */
@Serializable
data class LeaderboardEntryDto(
    val rank: Int = 0,
    val userId: String? = null,
    val name: String? = null,
    val thanksCount: Long = 0,
)
