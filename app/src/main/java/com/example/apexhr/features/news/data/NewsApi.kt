package com.example.apexhr.features.news.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.news.data.dto.CompanyNewsDto
import com.example.apexhr.features.news.data.dto.CompanyNewsPageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Только чтение новостей: публикация — в админке веба, в мобильном её нет. */
interface NewsApi {
    suspend fun getNews(page: Int, pageSize: Int): CompanyNewsPageDto
    suspend fun getNewsDetails(id: Long): CompanyNewsDto
}

/** @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT). */
class KtorNewsApi(
    private val client: HttpClient,
) : NewsApi {
    override suspend fun getNews(page: Int, pageSize: Int): CompanyNewsPageDto =
        client.get(NetworkConfig.COMPANY_NEWS_URL) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.requireSuccess().body()

    override suspend fun getNewsDetails(id: Long): CompanyNewsDto =
        client.get(NetworkConfig.companyNewsUrl(id)).requireSuccess().body()
}
