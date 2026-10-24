package com.example.apexhr.features.profile.data

import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import com.example.apexhr.features.profile.domain.BalanceTransaction
import com.example.apexhr.features.profile.domain.EarnedAchievement
import com.example.apexhr.features.profile.domain.UserProfile

fun CurrentUserProfileDto.toDomain() = UserProfile(
    id = id,
    firstName = firstName.cleaned(),
    lastName = lastName.cleaned(),
    middleName = middleName.cleaned(),
    avatarUrl = avatarUrl.cleaned(),
    department = department.cleaned(),
    position = position.cleaned(),
)

fun TransactionDto.toDomain() = BalanceTransaction(
    id = id,
    amount = amount,
    description = description.cleaned(),
)

fun UserAchievementDto.toDomain() = EarnedAchievement(
    id = id,
    title = achievement?.name.cleaned(),
    description = achievement?.description.cleaned(),
    iconUrl = achievement?.iconUrl.cleaned(),
)

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
