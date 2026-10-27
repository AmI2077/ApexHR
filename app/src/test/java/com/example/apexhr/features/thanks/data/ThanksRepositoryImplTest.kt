package com.example.apexhr.features.thanks.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.thanks.data.dto.CreateThankRequest
import com.example.apexhr.features.thanks.data.dto.ThankDto
import com.example.apexhr.features.thanks.data.dto.ThanksUserDto
import com.example.apexhr.features.thanks.domain.ThanksSendError
import com.example.apexhr.features.thanks.domain.ThanksSendResult
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class ThanksRepositoryImplTest {

    private val api = FakeThanksApi()
    private val repository = ThanksRepositoryImpl(api)

    @Test
    fun `colleagues exclude current user and inactive users`() = runBlocking {
        val colleagues = (repository.loadColleagues() as DataResult.Success).data

        assertEquals(listOf(ANNA, IVAN), colleagues.map { it.id })
    }

    @Test
    fun `thanks names are resolved from users`() = runBlocking {
        val overview = (repository.loadHistory() as DataResult.Success).data

        assertEquals(listOf("Анна Иванова", "Ольга Уволена"), overview.received.map { it.counterpartName })
        assertEquals(listOf("Иван Петров"), overview.sent.map { it.counterpartName })
    }

    @Test
    fun `thank from unknown user has no name`() = runBlocking {
        api.received = listOf(ThankDto(id = 9, fromUserId = UNKNOWN, fromUserName = UNKNOWN, toUserId = ME))

        val overview = (repository.loadHistory() as DataResult.Success).data

        assertEquals(UNKNOWN, overview.received.single().counterpartId)
        assertNull(overview.received.single().counterpartName)
    }

    @Test
    fun `failure of any request fails loading`() = runBlocking {
        api.usersError = ApiStatusException(500)

        assertEquals(DataResult.Failure(DataError.Server), repository.loadHistory())
        assertEquals(DataResult.Failure(DataError.Server), repository.loadColleagues())
    }

    @Test
    fun `network error is mapped`() = runBlocking {
        api.sentError = UnknownHostException("no network")

        assertEquals(DataResult.Failure(DataError.Network), repository.loadHistory())
    }

    @Test
    fun `colleagues do not request thanks`() = runBlocking {
        api.sentError = UnknownHostException("no network")

        assertTrue(repository.loadColleagues() is DataResult.Success)
    }

    @Test
    fun `successful send notifies subscribers`() = runBlocking {
        val event = async(start = CoroutineStart.UNDISPATCHED) { repository.thanksSent.first() }

        repository.sendThanks(toUserId = ANNA, message = "Спасибо")

        assertEquals(Unit, withTimeoutOrNull(1_000) { event.await() })
    }

    @Test
    fun `failed send does not notify`() = runBlocking {
        api.sendError = ThanksApiException(400, "Лимит")
        val event = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeoutOrNull(200) { repository.thanksSent.first() }
        }

        repository.sendThanks(toUserId = ANNA, message = "Спасибо")

        assertNull(event.await())
    }

    @Test
    fun `send passes recipient fixed amount and trimmed message`() = runBlocking {
        val result = repository.sendThanks(toUserId = ANNA, message = "  Спасибо!  ")

        assertEquals(ThanksSendResult.Success, result)
        assertEquals(
            CreateThankRequest(toUserId = ANNA, amount = 1, message = "Спасибо!", isPublic = true),
            api.sentRequest,
        )
    }

    @Test
    fun `blank message is not sent`() = runBlocking {
        repository.sendThanks(toUserId = ANNA, message = "   ")

        assertNull(api.sentRequest?.message)
    }

    @Test
    fun `weekly limit message is returned`() = runBlocking {
        api.sendError = ThanksApiException(400, "Превышен недельный лимит благодарностей")

        assertEquals(
            ThanksSendResult.Failure(ThanksSendError.Rejected("Превышен недельный лимит благодарностей")),
            repository.sendThanks(toUserId = ANNA, message = "Спасибо"),
        )
    }

    @Test
    fun `send network error is data error`() = runBlocking {
        api.sendError = UnknownHostException("no network")

        assertEquals(
            ThanksSendResult.Failure(ThanksSendError.Data(DataError.Network)),
            repository.sendThanks(toUserId = ANNA, message = "Спасибо"),
        )
    }

    private companion object {
        const val ME = "d5b02405-82d2-4607-80b6-b6e67d844349"
        const val ANNA = "5b850be7-abe7-4a45-91a6-53dceeed040f"
        const val IVAN = "e60846d7-de92-4214-928a-54423310a083"
        const val OLGA = "0c1d2e3f-0000-4000-8000-000000000001"
        const val UNKNOWN = "11111111-2222-3333-4444-555555555555"
    }

    private class FakeThanksApi : ThanksApi {
        var usersError: Throwable? = null
        var sentError: Throwable? = null
        var sendError: Throwable? = null

        var received = listOf(
            ThankDto(id = 2, fromUserId = ANNA, fromUserName = ANNA, toUserId = ME, toUserName = ME, amount = 10),
            ThankDto(id = 1, fromUserId = OLGA, fromUserName = OLGA, toUserId = ME, toUserName = ME, amount = 20),
        )
        var sentRequest: CreateThankRequest? = null

        override suspend fun getCurrentUser() = ThanksUserDto(id = ME, firstName = "Пётр")

        override suspend fun getUsers(): List<ThanksUserDto> {
            usersError?.let { throw it }
            return listOf(
                ThanksUserDto(id = ME, firstName = "Пётр", lastName = "Сидоров"),
                ThanksUserDto(id = ANNA, firstName = "Анна", lastName = "Иванова"),
                ThanksUserDto(id = OLGA, firstName = "Ольга", lastName = "Уволена", isActive = false),
                ThanksUserDto(id = IVAN, firstName = "Иван", lastName = "Петров"),
            )
        }

        override suspend fun getThanksForMe(): List<ThankDto> = received

        override suspend fun getThanksFromMe(): List<ThankDto> {
            sentError?.let { throw it }
            return listOf(ThankDto(id = 3, fromUserId = ME, fromUserName = ME, toUserId = IVAN, toUserName = IVAN, amount = 30))
        }

        override suspend fun sendThanks(request: CreateThankRequest) {
            sendError?.let { throw it }
            sentRequest = request
        }
    }
}
