package com.example.apexhr.features.home.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.engagement.FakeEngagementApi
import com.example.apexhr.features.home.data.dto.HomeAchievementDto
import com.example.apexhr.features.home.data.dto.HomeUserDto
import com.example.apexhr.features.home.data.dto.LeaderboardEntryDto
import com.example.apexhr.features.home.data.dto.QuestListItemDto
import com.example.apexhr.features.home.data.dto.QuestPageDto
import com.example.apexhr.features.home.domain.HomeData
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.profile.data.dto.AchievementDto
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.UnknownHostException

class HomeRepositoryImplTest {

    private val homeApi = FakeHomeApi()
    private val profileApi = FakeProfileApi()
    private val engagementApi = FakeEngagementApi()
    private val repository = HomeRepositoryImpl(homeApi, profileApi, engagementApi)

    @Test
    fun `home gets my activity level and works without it`() = runBlocking {
        assertEquals("Искра", (repository.loadHome() as DataResult.Success).data.activityLevel?.title)

        engagementApi.error = UnknownHostException()
        val result = repository.loadHome()

        assertEquals(null, (result as DataResult.Success).data.activityLevel)
    }

    private suspend fun loadData(): HomeData =
        (repository.loadHome() as DataResult.Success).data

    @Test
    fun `loads user balance and lists`() = runBlocking {
        val data = loadData()

        assertEquals("Анна", data.user.firstName)
        assertEquals(120L, data.balance)
        assertEquals("user-1", profileApi.achievementsUserId)
    }

    @Test
    fun `requests use home page sizes`() = runBlocking {
        loadData()

        assertEquals(1, homeApi.questsPage)
        assertEquals(HomeRepositoryImpl.QUESTS_PAGE_SIZE, homeApi.questsPageSize)
        assertEquals(HomeRepositoryImpl.LEADERBOARD_DAYS, homeApi.leaderboardDays)
        assertEquals(HomeRepositoryImpl.LEADERBOARD_MIN_LIMIT, homeApi.leaderboardLimit)
    }

    @Test
    fun `leaderboard limit covers all employees`() = runBlocking {
        homeApi.users = (1..25).map { HomeUserDto(id = "u-$it") }

        loadData()

        assertEquals(25, homeApi.leaderboardLimit)
    }

    @Test
    fun `leaderboard limit stays within server maximum`() = runBlocking {
        homeApi.users = (1..150).map { HomeUserDto(id = "u-$it") }

        repository.loadHome()

        assertEquals(HomeRepositoryImpl.LEADERBOARD_MAX_LIMIT, homeApi.leaderboardLimit)
    }

    @Test
    fun `finished quests are skipped and list is limited`() = runBlocking {
        homeApi.quests = listOf(
            QuestListItemDto(id = 1, participationStatus = "completed"),
            QuestListItemDto(id = 2, participationStatus = null),
            QuestListItemDto(id = 3, participationStatus = "in_progress"),
            QuestListItemDto(id = 4, participationStatus = "cancelled"),
            QuestListItemDto(id = 5),
            QuestListItemDto(id = 6),
        )

        assertEquals(listOf(2L, 3L, 5L), loadData().quests.map { it.id })
    }

    @Test
    fun `leaderboard is sorted by rank`() = runBlocking {
        homeApi.users = emptyList()
        homeApi.leaderboard = listOf(
            LeaderboardEntryDto(rank = 2, userId = "b"),
            LeaderboardEntryDto(rank = 1, userId = "a"),
        )

        assertEquals(listOf("a", "b"), loadData().leaderboard.map { it.userId })
    }

    @Test
    fun `employees without thanks are added to leaderboard`() = runBlocking {
        homeApi.leaderboard = listOf(LeaderboardEntryDto(rank = 1, userId = "u-1", name = "Иван", thanksCount = 3))
        homeApi.users = listOf(
            HomeUserDto(id = "u-1", firstName = "Иван"),
            HomeUserDto(id = "u-2", firstName = "Пётр", lastName = "Сидоров"),
        )

        val leaderboard = loadData().leaderboard

        assertEquals(listOf("u-1", "u-2"), leaderboard.map { it.userId })
        assertEquals(listOf(3L, 0L), leaderboard.map { it.thanksCount })
        assertEquals("Пётр Сидоров", leaderboard[1].name)
    }

    @Test
    fun `users failure fails the whole screen`() = runBlocking {
        homeApi.usersError = ApiStatusException(500)

        assertEquals(DataResult.Failure(DataError.Server), repository.loadHome())
    }

    @Test
    fun `all catalog achievements are shown with earned flag`() = runBlocking {
        homeApi.achievements = (1..5).map { HomeAchievementDto(id = it, name = "Достижение $it") }
        profileApi.achievements = listOf(
            UserAchievementDto(id = 10, achievementId = 4),
            UserAchievementDto(id = 11, achievementId = 2),
        )

        val achievements = loadData().achievements

        assertEquals(listOf(2, 4, 1, 3, 5), achievements.map { it.id })
        assertEquals(listOf(true, true, false, false, false), achievements.map { it.isEarned })
    }

    @Test
    fun `achievements catalog failure fails the whole screen`() = runBlocking {
        homeApi.achievementsError = ApiStatusException(500)

        assertEquals(DataResult.Failure(DataError.Server), repository.loadHome())
    }

    @Test
    fun `failure of any request fails the whole screen`() = runBlocking {
        homeApi.leaderboardError = ApiStatusException(500)

        assertEquals(DataResult.Failure(DataError.Server), repository.loadHome())
    }

    @Test
    fun `network error is mapped`() = runBlocking {
        profileApi.userError = UnknownHostException("no network")

        assertEquals(DataResult.Failure(DataError.Network), repository.loadHome())
    }

    @Test
    fun `401 is mapped to unauthorized`() = runBlocking {
        homeApi.questsError = ApiStatusException(401)

        assertEquals(DataResult.Failure(DataError.Unauthorized), repository.loadHome())
    }
}

private class FakeHomeApi : HomeApi {
    var quests = listOf(QuestListItemDto(id = 1, title = "Квест", rewardAmount = 100))
    var leaderboard = listOf(LeaderboardEntryDto(rank = 1, userId = "u-1", name = "Иван", thanksCount = 3))
    var users = listOf(HomeUserDto(id = "u-1", firstName = "Иван"))
    var questsError: Throwable? = null
    var leaderboardError: Throwable? = null
    var usersError: Throwable? = null
    var achievements = listOf(HomeAchievementDto(id = 1, name = "Первая покупка"))
    var achievementsError: Throwable? = null

    var questsPage: Int? = null
    var questsPageSize: Int? = null
    var leaderboardDays: Int? = null
    var leaderboardLimit: Int? = null

    override suspend fun getQuests(page: Int, pageSize: Int): QuestPageDto {
        questsError?.let { throw it }
        questsPage = page
        questsPageSize = pageSize
        return QuestPageDto(items = quests)
    }

    override suspend fun getLeaderboard(days: Int, limit: Int): List<LeaderboardEntryDto> {
        leaderboardError?.let { throw it }
        leaderboardDays = days
        leaderboardLimit = limit
        return leaderboard
    }

    override suspend fun getUsers(): List<HomeUserDto> {
        usersError?.let { throw it }
        return users
    }

    override suspend fun getAchievements(): List<HomeAchievementDto> {
        achievementsError?.let { throw it }
        return achievements
    }
}

private class FakeProfileApi : ProfileApi {
    var userError: Throwable? = null
    var achievements = listOf(
        UserAchievementDto(id = 1, achievementId = 1, achievement = AchievementDto(id = 1, name = "Первая покупка")),
    )
    var achievementsUserId: String? = null

    override suspend fun getCurrentUser(): CurrentUserProfileDto {
        userError?.let { throw it }
        return CurrentUserProfileDto(id = "user-1", firstName = "Анна", lastName = "Иванова")
    }

    override suspend fun getBalance(): BalanceDto = BalanceDto(amount = 120)

    override suspend fun getTransactions(skip: Int, take: Int): List<TransactionDto> = emptyList()

    override suspend fun updateOwnProfile(request: UpdateOwnProfileRequest) = error("not used")

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> {
        achievementsUserId = userId
        return achievements
    }
}
