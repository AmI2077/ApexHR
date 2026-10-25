package com.example.apexhr.features.home.domain

import com.example.apexhr.core.domain.DataResult

interface HomeRepository {
    suspend fun loadHome(): DataResult<HomeData>
}
