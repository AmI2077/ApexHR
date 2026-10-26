package com.example.apexhr.features.thanks.domain

import com.example.apexhr.core.domain.DataResult

interface ThanksRepository {
    /** Коллеги, полученные и отправленные благодарности. */
    suspend fun loadThanks(): DataResult<ThanksOverview>

    suspend fun sendThanks(toUserId: String, amount: Long, message: String?): ThanksSendResult
}
