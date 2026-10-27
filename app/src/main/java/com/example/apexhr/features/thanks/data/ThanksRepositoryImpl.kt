package com.example.apexhr.features.thanks.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.thanks.data.dto.CreateThankRequest
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.ThanksHistory
import com.example.apexhr.features.thanks.domain.ThanksRepository
import com.example.apexhr.features.thanks.domain.ThanksSendResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Сумма благодарности: в вебе она фиксированная и ничего не стоит отправителю. */
const val THANKS_AMOUNT = 1L

class ThanksRepositoryImpl(
    private val api: ThanksApi,
) : ThanksRepository {

    // Буфер, чтобы tryEmit не терялся, пока подписчик занят.
    private val _thanksSent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val thanksSent: Flow<Unit> = _thanksSent.asSharedFlow()

    override suspend fun loadColleagues(): DataResult<List<Colleague>> = safeApiCall {
        coroutineScope {
            val me = async { api.getCurrentUser() }
            val users = api.getUsers()
            val myId = me.await().id
            users
                .filter { it.isActive && !it.id.equals(myId, ignoreCase = true) }
                .map { it.toColleague() }
        }
    }

    override suspend fun loadHistory(): DataResult<ThanksHistory> = safeApiCall {
        coroutineScope {
            val users = async { api.getUsers() }
            val received = async { api.getThanksForMe() }
            val sent = async { api.getThanksFromMe() }

            // Имена берём у всех сотрудников, включая неактивных и себя:
            // благодарности могли быть от/для них.
            val namesById = users.await()
                .mapNotNull { user -> user.displayName()?.let { user.id to it } }
                .toMap()

            ThanksHistory(
                received = received.await().map { it.toReceived(namesById) },
                sent = sent.await().map { it.toSent(namesById) },
            )
        }
    }

    override suspend fun sendThanks(
        toUserId: String,
        message: String,
    ): ThanksSendResult = safeThanksAction {
        api.sendThanks(
            CreateThankRequest(
                toUserId = toUserId,
                amount = THANKS_AMOUNT,
                message = message.trim().takeIf { it.isNotEmpty() },
                isPublic = true,
            )
        )
    }.fold(
        onSuccess = {
            _thanksSent.tryEmit(Unit)
            ThanksSendResult.Success
        },
        onFailure = { ThanksSendResult.Failure(it.toThanksSendError()) },
    )
}
