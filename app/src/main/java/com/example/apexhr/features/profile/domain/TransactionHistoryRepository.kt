package com.example.apexhr.features.profile.domain

import com.example.apexhr.core.domain.DataResult

/** Полная история операций по балансу, постранично. */
interface TransactionHistoryRepository {
    /** @param skip сколько операций уже загружено (сервер отдаёт от новых к старым). */
    suspend fun loadPage(skip: Int): DataResult<TransactionPage>
}
