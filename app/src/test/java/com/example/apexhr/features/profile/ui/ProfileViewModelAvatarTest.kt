package com.example.apexhr.features.profile.ui

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.domain.AvatarChangeError
import com.example.apexhr.features.profile.domain.AvatarChangeResult
import com.example.apexhr.features.profile.domain.AvatarRepository
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.ProfileRepository
import com.example.apexhr.features.profile.domain.TransactionHistoryRepository
import com.example.apexhr.features.profile.domain.TransactionPage
import com.example.apexhr.features.profile.domain.UserProfile
import com.example.apexhr.features.profile.ui.models.ProfileScreenState
import com.example.apexhr.features.profile.ui.models.ProfileUiState
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.ThanksHistory
import com.example.apexhr.features.thanks.domain.ManagerBonusAccess
import com.example.apexhr.features.thanks.domain.ThanksRepository
import com.example.apexhr.features.thanks.domain.ThanksSendResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelAvatarTest {

    private val profiles = FakeProfileRepository()
    private val avatars = FakeAvatarRepository()
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ProfileViewModel(profiles, FakeThanksRepository(), FakeHistoryRepository(), avatars)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun content(): ProfileUiState =
        (viewModel.state.value as ProfileScreenState.Content).uiState

    @Test
    fun `successful upload shows progress and then reloaded photo`() {
        val pending = CompletableDeferred<AvatarChangeResult>()
        avatars.next = pending

        viewModel.onAvatarPicked("content://photo")
        assertTrue(content().isAvatarUploading)

        profiles.avatarUrl = "https://example.com/new.jpg"
        pending.complete(AvatarChangeResult.Success("https://example.com/new.jpg"))

        assertFalse(content().isAvatarUploading)
        assertNull(content().avatarErrorRes)
        assertEquals("https://example.com/new.jpg", content().avatarUrl)
        assertEquals(listOf("content://photo"), avatars.calls)
        assertEquals(2, profiles.loads)
    }

    @Test
    fun `failure shows message and keeps profile`() {
        avatars.next = CompletableDeferred(AvatarChangeResult.Failure(AvatarChangeError.SessionMismatch))

        viewModel.onAvatarPicked("content://photo")

        assertFalse(content().isAvatarUploading)
        assertEquals(R.string.profile_avatar_error_session, content().avatarErrorRes)
        assertEquals(1, profiles.loads)
    }

    @Test
    fun `second photo is ignored while uploading and error is cleared on new try`() {
        avatars.next = CompletableDeferred(AvatarChangeResult.Failure(AvatarChangeError.ImageUnreadable))
        viewModel.onAvatarPicked("content://first")
        assertEquals(R.string.profile_avatar_error_image, content().avatarErrorRes)

        val pending = CompletableDeferred<AvatarChangeResult>()
        avatars.next = pending
        viewModel.onAvatarPicked("content://second")
        assertNull(content().avatarErrorRes)
        viewModel.onAvatarPicked("content://third")

        assertEquals(listOf("content://first", "content://second"), avatars.calls)
        pending.complete(AvatarChangeResult.Success("https://example.com/a.jpg"))
        assertFalse(content().isAvatarUploading)
    }
}

private class FakeProfileRepository : ProfileRepository {
    var loads = 0
    var avatarUrl: String? = null

    override suspend fun loadProfile(): DataResult<Profile> {
        loads++
        return DataResult.Success(
            Profile(
                user = UserProfile(
                    id = "user-1",
                    firstName = "Анна",
                    lastName = "Иванова",
                    middleName = null,
                    avatarUrl = avatarUrl,
                    department = null,
                    position = null,
                ),
                balance = 0,
                achievements = emptyList(),
            )
        )
    }
}

private class FakeAvatarRepository : AvatarRepository {
    val calls = mutableListOf<String>()
    var next = CompletableDeferred<AvatarChangeResult>()

    override suspend fun changeAvatar(imageUri: String): AvatarChangeResult {
        calls += imageUri
        return next.await()
    }
}

private class FakeThanksRepository : ThanksRepository {
    override suspend fun loadColleagues(): DataResult<List<Colleague>> = DataResult.Success(emptyList())
    override suspend fun loadHistory(): DataResult<ThanksHistory> =
        DataResult.Success(ThanksHistory(received = emptyList(), sent = emptyList()))
    override suspend fun sendThanks(toUserId: String, message: String): ThanksSendResult =
        throw UnsupportedOperationException()
    override suspend fun loadManagerBonusAccess(): DataResult<ManagerBonusAccess> =
        throw UnsupportedOperationException()
    override suspend fun sendManagerBonus(toUserId: String, amount: Long, message: String): ThanksSendResult =
        throw UnsupportedOperationException()
    override val thanksSent: Flow<Unit> = emptyFlow()
}

private class FakeHistoryRepository : TransactionHistoryRepository {
    override suspend fun loadPage(skip: Int): DataResult<TransactionPage> =
        throw UnsupportedOperationException()
}
