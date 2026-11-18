package com.example.apexhr.features.news.domain

import com.example.apexhr.core.domain.DataResult
import java.time.Instant

/**
 * Новость компании (GET /api/company-news). Публикует её HR в админке веба,
 * в приложении новости только читаются.
 * @param title null — сервер не прислал заголовок; такие новости не показываются.
 */
data class CompanyNews(
    val id: Long,
    val title: String?,
    val summary: String?,
    val content: String?,
    val authorName: String?,
    val createdAt: Instant?,
)

data class CompanyNewsPage(
    val items: List<CompanyNews>,
    val page: Int,
    val hasNextPage: Boolean,
)

interface NewsRepository {
    /** Страница ленты (с 1), новые сверху; по умолчанию по 20, как в вебе. */
    suspend fun getNews(page: Int, pageSize: Int = PAGE_SIZE): DataResult<CompanyNewsPage>

    suspend fun getNewsDetails(id: Long): DataResult<CompanyNews>

    companion object {
        const val PAGE_SIZE = 20

        /** На главной, как в вебе, — четыре последние новости. */
        const val HOME_NEWS_COUNT = 4
    }
}
