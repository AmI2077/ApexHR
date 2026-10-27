package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import com.example.apexhr.features.profile.domain.TransactionPage
import com.example.apexhr.features.profile.domain.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class TransactionHistoryRepositoryImplTest {

    private val api = FakeHistoryProfileApi()
    private val repository = TransactionHistoryRepositoryImpl(api)

    @Test
    fun `page is requested with given skip and page size`() = runBlocking {
        repository.loadPage(skip = 50)

        assertEquals(50, api.transactionsSkip)
        assertEquals(TransactionHistoryRepositoryImpl.PAGE_SIZE, api.transactionsTake)
    }

    @Test
    fun `page size matches the web`() {
        assertEquals(50, TransactionHistoryRepositoryImpl.PAGE_SIZE)
    }

    @Test
    fun `transactions are mapped to history`() = runBlocking {
        api.transactions = listOf(
            TransactionDto(id = 2, amount = -50, description = "Заказ", relatedEntityType = "order"),
            TransactionDto(id = 1, amount = 100, description = "Спасибо", relatedEntityType = "thank"),
        )

        val page = (repository.loadPage(skip = 0) as DataResult.Success).data

        assertEquals(listOf(2L, 1L), page.transactions.map { it.id })
        assertEquals(
            listOf(TransactionType.Purchase, TransactionType.Thanks),
            page.transactions.map { it.type },
        )
    }

    @Test
    fun `full page means there may be more`() = runBlocking {
        api.transactions = transactions(count = TransactionHistoryRepositoryImpl.PAGE_SIZE)

        val page = (repository.loadPage(skip = 0) as DataResult.Success).data

        assertTrue(page.hasMore)
    }

    @Test
    fun `short page is the last one`() = runBlocking {
        api.transactions = transactions(count = TransactionHistoryRepositoryImpl.PAGE_SIZE - 1)

        val page = (repository.loadPage(skip = 0) as DataResult.Success).data

        assertFalse(page.hasMore)
    }

    @Test
    fun `empty page is the last one`() = runBlocking {
        api.transactions = emptyList()

        assertEquals(
            DataResult.Success(TransactionPage(transactions = emptyList(), hasMore = false)),
            repository.loadPage(skip = 100),
        )
    }

    @Test
    fun `server error is mapped`() = runBlocking {
        api.transactionsError = ApiStatusException(503)

        assertEquals(DataResult.Failure(DataError.Server), repository.loadPage(skip = 0))
    }

    @Test
    fun `network error is mapped`() = runBlocking {
        api.transactionsError = UnknownHostException("no network")

        assertEquals(DataResult.Failure(DataError.Network), repository.loadPage(skip = 50))
    }

    private fun transactions(count: Int) = List(count) { index ->
        TransactionDto(id = index.toLong(), amount = 10, relatedEntityType = "achievement")
    }
}

private class FakeHistoryProfileApi : ProfileApi {
    var transactions: List<TransactionDto> = emptyList()
    var transactionsError: Throwable? = null

    var transactionsSkip: Int? = null
    var transactionsTake: Int? = null

    override suspend fun getCurrentUser(): CurrentUserProfileDto = error("не нужен истории")

    override suspend fun getBalance(): BalanceDto = error("не нужен истории")

    override suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto> {
        transactionsSkip = skip
        transactionsTake = take
        transactionsError?.let { throw it }
        return transactions
    }

    override suspend fun updateOwnProfile(request: UpdateOwnProfileRequest) = error("not used")

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> =
        error("не нужен истории")
}
