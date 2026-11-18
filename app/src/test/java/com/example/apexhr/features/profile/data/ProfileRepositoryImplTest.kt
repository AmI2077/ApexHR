package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.engagement.FakeEngagementApi
import com.example.apexhr.features.profile.data.dto.AchievementDto
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class ProfileRepositoryImplTest {

    private val api = FakeProfileApi()
    private val engagementApi = FakeEngagementApi()
    private val repository = ProfileRepositoryImpl(api, engagementApi)

    @Test
    fun `activity level is loaded and its failure does not break profile`() = runBlocking {
        val loaded = repository.loadProfile() as DataResult.Success
        assertEquals("Искра", loaded.data.activityLevel?.title)

        engagementApi.error = ApiStatusException(500)
        val withoutLevel = repository.loadProfile()

        assertTrue(withoutLevel is DataResult.Success)
        assertNull((withoutLevel as DataResult.Success).data.activityLevel)
    }

    @Test
    fun `loads all profile parts`() = runBlocking {
        val result = repository.loadProfile()

        assertTrue(result is DataResult.Success)
        val profile = (result as DataResult.Success).data
        assertEquals("user-1", profile.user.id)
        assertEquals(250L, profile.balance)
        assertEquals(listOf("Первая покупка"), profile.achievements.map { it.title })
    }

    @Test
    fun `achievements are requested for current user id`() = runBlocking {
        repository.loadProfile()

        assertEquals("user-1", api.achievementsUserId)
    }

    @Test
    fun `transactions are not requested`() = runBlocking {
        repository.loadProfile()

        assertNull(api.transactionsSkip)
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
        return emptyList()
    }

    override suspend fun updateOwnProfile(request: UpdateOwnProfileRequest) = Unit

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> {
        achievementsError?.let { throw it }
        achievementsUserId = userId
        return listOf(
            UserAchievementDto(id = 1, achievement = AchievementDto(id = 1, name = "Первая покупка")),
        )
    }
}
