package com.example.apexhr.features.activities.data.dto

import kotlinx.serialization.Serializable

/**
 * Элемент GET /api/achievements, поля сняты с живого ответа: `{"id":2,"code":"FIRST_THANK",
 * "name":"Первая благодарность","description":"…","iconUrl":null,"rewardAmount":10,
 * "conditionType":"first_thank","conditionValue":1,"isActive":true,"createdAt":"…"}`.
 */
@Serializable
data class AchievementCatalogDto(
    val id: Long,
    val name: String? = null,
    val description: String? = null,
    val rewardAmount: Long = 0,
    val conditionType: String? = null,
    val isActive: Boolean = true,
)