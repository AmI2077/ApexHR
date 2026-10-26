package com.example.apexhr.features.home.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.home.domain.HomeData
import com.example.apexhr.features.home.domain.HomeRepository
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.profile.data.toDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Главная собирает данные из шести запросов. Пользователь, баланс
 * и достижения — те же запросы, что у профиля, поэтому берутся из ProfileApi.
 */
class HomeRepositoryImpl(
    private val homeApi: HomeApi,
    private val profileApi: ProfileApi,
) : HomeRepository {

    override suspend fun loadHome(): DataResult<HomeData> = safeApiCall {
        coroutineScope {
            val balance = async { profileApi.getBalance() }
            val quests = async { homeApi.getQuests(page = 1, pageSize = QUESTS_PAGE_SIZE) }
            val users = async { homeApi.getUsers() }
            val leaderboard = async {
                // У лидерборда на сервере есть только limit (по умолчанию 10), поэтому
                // просим не меньше, чем всего сотрудников, — иначе сотрудник с
                // благодарностями за пределами топа попал бы в список с нулём.
                val limit = maxOf(users.await().size, LEADERBOARD_MIN_LIMIT)
                homeApi.getLeaderboard(days = LEADERBOARD_DAYS, limit = limit)
            }
            // Достижениям нужен id пользователя из профиля.
            val user = profileApi.getCurrentUser().toDomain()
            val achievements = profileApi.getUserAchievements(user.id)

            HomeData(
                user = user,
                balance = balance.await().amount,
                quests = quests.await().items
                    .filter { it.isActiveForUser() }
                    .take(HOME_QUESTS_LIMIT)
                    .map { it.toDomain() },
                leaderboard = mergeLeaderboard(
                    entries = leaderboard.await().map { it.toDomain() },
                    users = users.await(),
                ),
                achievements = achievements
                    .take(HOME_ACHIEVEMENTS_LIMIT)
                    .map { it.toDomain() },
            )
        }
    }

    companion object {
        /** Берём с запасом: завершённые квесты отфильтровываются. */
        const val QUESTS_PAGE_SIZE = 20
        const val HOME_QUESTS_LIMIT = 3

        /** Период рейтинга (как значение по умолчанию на сервере). */
        const val LEADERBOARD_DAYS = 30

        /** Нижняя граница limit для лидерборда (значение по умолчанию на сервере). */
        const val LEADERBOARD_MIN_LIMIT = 10

        /** В ряд на главной помещаются три карточки. */
        const val HOME_ACHIEVEMENTS_LIMIT = 3
    }
}
