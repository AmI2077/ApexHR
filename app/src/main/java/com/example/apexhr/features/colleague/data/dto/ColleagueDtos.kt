package com.example.apexhr.features.colleague.data.dto

import kotlinx.serialization.Serializable

/** Ответ GET /api/users/{id} (CurrentUserProfileDto). */
@Serializable
data class ColleagueUserDto(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatarUrl: String? = null,
    val department: String? = null,
    val position: String? = null,
    val isActive: Boolean = true,
    val roles: List<String>? = null,
    val canAccessAdminPanel: Boolean = false,
)

/** Элемент ответа GET /api/leaderboard. */
@Serializable
data class ColleagueLeaderboardEntryDto(
    val userId: String? = null,
    val thanksCount: Long = 0,
)