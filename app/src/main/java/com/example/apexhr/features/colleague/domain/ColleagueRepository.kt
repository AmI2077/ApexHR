package com.example.apexhr.features.colleague.domain

import com.example.apexhr.core.domain.DataResult

interface ColleagueRepository {
    /**
     * Профиль сотрудника по id. Ошибкой считается только недоступный профиль:
     * без рейтинга, благодарностей или достижений экран всё равно показывается.
     */
    suspend fun loadColleague(userId: String): DataResult<ColleagueProfile>
}