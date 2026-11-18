package com.example.apexhr.features.colleague.data

import com.example.apexhr.core.domain.AuthTokens
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.colleague.data.dto.ColleagueLeaderboardEntryDto
import com.example.apexhr.features.colleague.data.dto.ColleagueUserDto
import com.example.apexhr.features.colleague.domain.ColleagueProfile
import com.example.apexhr.features.colleague.domain.UserRole
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.profile.data.dto.AchievementDto
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import com.example.apexhr.features.profile.data.testJwt
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.Thank
import com.example.apexhr.features.thanks.domain.ThanksHistory
import com.example.apexhr.features.thanks.domain.ThanksRepository
import com.example.apexhr.features.thanks.domain.ThanksSendResult
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ColleagueRepositoryImplTest {

    private val api = FakeColleagueApi()
    private val profileApi = FakeAchievementsProfileApi()
    private val thanks = FakeHistoryThanksRepository()
    private val tokens = StaticTokenStorage(testJwt("""{"sub":"me"}"""))
    private val repository = ColleagueRepositoryImpl(api, profileApi, thanks, tokens)

    private fun ColleagueRepositoryImpl.load(userId: String): ColleagueProfile = runBlocking {
        (loadColleague(userId) as DataResult.Success).data
    }

    @Test
    fun `colleague profile shows my thanks to colleague and achievements`() {
        api.users["col"] = ColleagueUserDto(id = "col", firstName = " Анна ", lastName = "Петрова", roles = listOf("Manager"))
        api.leaderboard = listOf(ColleagueLeaderboardEntryDto(userId = "COL", thanksCount = 5))
        profileApi.achievements["col"] = listOf(
            UserAchievementDto(id = 3, achievement = AchievementDto(id = 1, name = "Первая благодарность")),
        )
        thanks.history = DataResult.Success(
            ThanksHistory(
                received = listOf(thank(100, "col", day = 1)),
                sent = listOf(thank(1, "col", day = 1), thank(2, "other", day = 2), thank(3, "COL", day = 3)),
            )
        )

        val profile = repository.load("col")

        assertEquals("Анна", profile.firstName)
        assertEquals(UserRole.Manager, profile.role)
        assertFalse(profile.isMe)
        assertEquals(5L, profile.thanksScore)
        assertEquals(listOf(3L, 1L), profile.receivedThanks.map { it.id })
        assertTrue(profile.receivedThanks.all { it.fromMe })
        assertEquals(listOf("Первая благодарность"), profile.achievements.map { it.title })
        assertEquals(listOf(30 to 100), api.leaderboardRequests)
    }

    @Test
    fun `own profile shows received thanks with sender names`() {
        api.users["ME"] = ColleagueUserDto(id = "ME")
        thanks.history = DataResult.Success(
            ThanksHistory(
                received = (1L..10L).map { thank(it, "sender-$it", day = it.toInt(), name = "Коллега $it") },
                sent = listOf(thank(50, "ME", day = 1)),
            )
        )

        val profile = repository.load("ME")

        assertTrue(profile.isMe)
        assertEquals(8, profile.receivedThanks.size)
        assertEquals(10L, profile.receivedThanks.first().id)
        assertEquals("Коллега 10", profile.receivedThanks.first().fromName)
        assertFalse(profile.receivedThanks.first().fromMe)
    }

    @Test
    fun `optional parts may fail`() {
        api.users["col"] = ColleagueUserDto(id = "col", isActive = false)
        api.leaderboardError = IOException("нет сети")
        profileApi.achievementsError = IOException("нет сети")
        thanks.history = DataResult.Failure(DataError.Server)

        val profile = repository.load("col")

        assertNull(profile.thanksScore)
        assertTrue(profile.receivedThanks.isEmpty())
        assertTrue(profile.achievements.isEmpty())
        assertFalse(profile.isActive)
    }

    @Test
    fun `user load failure is an error`() = runBlocking {
        val result = repository.loadColleague("missing")

        assertEquals(DataResult.Failure(DataError.Network), result)
    }

    @Test
    fun `without token profile is not own`() {
        api.users["me"] = ColleagueUserDto(id = "me")
        val noToken = ColleagueRepositoryImpl(api, profileApi, thanks, StaticTokenStorage(null))

        assertFalse(noToken.load("me").isMe)
    }

    private fun thank(id: Long, counterpartId: String, day: Int, name: String? = null) = Thank(
        id = id,
        counterpartId = counterpartId,
        counterpartName = name,
        amount = 10,
        message = "Спасибо",
        createdAt = Instant.parse("2026-11-01T10:00:00Z").plusSeconds(day * 86_400L),
    )
}

private class FakeColleagueApi : ColleagueApi {
    val users = mutableMapOf<String, ColleagueUserDto>()
    var leaderboard: List<ColleagueLeaderboardEntryDto> = emptyList()
    var leaderboardError: Throwable? = null
    val leaderboardRequests = mutableListOf<Pair<Int, Int>>()

    override suspend fun getUser(userId: String): ColleagueUserDto =
        users[userId] ?: throw IOException("нет сети")

    override suspend fun getLeaderboard(days: Int, limit: Int): List<ColleagueLeaderboardEntryDto> {
        leaderboardRequests += days to limit
        leaderboardError?.let { throw it }
        return leaderboard
    }
}

private class FakeAchievementsProfileApi : ProfileApi {
    val achievements = mutableMapOf<String, List<UserAchievementDto>>()
    var achievementsError: Throwable? = null

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> {
        achievementsError?.let { throw it }
        return achievements[userId].orEmpty()
    }

    override suspend fun getCurrentUser(): CurrentUserProfileDto = throw UnsupportedOperationException()
    override suspend fun getBalance(): BalanceDto = throw UnsupportedOperationException()
    override suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto> =
        throw UnsupportedOperationException()
    override suspend fun updateOwnProfile(request: UpdateOwnProfileRequest) = throw UnsupportedOperationException()
}

private class FakeHistoryThanksRepository : ThanksRepository {
    var history: DataResult<ThanksHistory> =
        DataResult.Success(ThanksHistory(received = emptyList(), sent = emptyList()))

    override suspend fun loadColleagues(): DataResult<List<Colleague>> = throw UnsupportedOperationException()
    override suspend fun loadHistory(): DataResult<ThanksHistory> = history
    override suspend fun sendThanks(toUserId: String, message: String): ThanksSendResult =
        throw UnsupportedOperationException()
    override val thanksSent: Flow<Unit> = emptyFlow()
}

private class StaticTokenStorage(private val token: String?) : TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens) = Unit
    override suspend fun getTokens(): AuthTokens? = null
    override suspend fun getAccessToken(): String? = token
    override suspend fun clearTokens() = Unit
}