package com.example.apexhr.features.news

import com.example.apexhr.features.news.data.NewsApi
import com.example.apexhr.features.news.data.dto.CompanyNewsDto
import com.example.apexhr.features.news.data.dto.CompanyNewsPageDto

/** Подмена GET /api/company-news для тестов: страницы по номеру. */
class FakeNewsApi : NewsApi {
    val pages = mutableMapOf<Int, CompanyNewsPageDto>()
    val details = mutableMapOf<Long, CompanyNewsDto>()
    var error: Throwable? = null
    val requested = mutableListOf<Pair<Int, Int>>()

    override suspend fun getNews(page: Int, pageSize: Int): CompanyNewsPageDto {
        requested += page to pageSize
        error?.let { throw it }
        return pages[page] ?: CompanyNewsPageDto()
    }

    override suspend fun getNewsDetails(id: Long): CompanyNewsDto {
        error?.let { throw it }
        return details[id] ?: throw com.example.apexhr.core.data.network.ApiStatusException(404)
    }
}
