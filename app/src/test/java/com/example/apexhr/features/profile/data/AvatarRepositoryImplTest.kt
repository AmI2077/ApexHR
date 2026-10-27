package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.domain.AuthTokens
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.profile.data.dto.BalanceDto
import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import com.example.apexhr.features.profile.domain.AvatarChangeError
import com.example.apexhr.features.profile.domain.AvatarChangeResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AvatarRepositoryImplTest {

    private val profileApi = AvatarFakeProfileApi()
    private val storage = FakeAvatarStorageApi()
    private val tokens = FakeTokenStorage(testJwt("""{"sub":"user-1"}"""))
    private var image: ByteArray? = byteArrayOf(7, 7)
    private val repository = AvatarRepositoryImpl(
        profileApi = profileApi,
        storageApi = storage,
        tokenStorage = tokens,
        compressor = { image },
        newFileId = { "new" },
    )

    @Test
    fun `photo is uploaded, saved to profile with current name and old file removed`() = runBlocking {
        profileApi.user = profileApi.user.copy(avatarUrl = NetworkConfig.avatarPublicUrl("user-1/old.jpg"))

        val result = repository.changeAvatar("content://photo")

        val url = NetworkConfig.avatarPublicUrl("user-1/new.jpg")
        assertEquals(AvatarChangeResult.Success(url), result)
        assertEquals(listOf("user-1/new.jpg"), storage.uploaded)
        assertEquals(tokens.token, storage.lastToken)
        assertEquals(
            UpdateOwnProfileRequest(firstName = "Анна", lastName = "Иванова", middleName = "Петровна", avatarUrl = url),
            profileApi.updates.single(),
        )
        assertEquals(listOf("user-1/old.jpg"), storage.deleted)
    }

    @Test
    fun `token of another user stops before upload`() = runBlocking {
        tokens.token = testJwt("""{"sub":"user-2"}""")

        val result = repository.changeAvatar("content://photo")

        assertEquals(AvatarChangeResult.Failure(AvatarChangeError.SessionMismatch), result)
        assertTrue(storage.uploaded.isEmpty())
        assertTrue(profileApi.updates.isEmpty())
    }

    @Test
    fun `token without subject is a session mismatch`() = runBlocking {
        tokens.token = "not-a-jwt"

        assertEquals(
            AvatarChangeResult.Failure(AvatarChangeError.SessionMismatch),
            repository.changeAvatar("content://photo"),
        )
    }

    @Test
    fun `missing token is unauthorized`() = runBlocking {
        tokens.token = null

        assertEquals(
            AvatarChangeResult.Failure(AvatarChangeError.Data(DataError.Unauthorized)),
            repository.changeAvatar("content://photo"),
        )
    }

    @Test
    fun `profile load error is returned`() = runBlocking {
        profileApi.userError = IOException("offline")

        assertEquals(
            AvatarChangeResult.Failure(AvatarChangeError.Data(DataError.Network)),
            repository.changeAvatar("content://photo"),
        )
    }

    @Test
    fun `unreadable or too large image is not uploaded`() = runBlocking {
        image = null
        assertEquals(
            AvatarChangeResult.Failure(AvatarChangeError.ImageUnreadable),
            repository.changeAvatar("content://photo"),
        )

        image = ByteArray(MAX_AVATAR_BYTES + 1)
        assertEquals(
            AvatarChangeResult.Failure(AvatarChangeError.ImageTooLarge),
            repository.changeAvatar("content://photo"),
        )
        assertTrue(storage.uploaded.isEmpty())
    }

    @Test
    fun `storage refusal does not touch profile`() = runBlocking {
        storage.uploadError = ApiStatusException(403)
        assertEquals(
            AvatarChangeResult.Failure(AvatarChangeError.UploadRejected),
            repository.changeAvatar("content://photo"),
        )

        storage.uploadError = ApiStatusException(413)
        assertEquals(
            AvatarChangeResult.Failure(AvatarChangeError.ImageTooLarge),
            repository.changeAvatar("content://photo"),
        )

        storage.uploadError = ApiStatusException(503)
        assertEquals(
            AvatarChangeResult.Failure(AvatarChangeError.Data(DataError.Server)),
            repository.changeAvatar("content://photo"),
        )
        assertTrue(profileApi.updates.isEmpty())
    }

    @Test
    fun `failed profile update removes uploaded file`() = runBlocking {
        profileApi.updateError = ApiStatusException(500)

        val result = repository.changeAvatar("content://photo")

        assertEquals(AvatarChangeResult.Failure(AvatarChangeError.Data(DataError.Server)), result)
        assertEquals(listOf("user-1/new.jpg"), storage.deleted)
    }

    @Test
    fun `failed removal of old file is ignored`() = runBlocking {
        profileApi.user = profileApi.user.copy(avatarUrl = NetworkConfig.avatarPublicUrl("user-1/old.jpg"))
        storage.deleteError = IOException("offline")

        val result = repository.changeAvatar("content://photo")

        assertEquals(AvatarChangeResult.Success(NetworkConfig.avatarPublicUrl("user-1/new.jpg")), result)
    }

    @Test
    fun `old avatar outside our bucket folder is kept`() = runBlocking {
        profileApi.user = profileApi.user.copy(avatarUrl = "https://cdn.example.com/user-1/old.jpg")

        repository.changeAvatar("content://photo")

        assertTrue(storage.deleted.isEmpty())
    }

    @Test
    fun `avatar path is taken only from own folder in bucket`() {
        val base = NetworkConfig.AVATARS_PUBLIC_URL
        assertEquals("user-1/a.jpg", avatarPathOf("$base/user-1/a.jpg?t=1", "user-1"))
        assertNull(avatarPathOf("$base/user-2/a.jpg", "user-1"))
        assertNull(avatarPathOf("$base/user-1/../user-2/a.jpg", "user-1"))
        assertNull(avatarPathOf("$base/user-1/", "user-1"))
        assertNull(avatarPathOf(null, "user-1"))
    }
}

private class AvatarFakeProfileApi : ProfileApi {
    var user = CurrentUserProfileDto(
        id = "USER-1",
        firstName = "Анна",
        lastName = "Иванова",
        middleName = "Петровна",
    )
    var userError: Throwable? = null
    var updateError: Throwable? = null
    val updates = mutableListOf<UpdateOwnProfileRequest>()

    override suspend fun getCurrentUser(): CurrentUserProfileDto {
        userError?.let { throw it }
        return user
    }

    override suspend fun updateOwnProfile(request: UpdateOwnProfileRequest) {
        updateError?.let { throw it }
        updates += request
    }

    override suspend fun getBalance() = BalanceDto(amount = 0)
    override suspend fun getTransactions(skip: Int, take: Int) = emptyList<TransactionDto>()
    override suspend fun getUserAchievements(userId: String) = emptyList<UserAchievementDto>()
}

private class FakeAvatarStorageApi : AvatarStorageApi {
    val uploaded = mutableListOf<String>()
    val deleted = mutableListOf<String>()
    var lastToken: String? = null
    var uploadError: Throwable? = null
    var deleteError: Throwable? = null

    override suspend fun upload(path: String, jpeg: ByteArray, accessToken: String) {
        uploadError?.let { throw it }
        lastToken = accessToken
        uploaded += path
    }

    override suspend fun delete(path: String, accessToken: String) {
        deleteError?.let { throw it }
        deleted += path
    }
}

private class FakeTokenStorage(var token: String?) : TokenStorage {
    override suspend fun saveTokens(tokens: AuthTokens) = Unit
    override suspend fun getTokens(): AuthTokens? = null
    override suspend fun getAccessToken(): String? = token
    override suspend fun clearTokens() = Unit
}
