package com.example.apexhr.features.engagement.data.dto

import kotlinx.serialization.Serializable

/**
 * Ответ GET /api/me/engagement (EngagementDto в Swagger). Живой пример:
 * `{"status":{"code":"spark","title":"Искра","rank":1},"score":7.6,"maxScore":100,
 * "nextLevel":{"code":"flicker","title":"Мерцание","scoreRequired":18,"pointsToNext":10.4,
 * "progress":0.422},"period":{"days":30,…},"breakdown":[…]}`. Нужные поля — только для уровня.
 */
@Serializable
data class EngagementDto(
    val status: EngagementStatusDto? = null,
    val score: Double = 0.0,
    val maxScore: Double = 0.0,
    val nextLevel: EngagementNextLevelDto? = null,
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
)
