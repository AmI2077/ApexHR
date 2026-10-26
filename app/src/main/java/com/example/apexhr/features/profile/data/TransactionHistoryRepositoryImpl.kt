package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.domain.TransactionHistoryRepository
import com.example.apexhr.features.profile.domain.TransactionPage

class TransactionHistoryRepositoryImpl(
    private val api: ProfileApi,
) : TransactionHistoryRepository {

    override suspend fun loadPage(skip: Int): DataResult<TransactionPage> = safeApiCall {
        val transactions = api.getTransactions(skip = skip, take = PAGE_SIZE)
        TransactionPage(
            transactions = transactions.map { it.toHistoryDomain() },
            // Общего числа операций API не отдаёт: неполная страница — последняя.
            hasMore = transactions.size >= PAGE_SIZE,
        )
    }

    companion object {
        /** Операций за один запрос. */
        const val PAGE_SIZE = 50
    }
}
