package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.ProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ProfileRepositoryImpl(
    private val api: ProfileApi,
) : ProfileRepository {

    override suspend fun loadProfile(): DataResult<Profile> = safeApiCall {
        coroutineScope {
            // Профиль, баланс и операции независимы — грузим параллельно.
            // Достижениям нужен id пользователя из профиля.
            val balance = async { api.getBalance() }
            val transactions = async { api.getTransactions(skip = 0, take = TRANSACTIONS_PAGE_SIZE) }
            val user = api.getCurrentUser().toDomain()
            val achievements = api.getUserAchievements(user.id)

            Profile(
                user = user,
                balance = balance.await().amount,
                transactions = transactions.await().map { it.toDomain() },
                achievements = achievements.map { it.toDomain() },
            )
        }
    }

    companion object {
        /** Профиль показывает последние операции одной страницей, остальные — экран истории. */
        const val TRANSACTIONS_PAGE_SIZE = 50
    }
}
