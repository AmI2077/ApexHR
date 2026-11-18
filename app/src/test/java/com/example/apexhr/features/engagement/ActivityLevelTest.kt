package com.example.apexhr.features.engagement

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.features.engagement.data.dto.EngagementDto
import com.example.apexhr.features.engagement.data.loadActivityLevelOrNull
import com.example.apexhr.features.engagement.data.toDomain
import com.example.apexhr.features.engagement.domain.ActivityLevel
import com.example.apexhr.features.home.domain.HomeData
import com.example.apexhr.features.home.domain.LeaderboardEntry
import com.example.apexhr.features.home.ui.models.toUiState
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.UserProfile
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.toUiState
import com.example.apexhr.features.thanks.domain.ThanksHistory
import java.net.UnknownHostException
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActivityLevelTest {

    // Как Json в NetworkModule.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    /** Ответ GET /api/me/engagement пользователя prooo.01 (сокращён breakdown). */
    private val live = """
        {"status":{"code":"spark","title":"Искра","rank":1},"score":7.6,"maxScore":100,
         "nextLevel":{"code":"flicker","title":"Мерцание","scoreRequired":18,"pointsToNext":10.4,"progress":0.422},
         "period":{"days":30,"from":"2026-10-19T20:39:40.0392021Z","to":"2026-11-18T20:39:40.0392021Z"},
         "lastActivityAt":"2026-10-29T19:22:23.037923Z",
         "breakdown":[{"code":"surveys","title":"Пройденные опросы","count":1,"points":2.4,"maxPoints":18}],
         "computedAt":"2026-11-18T20:39:40.0392021Z"}
    """.trimIndent()

    private val spark = ActivityLevel(
        code = "spark", title = "Искра", rank = 1, score = 7.6, maxScore = 100.0,
        nextTitle = "Мерцание", pointsToNext = 10.4,
    )

    @Test
    fun `live response is parsed`() {
        assertEquals(spark, json.decodeFromString<EngagementDto>(live).toDomain())
    }

    @Test
    fun `no status means no level and rank is clamped`() {
        assertNull(EngagementDto().toDomain())
        val top = json.decodeFromString<EngagementDto>("""{"status":{"code":"x","title":" Пламя ","rank":9},"score":99}""")
            .toDomain()!!
        assertEquals(5, top.rank)
        assertEquals("Пламя", top.title)
        assertNull(top.nextTitle)
        assertEquals(100.0, top.maxScore, 0.0)
    }

    @Test
    fun `failed request gives no level instead of error`() = runBlocking {
        assertNull(FakeEngagementApi(error = ApiStatusException(500)).loadActivityLevelOrNull())
        assertNull(FakeEngagementApi(error = UnknownHostException()).loadActivityLevelOrNull())
    }

    private val user = UserProfile("ME", "Дмитрий", "Новохатский", null, null, null, null)

    @Test
    fun `profile shows level title`() {
        val profile = Profile(user = user, balance = 0, achievements = emptyList(), activityLevel = spark)
        val empty = ThanksHistory(received = emptyList(), sent = emptyList())

        assertEquals("Искра", profile.toUiState(ProfileTab.Thanks, empty, zone = ZoneOffset.UTC).activityLevel)
        assertNull(profile.copy(activityLevel = null).toUiState(ProfileTab.Thanks, empty, zone = ZoneOffset.UTC).activityLevel)
    }

    private fun home(myIndex: Int?) = HomeData(
        user = user,
        balance = 0,
        quests = emptyList(),
        leaderboard = (0 until 8).map { index ->
            LeaderboardEntry(
                rank = index + 1,
                userId = if (index == myIndex) "me" else "u-$index",
                name = "Сотрудник $index",
                thanksCount = (8 - index).toLong(),
            )
        },
        achievements = emptyList(),
        activityLevel = spark,
    )

    @Test
    fun `level is shown only in my leaderboard row`() {
        val inTop = home(myIndex = 2).toUiState()
        assertEquals(listOf(null, null, "Искра", null, null, null, null, null), inTop.leaderboard.map { it.activityLevel })
        assertNull(inTop.myLeaderboardEntry)

        val outside = home(myIndex = 6).toUiState()
        assertEquals("Искра", outside.myLeaderboardEntry?.activityLevel)
        assertEquals(1, outside.leaderboard.count { it.activityLevel != null })
    }
}
