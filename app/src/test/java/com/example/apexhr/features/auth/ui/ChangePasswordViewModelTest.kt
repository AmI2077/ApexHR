package com.example.apexhr.features.auth.ui

import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.auth.domain.ChangePasswordError
import com.example.apexhr.features.auth.domain.ChangePasswordResult
import com.example.apexhr.features.auth.domain.ConfirmPasswordError
import com.example.apexhr.features.auth.domain.LoginResult
import com.example.apexhr.features.auth.domain.NewPasswordError
import com.example.apexhr.features.auth.domain.SessionStatus
import com.example.apexhr.features.auth.ui.model.ChangePasswordUiActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    private val repository = FakeAuthRepository()
    private lateinit var viewModel: ChangePasswordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ChangePasswordViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun enter(newPassword: String, confirmPassword: String) {
        viewModel.onNewPasswordChange(newPassword)
        viewModel.onConfirmPasswordChange(confirmPassword)
        viewModel.onSubmitClick()
    }

    @Test
    fun `empty fields show errors and do not call repository`() {
        enter("", "")

        val state = viewModel.uiState.value
        assertEquals(NewPasswordError.Empty, state.newPasswordError)
        assertEquals(ConfirmPasswordError.Empty, state.confirmPasswordError)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `password shorter than six characters is rejected`() {
        enter("12345", "12345")

        assertEquals(NewPasswordError.TooShort, viewModel.uiState.value.newPasswordError)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `mismatched passwords are rejected`() {
        enter("123456", "123457")

        val state = viewModel.uiState.value
        assertNull(state.newPasswordError)
        assertEquals(ConfirmPasswordError.Mismatch, state.confirmPasswordError)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `editing a field clears its error`() {
        enter("123", "")

        viewModel.onNewPasswordChange("1234")
        viewModel.onConfirmPasswordChange("1")

        val state = viewModel.uiState.value
        assertNull(state.newPasswordError)
        assertNull(state.confirmPasswordError)
    }

    @Test
    fun `valid passwords call repository and emit success`() = runTest {
        enter("newpass1", "newpass1")

        assertEquals(listOf("newpass1"), repository.calls)
        assertEquals(ChangePasswordUiActions.Success, viewModel.actions.first())
        val state = viewModel.uiState.value
        assertEquals("", state.newPassword)
        assertFalse(state.isLoading)
    }

    @Test
    fun `server failure is shown and passwords are kept`() {
        val error = ChangePasswordError.Rejected("Пароль слишком простой")
        repository.result = ChangePasswordResult.Failure(error)

        enter("newpass1", "newpass1")

        val state = viewModel.uiState.value
        assertEquals(error, state.error)
        assertFalse(state.isLoading)
        assertEquals("newpass1", state.newPassword)
    }

    @Test
    fun `expired session resets state`() {
        repository.result = ChangePasswordResult.Failure(ChangePasswordError.SessionExpired)

        enter("newpass1", "newpass1")

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
    }

    private class FakeAuthRepository : AuthRepository {
        var result: ChangePasswordResult = ChangePasswordResult.Success
        val calls = mutableListOf<String>()

        override suspend fun changePassword(newPassword: String): ChangePasswordResult {
            calls += newPassword
            return result
        }

        override suspend fun login(email: String, password: String): LoginResult =
            throw UnsupportedOperationException()

        override suspend fun logout() = Unit

        override suspend fun checkSession(): SessionStatus =
            throw UnsupportedOperationException()
    }
}