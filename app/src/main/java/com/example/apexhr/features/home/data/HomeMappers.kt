package com.example.apexhr.features.home.data

import com.example.apexhr.features.home.data.dto.LeaderboardEntryDto
import com.example.apexhr.features.home.data.dto.QuestListItemDto
import com.example.apexhr.features.home.domain.LeaderboardEntry
import com.example.apexhr.features.home.domain.Quest

fun QuestListItemDto.toDomain() = Quest(
    id = id,
    title = title.cleaned(),
    rewardAmount = rewardAmount,
)

fun LeaderboardEntryDto.toDomain() = LeaderboardEntry(
    rank = rank,
    userId = userId.cleaned(),
    name = name.cleaned(),
    thanksCount = thanksCount,
)

/**
 * Квест показывается в «Активных вызовах», если пользователь его ещё не
 * завершил и не отменил. Сервер и так отдаёт только квесты в пределах дат.
 */
internal fun QuestListItemDto.isActiveForUser(): Boolean =
    participationStatus !in FINISHED_STATUSES

private val FINISHED_STATUSES = setOf("completed", "cancelled")

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
