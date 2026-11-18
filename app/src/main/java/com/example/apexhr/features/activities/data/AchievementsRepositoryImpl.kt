package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.activities.data.dto.AchievementCatalogDto
import com.example.apexhr.features.activities.domain.Achievement
import com.example.apexhr.features.activities.domain.AchievementCondition
import com.example.apexhr.features.activities.domain.AchievementsRepository
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import com.example.apexhr.features.profile.data.jwtSubject
import com.example.apexhr.features.shop.data.toInstantOrNull
import java.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Каталог достижений (GET /api/achievements) и полученные мной
 * (GET /api/achievements/users/{id}); id берётся из токена, а если его там нет —
 * из профиля. Как в вебе, неактивные достижения не показываются.
 */
class AchievementsRepositoryImpl(
    private val api: AchievementsApi,
    private val profileApi: ProfileApi,
    private val tokenStorage: TokenStorage,
) : AchievementsRepository {

    override suspend fun getAchievements(): DataResult<List<Achievement>> = safeApiCall {
        coroutineScope {
            val catalog = async { api.getAchievements() }
            val userId = tokenStorage.getAccessToken()?.let(::jwtSubject) ?: profileApi.getCurrentUser().id
            mergeAchievements(catalog.await(), profileApi.getUserAchievements(userId))
        }
    }
}

internal fun mergeAchievements(
    catalog: List<AchievementCatalogDto>,
    earned: List<UserAchievementDto>,
): List<Achievement> {
    val earnedAt = mutableMapOf<Long, Instant?>()
    earned.forEach { item ->
        val id = item.achievementId.takeIf { it != 0 }?.toLong() ?: item.achievement?.id?.takeIf { it != 0 }?.toLong()
        if (id != null && id !in earnedAt) earnedAt[id] = item.earnedAt.toInstantOrNull()
    }
    return catalog
        .filter { it.isActive }
        .distinctBy { it.id }
        .map { dto ->
            Achievement(
                id = dto.id,
                name = dto.name?.trim()?.takeIf { it.isNotEmpty() },
                description = dto.description?.trim()?.takeIf { it.isNotEmpty() },
                rewardAmount = dto.rewardAmount,
                conditionType = dto.conditionType.toAchievementCondition(),
                isEarned = dto.id in earnedAt,
                earnedAt = earnedAt[dto.id],
            )
        }
}

fun String?.toAchievementCondition(): AchievementCondition = when (this?.trim()?.lowercase()) {
    "first_order" -> AchievementCondition.FirstOrder
    "first_thank" -> AchievementCondition.FirstThank
    "first_activity" -> AchievementCondition.FirstActivity
    else -> AchievementCondition.Other
}