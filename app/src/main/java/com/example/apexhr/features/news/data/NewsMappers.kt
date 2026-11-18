package com.example.apexhr.features.news.data

import com.example.apexhr.features.news.data.dto.CompanyNewsDto
import com.example.apexhr.features.news.data.dto.CompanyNewsPageDto
import com.example.apexhr.features.news.domain.CompanyNews
import com.example.apexhr.features.news.domain.CompanyNewsPage
import java.time.Instant
import java.time.OffsetDateTime

fun CompanyNewsDto.toDomain(): CompanyNews = CompanyNews(
    id = id,
    title = title.cleaned(),
    summary = summary.cleaned(),
    content = content.cleaned(),
    authorName = authorName.cleaned(),
    createdAt = createdAt.toNewsInstantOrNull(),
)

/** Как в вебе, новости без заголовка не показываются. */
fun CompanyNewsPageDto.toDomain(requestedPage: Int): CompanyNewsPage = CompanyNewsPage(
    items = items.map { it.toDomain() }.filter { it.title != null },
    page = requestedPage,
    hasNextPage = hasNextPage,
)

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/** Сервер присылает дату со смещением («…+00:00»), иногда — с «Z». */
fun String?.toNewsInstantOrNull(): Instant? {
    val value = cleaned() ?: return null
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
}
