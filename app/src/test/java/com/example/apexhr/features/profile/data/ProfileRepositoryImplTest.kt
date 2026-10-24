package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.data.dto.AchievementDto
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class ProfileRepositoryImplTest {

    private val api = FakeProfileApi()
    private val repository = ProfileRepositoryImpl(api)

    @Test
    fun `loads all profile parts`() = runBlocking {
        val result = repository.loadProfile()

        assertTrue(result is DataResult.Success)
        val profile = (result as DataResult.Success).data
        assertEquals("user-1", profile.user.id)
        assertEquals(250L, profile.balance)
        assertEquals(listOf(1L, 2L), profile.transactions.map { it.id })
        assertEquals(listOf("Первая покупка"), profile.achievements.map { it.title })
    }

    @Test
    fun `achievements are requested for current user id`() = runBlocking {
        repository.loadProfile()

        assertEquals("user-1", api.achievementsUserId)
    }

    @Test
    fun `transactions are requested with first page`() = runBlocking {
        repository.loadProfile()

        assertEquals(0, api.transactionsSkip)
        assertEquals(ProfileRepositoryImpl.TRANSACTIONS_PAGE_SIZE, api.transactionsTake)
    }

    @Test
    fun `failure of any request fails the whole profile`() = runBlocking {
        api.balanceError = ApiStatusException(500)

        assertEquals(DataResult.Failure(DataError.Server), repository.loadProfile())
    }

    @Test
    fun `network error is mapped`() = runBlocking {
        api.userError = UnknownHostException("no network")

        assertEquals(DataResult.Failure(DataError.Network), repository.loadProfile())
    }

    @Test
    fun `401 is mapped to unauthorized`() = runBlocking {
        api.achievementsError = ApiStatusException(401)

        assertEquals(DataResult.Failure(DataError.Unauthorized), repository.loadProfile())
    }
}

private class FakeProfileApi : ProfileApi {
    var userError: Throwable? = null
    var balanceError: Throwable? = null
    var achievementsError: Throwable? = null

    var achievementsUserId: String? = null
    var transactionsSkip: Int? = null
    var transactionsTake: Int? = null

    override suspend fun getCurrentUser(): CurrentUserProfileDto {
        userError?.let { throw it }
        return CurrentUserProfileDto(id = "user-1", firstName = "Анна", lastName = "Иванова")
    }

    override suspend fun getBalance(): BalanceDto {
        balanceError?.let { throw it }
        return BalanceDto(amount = 250)
    }

    override suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto> {
        transactionsSkip = skip
        transactionsTake = take
        return listOf(
            TransactionDto(id = 1, amount = 100, description = "Благодарность"),
            TransactionDto(id = 2, amount = -50, description = "Заказ"),
        )
    }

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> {
        achievementsError?.let { throw it }
        achievementsUserId = userId
        return listOf(
            UserAchievementDto(id = 1, achievement = AchievementDto(id = 1, name = "Первая покупка")),
        )
    }
}
