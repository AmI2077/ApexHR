package com.example.apexhr.features.profile.domain

import com.example.apexhr.core.domain.DataResult

interface ProfileRepository {
    /** Загружает профиль, баланс и достижения текущего пользователя. */
    suspend fun loadProfile(): DataResult<Profile>
}
