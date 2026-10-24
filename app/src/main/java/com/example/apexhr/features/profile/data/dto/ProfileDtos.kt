package com.example.apexhr.features.profile.data.dto

import kotlinx.serialization.Serializable

/** Ответ GET /api/users/me (CurrentUserProfileDto). */
@Serializable
data class CurrentUserProfileDto(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val avatarUrl: String? = null,
    val department: String? = null,
    val position: String? = null,
)

/** Ответ GET /api/balances/me (BalanceResponse). */
@Serializable
data class BalanceDto(
    val amount: Long = 0,
)

/** Элемент ответа GET /api/balances/me/transactions (TransactionResponse). */
@Serializable
data class TransactionDto(
    val id: Long,
    val amount: Long = 0,
    val description: String? = null,
    val relatedEntityType: String? = null,
    val createdAt: String? = null,
)

/** Элемент ответа GET /api/achievements/users/{userId} (UserAchievementDto). */
@Serializable
data class UserAchievementDto(
    val id: Long,
    val achievementId: Int = 0,
    val earnedAt: String? = null,
    val achievement: AchievementDto? = null,
)

@Serializable
data class AchievementDto(
    val id: Int = 0,
    val name: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
)
