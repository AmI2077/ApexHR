package com.example.apexhr.features.engagement.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.engagement.domain.ActivityLevel
import com.example.apexhr.features.engagement.domain.EngagementRepository

class EngagementRepositoryImpl(
    private val api: EngagementApi,
) : EngagementRepository {

    override suspend fun getActivityLevel(): DataResult<ActivityLevel> =
        when (val result = safeApiCall { api.getMyEngagement().toDomain() }) {
            is DataResult.Success -> result.data?.let { DataResult.Success(it) }
                ?: DataResult.Failure(DataError.Unknown)

            is DataResult.Failure -> result
        }
}
