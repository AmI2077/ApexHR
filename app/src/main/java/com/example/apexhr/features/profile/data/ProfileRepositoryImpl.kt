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
            // Баланс не зависит от профиля — грузим параллельно.
            // Достижениям нужен id пользователя из профиля.
            val balance = async { api.getBalance() }
            val user = api.getCurrentUser().toDomain()
            val achievements = api.getUserAchievements(user.id)

            Profile(
                user = user,
                balance = balance.await().amount,
                achievements = achievements.map { it.toDomain() },
            )
        }
    }
}
