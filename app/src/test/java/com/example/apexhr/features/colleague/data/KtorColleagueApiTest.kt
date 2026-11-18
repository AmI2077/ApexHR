package com.example.apexhr.features.colleague.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.testing.JsonRecordingHttpClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Запросы KtorColleagueApi; в сеть ничего не уходит. */
class KtorColleagueApiTest {

    @Test
    fun `user is requested by id`() = runBlocking {
        val http = JsonRecordingHttpClient(
            body = """{"id":"u-1","firstName":"Анна","isActive":false,"roles":["Manager"],"canAccessAdminPanel":false,"lastLoginAt":null}""",
        )

        val user = KtorColleagueApi(http.client).getUser("u-1")

        assertEquals("GET", http.requests.single().method)
        assertEquals("${NetworkConfig.BASE_URL}/api/users/u-1", http.requests.single().url.toString())
        assertEquals(listOf("Manager"), user.roles)
        assertFalse(user.isActive)
    }

    @Test
    fun `leaderboard is requested with days and limit`() = runBlocking {
        val http = JsonRecordingHttpClient(body = """[{"rank":1,"userId":"u-1","name":"u-1","thanksCount":4}]""")

        val entries = KtorColleagueApi(http.client).getLeaderboard(days = 30, limit = 100)

        assertEquals("${NetworkConfig.BASE_URL}/api/leaderboard?days=30&limit=100", http.requests.single().url.toString())
        assertEquals(4L, entries.single().thanksCount)
    }
}