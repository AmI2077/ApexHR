package com.example.apexhr.features.news.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.news.domain.CompanyNews
import com.example.apexhr.features.news.domain.CompanyNewsPage
import com.example.apexhr.features.news.domain.NewsRepository

class NewsRepositoryImpl(
    private val api: NewsApi,
) : NewsRepository {

    override suspend fun getNews(page: Int, pageSize: Int): DataResult<CompanyNewsPage> = safeApiCall {
        api.getNews(page, pageSize).toDomain(page)
    }

    override suspend fun getNewsDetails(id: Long): DataResult<CompanyNews> = safeApiCall {
        api.getNewsDetails(id).toDomain()
    }
}
