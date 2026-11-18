package com.example.apexhr.features.news.ui.models

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.LoadMoreState
import com.example.apexhr.core.ui.model.loadMoreStateOf
import com.example.apexhr.features.news.domain.CompanyNews
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val NEWS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/**
 * Новость в списке и на главной: заголовок и краткое описание, как в вебе.
 * @param author автор, если сервер его прислал; @param date «15.11.2026».
 * @param content полный текст — только на экране новости.
 */
data class NewsUiModel(
    val id: Long,
    val title: String,
    val summary: String?,
    val author: String?,
    val date: String?,
    val content: String? = null,
) {
    companion object {
        val Preview = listOf(
            NewsUiModel(2, "Тестовая новость для UI", "краткое описание", "Валерия Лапшина", "18.11.2026"),
            NewsUiModel(1, "Новость", "Новость Новость", "Валерия Лапшина", "16.11.2026"),
        )
    }
}

/** Заголовок null отфильтрован ещё в data-слое; запасной текст — на всякий случай. */
fun CompanyNews.toUiModel(zone: ZoneId = ZoneId.systemDefault()): NewsUiModel = NewsUiModel(
    id = id,
    title = title.orEmpty(),
    summary = summary,
    author = authorName,
    date = createdAt?.atZone(zone)?.format(NEWS_DATE_FORMAT),
    content = content,
)

sealed interface NewsScreenState {
    data object Loading : NewsScreenState
    data class Error(val error: DataError) : NewsScreenState
    data class Content(val uiState: NewsUiState) : NewsScreenState
}

/** Лента новостей с бесконечной прокруткой по 20, как NewsDesk в вебе. */
data class NewsUiState(
    val items: List<NewsUiModel>,
    val hasNextPage: Boolean,
    val isLoadingMore: Boolean = false,
    val loadMoreError: DataError? = null,
) {
    val loadMore: LoadMoreState
        get() = loadMoreStateOf(hasMore = hasNextPage, isLoading = isLoadingMore, error = loadMoreError)
}

interface NewsScreenCallbacks {
    fun onRetryClick()
    fun onLoadMore()
    fun onBackClick() = Unit
    fun onNewsClick(id: Long) = Unit

    companion object {
        val Empty = object : NewsScreenCallbacks {
            override fun onRetryClick() = Unit
            override fun onLoadMore() = Unit
        }
    }
}

sealed interface NewsDetailsScreenState {
    data object Loading : NewsDetailsScreenState
    data class Error(val error: DataError) : NewsDetailsScreenState
    data class Content(val news: NewsUiModel) : NewsDetailsScreenState
}

interface NewsDetailsScreenCallbacks {
    fun onRetryClick()
    fun onBackClick() = Unit

    companion object {
        val Empty = object : NewsDetailsScreenCallbacks {
            override fun onRetryClick() = Unit
        }
    }
}
