package com.example.apexhr.features.news.ui.models

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.LoadMoreState
import com.example.apexhr.core.ui.model.loadMoreStateOf
import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.news.domain.CompanyNews
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Короткие месяцы, как в вебе: «18 ноя 2026, 01:59». */
private val NEWS_MONTHS = listOf("янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")

private val NEWS_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

/** «18 ноя 2026, 01:59» в часовом поясе [zone]. */
fun Instant.toNewsDate(zone: ZoneId): String {
    val local = atZone(zone)
    return "${local.dayOfMonth} ${NEWS_MONTHS[local.monthValue - 1]} ${local.year}, ${local.format(NEWS_TIME_FORMAT)}"
}

/**
 * Новость в списке и на главной: заголовок и краткое описание, как в вебе.
 * @param author автор, если сервер его прислал; @param date «18 ноя 2026, 01:59».
 * @param content полный текст — только на экране новости.
 * @param imageUrl обложка (только http/https); null — заглушка.
 */
data class NewsUiModel(
    val id: Long,
    val title: String,
    val summary: String?,
    val author: String?,
    val date: String?,
    val content: String? = null,
    val imageUrl: String? = null,
) {
    companion object {
        val Preview = listOf(
            NewsUiModel(2, "Тестовая новость для UI", "краткое описание", "Валерия Лапшина", "18 ноя 2026, 01:59"),
            NewsUiModel(1, "Новость", "Новость Новость", "Валерия Лапшина", "16 ноя 2026, 00:32"),
        )
    }
}

/** Заголовок null отфильтрован ещё в data-слое; запасной текст — на всякий случай. */
fun CompanyNews.toUiModel(zone: ZoneId = ZoneId.systemDefault()): NewsUiModel = NewsUiModel(
    id = id,
    title = title.orEmpty(),
    summary = summary,
    author = authorName,
    date = createdAt?.toNewsDate(zone),
    content = content,
    imageUrl = imageUrl.takeIfHttpUrl(),
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
