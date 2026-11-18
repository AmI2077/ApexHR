package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.domain.AuthTokens
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.activities.data.dto.CompleteQuestStepRequest
import com.example.apexhr.features.activities.domain.ActivityActionError
import com.example.apexhr.features.activities.domain.MAX_PROOF_BYTES
import com.example.apexhr.features.activities.domain.QuestProofError
import com.example.apexhr.features.activities.domain.QuestProofResult
import com.example.apexhr.features.activities.domain.jpeg
import com.example.apexhr.features.activities.domain.latin1
import com.example.apexhr.features.activities.domain.png
import com.example.apexhr.features.profile.data.testJwt
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestProofRepositoryImplTest {

    private val api = FakeQuestProofApi()
    private val storage = FakeProofStorageApi()
    private val tokens = ProofTokenStorage(testJwt("""{"sub":"user-1"}"""))
    private val files = mutableMapOf<String, ByteArray>()
    private var compressed: ByteArray? = jpeg(100)
    private val compressedUris = mutableListOf<String>()

    private val repository = QuestProofRepositoryImpl(
        api = api,
        storageApi = storage,
        tokenStorage = tokens,
        fileSource = { uri, limit -> files[uri]?.let { it.copyOf(minOf(it.size, limit)) } },
        compressor = { uri -> compressedUris += uri; compressed },
        newFileId = { "file-id" },
    )

    private fun submit(
        fileUri: String?,
        existing: String? = null,
        comment: String? = null,
        update: Boolean = false,
    ) = runBlocking { repository.submitProof(2, 4, fileUri, existing, comment, update) }

    @Test
    fun `small photo is uploaded and step is completed with its public url`() {
        files["content://photo"] = jpeg(1000)

        val result = submit("content://photo", comment = "  Были всей командой ")

        assertEquals(QuestProofResult.Success, result)
        assertEquals(listOf("user-1/file-id.jpg"), storage.paths)
        assertEquals(listOf("image/jpeg"), storage.mimeTypes)
        assertEquals(1000, storage.sizes.single())
        assertEquals(tokens.token, storage.tokens.single())
        assertEquals(
            listOf(
                Triple(
                    "complete",
                    4L,
                    CompleteQuestStepRequest(NetworkConfig.proofPublicUrl("user-1/file-id.jpg"), "Были всей командой"),
                )
            ),
            api.calls,
        )
        assertTrue(compressedUris.isEmpty())
    }

    @Test
    fun `pdf keeps its type`() {
        files["content://doc"] = "%PDF-1.7 ...".latin1()

        submit("content://doc")

        assertEquals(listOf("user-1/file-id.pdf"), storage.paths)
        assertEquals(listOf("application/pdf"), storage.mimeTypes)
    }

    @Test
    fun `large photo is compressed to jpeg`() {
        files["content://big"] = png(MAX_PROOF_BYTES + 10)

        val result = submit("content://big")

        assertEquals(QuestProofResult.Success, result)
        assertEquals(listOf("content://big"), compressedUris)
        assertEquals(listOf("user-1/file-id.jpg"), storage.paths)
        assertEquals(listOf("image/jpeg"), storage.mimeTypes)
    }

    @Test
    fun `large pdf and too large compressed photo are rejected`() {
        files["content://doc"] = "%PDF".latin1() + ByteArray(MAX_PROOF_BYTES)
        files["content://big"] = png(MAX_PROOF_BYTES + 10)
        compressed = jpeg(MAX_PROOF_BYTES + 1)

        assertEquals(QuestProofResult.Failure(QuestProofError.FileTooLarge), submit("content://doc"))
        assertEquals(QuestProofResult.Failure(QuestProofError.FileTooLarge), submit("content://big"))
        assertTrue(storage.paths.isEmpty())
        assertTrue(api.calls.isEmpty())
    }

    @Test
    fun `unsupported or unreadable file is not uploaded`() {
        files["content://gif"] = "GIF89a......".latin1()

        assertEquals(QuestProofResult.Failure(QuestProofError.UnsupportedType), submit("content://gif"))
        assertEquals(QuestProofResult.Failure(QuestProofError.FileUnreadable), submit("content://missing"))
        assertTrue(storage.paths.isEmpty())
    }

    @Test
    fun `storage refusal stops before backend`() {
        files["content://photo"] = jpeg()
        storage.error = ApiStatusException(403)

        assertEquals(QuestProofResult.Failure(QuestProofError.UploadRejected), submit("content://photo"))
        assertTrue(api.calls.isEmpty())
    }

    @Test
    fun `pending proof is updated without new file`() {
        val result = submit(fileUri = null, existing = "https://x/old.jpg", comment = "", update = true)

        assertEquals(QuestProofResult.Success, result)
        assertTrue(storage.paths.isEmpty())
        assertEquals(listOf(Triple("update", 4L, CompleteQuestStepRequest("https://x/old.jpg", null))), api.calls)
    }

    @Test
    fun `new proof needs a file`() {
        assertEquals(
            QuestProofResult.Failure(QuestProofError.NoFile),
            submit(fileUri = null, existing = "https://x/old.jpg", update = false),
        )
        assertTrue(api.calls.isEmpty())
    }

    @Test
    fun `backend refusal is returned with server message`() {
        files["content://photo"] = jpeg()
        api.error = ActivitiesApiException(409, "Step already completed.")

        assertEquals(
            QuestProofResult.Failure(QuestProofError.Action(ActivityActionError.Rejected("Step already completed."))),
            submit("content://photo"),
        )
    }

    @Test
    fun `token without user id is a session problem`() {
        files["content://photo"] = jpeg()
        tokens.token = "not-a-jwt"

        assertEquals(QuestProofResult.Failure(QuestProofError.SessionMismatch), submit("content://photo"))
        assertTrue(storage.paths.isEmpty())
    }
}

private class FakeQuestProofApi : QuestProofApi {
    val calls = mutableListOf<Triple<String, Long, CompleteQuestStepRequest>>()
    var error: Throwable? = null

    override suspend fun completeStepWithProof(questId: Long, stepId: Long, request: CompleteQuestStepRequest) {
        error?.let { throw it }
        calls += Triple("complete", stepId, request)
    }

    override suspend fun updateProof(questId: Long, stepId: Long, request: CompleteQuestStepRequest) {
        error?.let { throw it }
        calls += Triple("update", stepId, request)
    }
}

private class FakeProofStorageApi : ProofStorageApi {
    val paths = mutableListOf<String>()
    val mimeTypes = mutableListOf<String>()
    val sizes = mutableListOf<Int>()
    val tokens = mutableListOf<String>()
    var error: Throwable? = null

    override suspend fun upload(path: String, bytes: ByteArray, mimeType: String, accessToken: String) {
        error?.let { throw it }
        paths += path
        mimeTypes += mimeType
        sizes += bytes.size
        tokens += accessToken
    }
}

private class ProofTokenStorage(var token: String?) : TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens) = Unit
    override suspend fun getTokens(): AuthTokens? = null
    override suspend fun getAccessToken(): String? = token
    override suspend fun clearTokens() = Unit
}