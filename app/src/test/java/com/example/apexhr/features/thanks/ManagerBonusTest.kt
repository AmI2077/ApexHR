package com.example.apexhr.features.thanks

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.thanks.data.ThanksApi
import com.example.apexhr.features.thanks.data.ThanksApiException
import com.example.apexhr.features.thanks.data.ThanksRepositoryImpl
import com.example.apexhr.features.thanks.data.dto.CreateManagerBonusRequest
import com.example.apexhr.features.thanks.data.dto.CreateThankRequest
import com.example.apexhr.features.thanks.data.dto.ThankDto
import com.example.apexhr.features.thanks.data.dto.ThanksUserDto
import com.example.apexhr.features.thanks.data.dto.WorkBalanceDto
import com.example.apexhr.features.thanks.domain.AppRole
import com.example.apexhr.features.thanks.domain.ManagerBonusAccess
import com.example.apexhr.features.thanks.domain.appRoleOf
import com.example.apexhr.features.thanks.domain.canRewardFromWorkBalance
import com.example.apexhr.features.thanks.ui.ThanksViewModel
import com.example.apexhr.features.thanks.ui.models.ThanksScreenState
import com.example.apexhr.features.thanks.ui.models.ThanksUiState
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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

/** Поощрение руководителя: роли как в вебе, запросы и форма. Сеть не используется. */
@OptIn(ExperimentalCoroutinesApi::class)
class ManagerBonusTest {

    private val api = BonusFakeApi()
    private val repository = ThanksRepositoryImpl(api)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ThanksViewModel(repository) { Instant.parse("2026-11-18T12:00:00Z") }

    private fun ThanksViewModel.ui(): ThanksUiState = (state.value as ThanksScreenState.Content).uiState

    @Test
    fun `roles are mapped with the web precedence`() {
        assertEquals(AppRole.Manager, appRoleOf(listOf("manager")))
        assertEquals(AppRole.Manager, appRoleOf(listOf("employee", "TeamLead")))
        assertEquals(AppRole.Chief, appRoleOf(listOf("manager", "superadmin")))
        assertEquals(AppRole.Admin, appRoleOf(listOf("admin", "manager")))
        assertEquals(AppRole.Hr, appRoleOf(listOf("hr", "manager")))
        assertEquals(AppRole.Employee, appRoleOf(listOf("employee")))
        assertEquals(AppRole.Admin, appRoleOf(listOf("employee"), canAccessAdminPanel = true))
        assertEquals(listOf(AppRole.Manager), AppRole.entries.filter { it.canRewardFromWorkBalance })
    }

    @Test
    fun `employee has no manager bonus and no work balance request`() = runBlocking {
        api.roles = listOf("employee")

        assertEquals(DataResult.Success(ManagerBonusAccess.None), repository.loadManagerBonusAccess())
        assertEquals(0, api.workBalanceCalls)
        val vm = viewModel()
        assertFalse(vm.ui().canReward)
        vm.onManagerModeChange(true)
        assertFalse(vm.ui().isManagerMode)
    }

    @Test
    fun `manager sees work balance or no access`() = runBlocking {
        api.roles = listOf("manager")
        assertEquals(DataResult.Success(ManagerBonusAccess(true, 4_500)), repository.loadManagerBonusAccess())

        api.workBalanceError = ApiStatusException(403)
        assertEquals(DataResult.Success(ManagerBonusAccess(true, null)), repository.loadManagerBonusAccess())
    }

    @Test
    fun `manager bonus needs a colleague and an amount, message is optional`() {
        api.roles = listOf("manager")
        val vm = viewModel()

        vm.onManagerModeChange(true)
        assertTrue(vm.ui().isManagerMode)
        assertEquals(listOf(50L, 100L, 200L, 500L), vm.ui().bonusAmounts)
        vm.onColleagueClick("anna")
        assertFalse(vm.ui().canSend)
        vm.onBonusAmountClick(30)
        assertNull(vm.ui().selectedBonusAmount)
        vm.onBonusAmountClick(200)
        assertTrue(vm.ui().canSend)

        vm.onSendClick()

        assertEquals(CreateManagerBonusRequest(toUserId = "anna", amount = 200, message = null), api.bonusRequest)
        assertNull(api.thankRequest)
        val ui = vm.ui()
        assertEquals(4_300L, ui.workBalance)
        assertNull(ui.selectedBonusAmount)
        assertTrue(ui.isManagerMode)
        assertFalse(ui.notice!!.isError)
    }

    @Test
    fun `manager bonus is not limited by the weekly thanks rule and keeps the message`() {
        api.roles = listOf("manager")
        api.sent = listOf(ThankDto(id = 1, fromUserId = "me", toUserId = "anna", amount = 25, createdAt = "2026-11-17T10:00:00Z"))
        val vm = viewModel()
        vm.onColleagueClick("anna")
        assertFalse(vm.ui().canSend)

        vm.onManagerModeChange(true)
        vm.onBonusAmountClick(50)
        vm.onMessageChange("  За релиз ")
        vm.onSendClick()

        assertEquals(CreateManagerBonusRequest("anna", 50, "За релиз"), api.bonusRequest)
    }

    @Test
    fun `server rejection is shown and the form is kept`() {
        api.roles = listOf("manager")
        api.bonusError = ThanksApiException(400, "Недостаточно средств на рабочем балансе")
        val vm = viewModel()
        vm.onManagerModeChange(true)
        vm.onColleagueClick("anna")
        vm.onBonusAmountClick(500)

        vm.onSendClick()

        val ui = vm.ui()
        assertTrue(ui.notice!!.isError)
        assertEquals(listOf("Недостаточно средств на рабочем балансе"), ui.notice!!.args)
        assertEquals(500L, ui.selectedBonusAmount)
        assertEquals(4_500L, ui.workBalance)
    }

    private class BonusFakeApi : ThanksApi {
        var roles = listOf("employee")
        var sent = emptyList<ThankDto>()
        var workBalanceError: Throwable? = null
        var bonusError: Throwable? = null
        var workBalanceCalls = 0
        var bonusRequest: CreateManagerBonusRequest? = null
        var thankRequest: CreateThankRequest? = null

        override suspend fun getCurrentUser() = ThanksUserDto(id = "me", firstName = "Пётр", roles = roles)
        override suspend fun getUsers() = listOf(
            ThanksUserDto(id = "me", firstName = "Пётр"),
            ThanksUserDto(id = "anna", firstName = "Анна"),
        )
        override suspend fun getThanksForMe(): List<ThankDto> = emptyList()
        override suspend fun getThanksFromMe(): List<ThankDto> = sent
        override suspend fun sendThanks(request: CreateThankRequest) {
            thankRequest = request
        }
        override suspend fun getWorkBalance(): WorkBalanceDto {
            workBalanceCalls++
            workBalanceError?.let { throw it }
            return WorkBalanceDto(amount = 4_500)
        }
        override suspend fun sendManagerBonus(request: CreateManagerBonusRequest) {
            bonusError?.let { throw it }
            bonusRequest = request
        }
    }
}
