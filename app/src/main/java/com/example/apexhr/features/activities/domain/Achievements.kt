package com.example.apexhr.features.activities.domain

import com.example.apexhr.core.domain.DataResult
import java.time.Instant

/**
 * Достижение из каталога и получено ли оно текущим пользователем.
 * @param conditionType условие получения с сервера (first_order, first_thank, first_activity…).
 * @param earnedAt когда получено; null — ещё не получено (или сервер не прислал дату).
 */
data class Achievement(
    val id: Long,
    val name: String?,
    val description: String?,
    val rewardAmount: Long,
    val conditionType: AchievementCondition,
    val isEarned: Boolean,
    val earnedAt: Instant?,
)

/** Условия, которые знает веб; остальные проверяет сервер. */
enum class AchievementCondition { FirstOrder, FirstThank, FirstActivity, Other }

interface AchievementsRepository {
    /** Активные достижения каталога с отметкой «получено», в порядке каталога. */
    suspend fun getAchievements(): DataResult<List<Achievement>>
}