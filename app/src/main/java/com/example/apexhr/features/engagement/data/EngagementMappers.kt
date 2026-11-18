package com.example.apexhr.features.engagement.data

import com.example.apexhr.features.engagement.data.dto.EngagementDto
import com.example.apexhr.features.engagement.domain.ActivityLevel
import com.example.apexhr.features.engagement.domain.ActivityLevelSource
import kotlin.coroutines.cancellation.CancellationException

/**
 * Как mapEngagement в вебе: без status уровня нет; rank приводится к 1..5;
 * progress — 0..1, а без следующего уровня (высший) — 1.
 */
fun EngagementDto.toDomain(): ActivityLevel? {
    val status = status ?: return null
    return ActivityLevel(
        code = status.code?.trim()?.takeIf { it.isNotEmpty() },
        title = status.title?.trim()?.takeIf { it.isNotEmpty() },
        rank = status.rank.coerceIn(1, 5),
        score = score,
        maxScore = maxScore.takeIf { it > 0 } ?: DEFAULT_MAX_SCORE,
        nextTitle = nextLevel?.title?.trim()?.takeIf { it.isNotEmpty() },
        pointsToNext = nextLevel?.pointsToNext,
        progress = nextLevel?.let { (it.progress ?: 0.0).coerceIn(0.0, 1.0) } ?: 1.0,
        breakdown = breakdown.map { source ->
            ActivityLevelSource(
                code = source.code?.trim()?.takeIf { it.isNotEmpty() },
                title = source.title?.trim()?.takeIf { it.isNotEmpty() },
                count = source.count.coerceAtLeast(0),
                points = source.points,
                maxPoints = source.maxPoints,
            )
        },
    )
}

/**
 * Уровень — дополнительная строка в профиле и лидерборде: если он не загрузился,
 * экран показывается без него, а не с ошибкой (в вебе — так же, предупреждением).
 */
suspend fun EngagementApi.loadActivityLevelOrNull(): ActivityLevel? = try {
    getMyEngagement().toDomain()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}

private const val DEFAULT_MAX_SCORE = 100.0
