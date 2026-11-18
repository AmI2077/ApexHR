package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.testing.JsonRecordingHttpClient
import com.example.apexhr.core.testing.bodyText
import com.example.apexhr.features.activities.data.dto.CompleteQuestStepRequest
import com.example.apexhr.features.profile.data.bodyBytes
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/** Запросы подтверждения шага и загрузки в бакет proofs; в сеть ничего не уходит. */
class QuestProofApisTest {

    @Test
    fun `proof file is uploaded to proofs bucket with user token and anon key`() = runBlocking {
        val http = JsonRecordingHttpClient()
        val bytes = byteArrayOf(1, 2, 3)

        KtorProofStorageApi(http.client).upload("user-1/file.pdf", bytes, "application/pdf", "user-token")

        val request = http.requests.single()
        assertEquals("POST", request.method)
        assertEquals("${NetworkConfig.SUPABASE_URL}/storage/v1/object/proofs/user-1/file.pdf", request.url.toString())
        assertEquals("Bearer user-token", request.header("Authorization"))
        assertEquals(NetworkConfig.SUPABASE_ANON_KEY, request.header("apikey"))
        assertEquals("false", request.header("x-upsert"))
        assertEquals("3600", request.header("Cache-Control"))
        assertEquals("application/pdf", request.body!!.contentType().toString())
        assertArrayEquals(bytes, request.bodyBytes())
    }

    @Test
    fun `storage refusal throws status`() = runBlocking {
        try {
            KtorProofStorageApi(JsonRecordingHttpClient(code = 403).client)
                .upload("user-1/file.jpg", byteArrayOf(1), "image/jpeg", "user-token")
            fail("Ожидалась ошибка")
        } catch (e: ApiStatusException) {
            assertEquals(403, e.statusCode)
        }
    }

    @Test
    fun `proof completes step with post`() = runBlocking {
        val http = JsonRecordingHttpClient()

        KtorQuestProofApi(http.client).completeStepWithProof(2, 4, CompleteQuestStepRequest("https://x/p.jpg", "Готово"))

        val request = http.requests.single()
        assertEquals("POST", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/quests/2/steps/4/complete", request.url.toString())
        assertEquals("""{"storagePath":"https://x/p.jpg","comment":"Готово"}""", request.bodyText())
    }

    @Test
    fun `pending proof is replaced with put`() = runBlocking {
        val http = JsonRecordingHttpClient()

        KtorQuestProofApi(http.client).updateProof(2, 4, CompleteQuestStepRequest("https://x/p.jpg", null))

        val request = http.requests.single()
        assertEquals("PUT", request.method)
        assertEquals("${NetworkConfig.BASE_URL}/api/quests/2/steps/4/proof", request.url.toString())
        assertEquals("""{"storagePath":"https://x/p.jpg"}""", request.bodyText())
    }

    @Test
    fun `server message is kept on refusal`() = runBlocking {
        val http = JsonRecordingHttpClient(code = 409, body = """{"message":"Step already completed."}""")
        try {
            KtorQuestProofApi(http.client).completeStepWithProof(2, 4, CompleteQuestStepRequest("u", null))
            fail("Ожидалась ошибка")
        } catch (e: ActivitiesApiException) {
            assertEquals(409, e.statusCode)
            assertEquals("Step already completed.", e.serverMessage)
        }
    }
}