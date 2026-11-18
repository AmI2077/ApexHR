package com.example.apexhr.features.news

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.ui.model.LoadMoreState
import com.example.apexhr.features.news.data.NewsRepositoryImpl
import com.example.apexhr.features.news.data.dto.CompanyNewsDto
import com.example.apexhr.features.news.data.dto.CompanyNewsPageDto
import com.example.apexhr.features.news.data.toDomain
import com.example.apexhr.features.news.ui.NewsDetailsViewModel
import com.example.apexhr.features.news.ui.NewsViewModel
import com.example.apexhr.features.news.ui.models.NewsDetailsScreenState
import com.example.apexhr.features.news.ui.models.NewsScreenState
import com.example.apexhr.features.news.ui.models.toNewsDate
import com.example.apexhr.features.news.ui.models.toUiModel
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val api = FakeNewsApi()
    private val repository = NewsRepositoryImpl(api)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `live page is parsed and untitled news are skipped`() {
        val live = """
            {"items":[{"id":1,"title":"Новость","summary":"Новость Новость","content":"Текст","imageUrl":null,
            "authorId":"2c659784-e804-415c-9e93-982615ed81ff","authorName":"Валерия Лапшина",
            "createdAt":"2026-11-15T21:32:12.127907+00:00","updatedAt":"2026-11-15T21:32:12.127907+00:00"},
            {"id":3,"title":"  ","summary":null}],
            "totalCount":2,"page":1,"pageSize":20,"totalPages":1,"hasPreviousPage":false,"hasNextPage":true}
        """.trimIndent()

        val page = json.decodeFromString<CompanyNewsPageDto>(live).toDomain(requestedPage = 1)

        val news = page.items.single()
        assertEquals("Новость", news.title)
        assertEquals("Валерия Лапшина", news.authorName)
        assertEquals(Instant.parse("2026-11-15T21:32:12.127907Z"), news.createdAt)
        assertEquals(true, page.hasNextPage)
        val ui = news.toUiModel(ZoneOffset.ofHours(3))
        // Формат веба: «18 ноя 2026, 01:59» в часовом поясе пользователя.
        assertEquals("16 ноя 2026, 00:32", ui.date)
        assertEquals("Текст", ui.content)
        assertEquals(null, ui.imageUrl)
    }

    @Test
    fun `cover url and web date format`() {
        val dto = CompanyNewsDto(
            id = 2, title = "Тестовая новость для UI", summary = "краткое описание",
            imageUrl = " https://cdn.example.com/news/2.jpg ", createdAt = "2026-11-17T22:59:45.096685+00:00",
        )

        val ui = dto.toDomain().toUiModel(ZoneOffset.ofHours(3))

        assertEquals("https://cdn.example.com/news/2.jpg", ui.imageUrl)
        assertEquals("18 ноя 2026, 01:59", ui.date)
        // Не http(s) — обложки нет, будет заглушка.
        assertEquals(null, dto.copy(imageUrl = "ftp://x/1.jpg").toDomain().toUiModel().imageUrl)
        assertEquals("1 мая 2026, 09:05", Instant.parse("2026-05-01T06:05:00Z").toNewsDate(ZoneOffset.ofHours(3)))
    }

    @Test
    fun `list loads 20 per page, appends next page and stops at the last`() {
        api.pages[1] = CompanyNewsPageDto(items = listOf(dto(3), dto(2)), hasNextPage = true)
        api.pages[2] = CompanyNewsPageDto(items = listOf(dto(2), dto(1)), hasNextPage = false)
        val viewModel = NewsViewModel(repository)

        viewModel.onLoadMore()

        val ui = (viewModel.state.value as NewsScreenState.Content).uiState
        assertEquals(listOf(3L, 2L, 1L), ui.items.map { it.id })
        assertEquals(LoadMoreState.Hidden, ui.loadMore)
        assertEquals(listOf(1 to 20, 2 to 20), api.requested)
    }

    @Test
    fun `load more error stays in footer and retry continues`() {
        api.pages[1] = CompanyNewsPageDto(items = listOf(dto(2)), hasNextPage = true)
        val viewModel = NewsViewModel(repository)

        api.error = ApiStatusException(500)
        viewModel.onLoadMore()
        var ui = (viewModel.state.value as NewsScreenState.Content).uiState
        assertEquals(LoadMoreState.Error(DataError.Server), ui.loadMore)

        api.error = null
        api.pages[2] = CompanyNewsPageDto(items = listOf(dto(1)), hasNextPage = false)
        viewModel.onLoadMore()
        ui = (viewModel.state.value as NewsScreenState.Content).uiState
        assertEquals(listOf(2L, 1L), ui.items.map { it.id })
    }

    @Test
    fun `first page error and retry`() {
        api.error = ApiStatusException(503)
        val viewModel = NewsViewModel(repository)
        assertEquals(NewsScreenState.Error(DataError.Server), viewModel.state.value)

        api.error = null
        api.pages[1] = CompanyNewsPageDto(items = listOf(dto(1)))
        viewModel.onRetryClick()
        assertEquals(1, (viewModel.state.value as NewsScreenState.Content).uiState.items.size)
    }

    @Test
    fun `details are loaded by id`() = runBlocking {
        api.details[5] = dto(5).copy(content = "Полный текст")
        val viewModel = NewsDetailsViewModel(5, repository)

        val content = viewModel.state.value as NewsDetailsScreenState.Content
        assertEquals("Полный текст", content.news.content)

        val missing = NewsDetailsViewModel(6, repository)
        assertTrue(missing.state.value is NewsDetailsScreenState.Error)
        assertTrue(repository.getNewsDetails(6) is DataResult.Failure)
    }

    private fun dto(id: Long) = CompanyNewsDto(
        id = id,
        title = "Новость $id",
        summary = "Коротко",
        authorName = "Автор",
        createdAt = "2026-11-1${id}T10:00:00+00:00",
    )
}
