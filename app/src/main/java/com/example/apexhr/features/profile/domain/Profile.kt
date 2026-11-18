package com.example.apexhr.features.profile.domain

import com.example.apexhr.features.engagement.domain.ActivityLevel

/** @param activityLevel уровень активности; null — сервер его не отдал. */
data class Profile(
    val user: UserProfile,
    val balance: Long,
    val achievements: List<EarnedAchievement>,
    val activityLevel: ActivityLevel? = null,
)

data class UserProfile(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val middleName: String?,
    val avatarUrl: String?,
    val department: String?,
    val position: String?,
)

data class EarnedAchievement(
    val id: Long,
    val title: String?,
    val description: String?,
    val iconUrl: String?,
)
