package com.example.apexhr.features.engagement.domain

import com.example.apexhr.core.domain.DataResult

interface EngagementRepository {
    /** GET /api/me/engagement; ответ без status — ошибка загрузки. */
    suspend fun getActivityLevel(): DataResult<ActivityLevel>
}
