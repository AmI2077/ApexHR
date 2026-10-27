package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/** Запросы к Supabase Storage и PATCH профиля; в сеть ничего не уходит. */
class KtorAvatarStorageApiTest {

    @Test
    fun `upload posts raw jpeg with supabase headers`() = runBlocking {
        val http = RecordingHttpClient()
        val jpeg = byteArrayOf(1, 2, 3)

        KtorAvatarStorageApi(http.client).upload("user-1/a.jpg", jpeg, "token-1")

        val request = http.requests.single()
        assertEquals("POST", request.method)
        assertEquals(
            "${NetworkConfig.SUPABASE_URL}/storage/v1/object/avatars/user-1/a.jpg",
            request.url.toString(),
        )
        assertEquals("Bearer token-1", request.header("Authorization"))
        assertEquals(NetworkConfig.SUPABASE_ANON_KEY, request.header("apikey"))
        assertEquals("image/jpeg", request.body!!.contentType().toString())
        assertArrayEquals(jpeg, request.bodyBytes())
    }

    @Test
    fun `delete sends supabase headers`() = runBlocking {
        val http = RecordingHttpClient()

        KtorAvatarStorageApi(http.client).delete("user-1/old.jpg", "token-1")

        val request = http.requests.single()
        assertEquals("DELETE", request.method)
        assertEquals(NetworkConfig.avatarObjectUrl("user-1/old.jpg"), request.url.toString())
        assertEquals("Bearer token-1", request.header("Authorization"))
        assertEquals(NetworkConfig.SUPABASE_ANON_KEY, request.header("apikey"))
    }

    @Test
    fun `storage error is thrown with status`() = runBlocking {
        val http = RecordingHttpClient(code = 403)
        try {
            KtorAvatarStorageApi(http.client).upload("user-1/a.jpg", byteArrayOf(1), "token-1")
            fail("HTTP 403 должен бросать ApiStatusException")
        } catch (e: ApiStatusException) {
            assertEquals(403, e.statusCode)
        }
    }

    @Test
    fun `profile update patches users me with json`() = runBlocking {
        val http = RecordingHttpClient()

        KtorProfileApi(http.client).updateOwnProfile(
            UpdateOwnProfileRequest(firstName = "Анна", lastName = "Иванова", avatarUrl = "https://x/a.jpg")
        )

        val request = http.requests.single()
        assertEquals("PATCH", request.method)
        assertEquals(NetworkConfig.USER_PROFILE_URL, request.url.toString())
        assertEquals(
            """{"firstName":"Анна","lastName":"Иванова","avatarUrl":"https://x/a.jpg"}""",
            request.bodyBytes().toString(Charsets.UTF_8),
        )
    }
}
