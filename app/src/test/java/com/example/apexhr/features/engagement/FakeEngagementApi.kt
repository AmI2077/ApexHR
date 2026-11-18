package com.example.apexhr.features.engagement

import com.example.apexhr.features.engagement.data.EngagementApi
import com.example.apexhr.features.engagement.data.dto.EngagementDto
import com.example.apexhr.features.engagement.data.dto.EngagementStatusDto

/** Подмена GET /api/me/engagement для тестов; по умолчанию уровень «Искра». */
class FakeEngagementApi(
    var result: EngagementDto = EngagementDto(
        status = EngagementStatusDto(code = "spark", title = "Искра", rank = 1),
        score = 7.6,
        maxScore = 100.0,
    ),
    var error: Throwable? = null,
) : EngagementApi {
    var calls = 0

    override suspend fun getMyEngagement(): EngagementDto {
        calls++
        error?.let { throw it }
        return result
    }
}
