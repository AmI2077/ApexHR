package com.example.apexhr.features.news.data.dto

import kotlinx.serialization.Serializable

/**
 * Новость из GET /api/company-news и /api/company-news/{id}. Живой пример:
 * `{"id":1,"title":"Новость","summary":"Новость Новость","content":"…","imageUrl":null,
 * "authorId":"…","authorName":"Валерия Лапшина","authorAvatarUrl":"…",
 * "createdAt":"2026-11-15T21:32:12.127907+00:00","updatedAt":"…"}`.
 */
@Serializable
data class CompanyNewsDto(
    val id: Long,
    val title: String? = null,
    val summary: String? = null,
    val content: String? = null,
    /** Обложка; у живых новостей пока null — тогда показывается заглушка. */
    val imageUrl: String? = null,
    val authorName: String? = null,
    val createdAt: String? = null,
)

/** Страница: `{items, totalCount, page, pageSize, totalPages, hasPreviousPage, hasNextPage}`. */
@Serializable
data class CompanyNewsPageDto(
    val items: List<CompanyNewsDto> = emptyList(),
    val page: Int = 1,
    val hasNextPage: Boolean = false,
)
