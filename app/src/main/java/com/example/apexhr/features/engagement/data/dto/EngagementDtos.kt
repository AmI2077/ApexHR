package com.example.apexhr.features.engagement.data.dto

import kotlinx.serialization.Serializable

/**
 * Ответ GET /api/me/engagement (EngagementDto в Swagger). Живой пример:
 * `{"status":{"code":"spark","title":"Искра","rank":1},"score":7.6,"maxScore":100,
 * "nextLevel":{"code":"flicker","title":"Мерцание","scoreRequired":18,"pointsToNext":10.4,
 * "progress":0.422},"period":{"days":30,…},"breakdown":[{"code":"surveys",
 * "title":"Пройденные опросы","count":1,"points":2.4,"maxPoints":18},…]}`.
 */
@Serializable
data class EngagementDto(
    val status: EngagementStatusDto? = null,
    val score: Double = 0.0,
    val maxScore: Double = 0.0,
    val nextLevel: EngagementNextLevelDto? = null,
    val breakdown: List<EngagementBreakdownDto> = emptyList(),
)

@Serializable
data class EngagementStatusDto(
    val code: String? = null,
    val title: String? = null,
    val rank: Int = 0,
)

@Serializable
data class EngagementNextLevelDto(
    val code: String? = null,
    val title: String? = null,
    val pointsToNext: Double? = null,
    val progress: Double? = null,
)

@Serializable
data class EngagementBreakdownDto(
    val code: String? = null,
    val title: String? = null,
    val count: Int = 0,
    val points: Double = 0.0,
    val maxPoints: Double = 0.0,
)
