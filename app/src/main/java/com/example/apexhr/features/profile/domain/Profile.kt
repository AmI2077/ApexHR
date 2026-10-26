package com.example.apexhr.features.profile.domain

data class Profile(
    val user: UserProfile,
    val balance: Long,
    val achievements: List<EarnedAchievement>,
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
