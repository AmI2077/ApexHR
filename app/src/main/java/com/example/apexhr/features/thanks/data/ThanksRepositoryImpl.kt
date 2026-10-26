package com.example.apexhr.features.thanks.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.thanks.data.dto.CreateThankRequest
import com.example.apexhr.features.thanks.domain.ThanksOverview
import com.example.apexhr.features.thanks.domain.ThanksRepository
import com.example.apexhr.features.thanks.domain.ThanksSendResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ThanksRepositoryImpl(
    private val api: ThanksApi,
) : ThanksRepository {

    override suspend fun loadThanks(): DataResult<ThanksOverview> = safeApiCall {
        coroutineScope {
            val me = async { api.getCurrentUser() }
            val users = async { api.getUsers() }
            val received = async { api.getThanksForMe() }
            val sent = async { api.getThanksFromMe() }

            // Имена берём у всех сотрудников, включая неактивных и себя:
            // благодарности могли быть от/для них.
            val allUsers = users.await()
            val namesById = allUsers
                .mapNotNull { user -> user.displayName()?.let { user.id to it } }
                .toMap()
            val myId = me.await().id

            ThanksOverview(
                colleagues = allUsers
                    .filter { it.isActive && !it.id.equals(myId, ignoreCase = true) }
                    .map { it.toColleague() },
                received = received.await().map { it.toReceived(namesById) },
                sent = sent.await().map { it.toSent(namesById) },
            )
        }
    }

    override suspend fun sendThanks(
        toUserId: String,
        amount: Long,
        message: String?,
    ): ThanksSendResult = safeThanksAction {
        api.sendThanks(
            CreateThankRequest(
                toUserId = toUserId,
                amount = amount,
                message = message?.trim()?.takeIf { it.isNotEmpty() },
                isPublic = true,
            )
        )
    }.fold(
        onSuccess = { ThanksSendResult.Success },
        onFailure = { ThanksSendResult.Failure(it.toThanksSendError()) },
    )
}
