package com.example.apexhr.features.home.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.engagement.data.EngagementApi
import com.example.apexhr.features.engagement.data.loadActivityLevelOrNull
import com.example.apexhr.features.home.domain.HomeData
import com.example.apexhr.features.home.domain.HomeRepository
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.profile.data.toDomain
import com.example.apexhr.features.news.data.NewsApi
import com.example.apexhr.features.news.data.toDomain
import com.example.apexhr.features.thanks.data.ThanksApi
import com.example.apexhr.features.news.domain.NewsRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Главная собирает данные из двенадцати запросов (уровень активности, новости и лента
 * благодарностей необязательные). Пользователь, баланс
 * и полученные достижения — те же запросы, что у профиля, поэтому берутся
 * из ProfileApi; каталог достижений, квесты, лидерборд и сотрудники — из HomeApi.
 */
class HomeRepositoryImpl(
    private val homeApi: HomeApi,
    private val profileApi: ProfileApi,
    private val engagementApi: EngagementApi,
    private val newsApi: NewsApi,
    private val thanksApi: ThanksApi,
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
                // Сервер принимает limit только от 1 до 100, на большее отвечает 400.
                val limit = maxOf(users.await().size, LEADERBOARD_MIN_LIMIT).coerceAtMost(LEADERBOARD_MAX_LIMIT)
                homeApi.getLeaderboard(days = LEADERBOARD_DAYS, limit = limit)
            }
            val achievementsCatalog = async { homeApi.getAchievements() }
            val activityLevel = async { engagementApi.loadActivityLevelOrNull() }
            val news = async {
                optionalList {
                    newsApi.getNews(page = 1, pageSize = NewsRepository.HOME_NEWS_COUNT).toDomain(requestedPage = 1).items
                }
            }
            // Лента благодарностей необязательная, как в вебе: источник, который не
            // загрузился, просто не попадает в ленту.
            val received = async { optionalList { thanksApi.getThanksForMe() } }
            val sent = async { optionalList { thanksApi.getThanksFromMe() } }
            val transactions = async { optionalList { profileApi.getTransactions(skip = 0, take = FEED_TRANSACTIONS_TAKE) } }
            // Полученным достижениям нужен id пользователя из профиля (GET /api/users/me).
            val user = profileApi.getCurrentUser().toDomain()
            val earnedAchievements = profileApi.getUserAchievements(user.id)

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
                achievements = mergeAchievements(
                    catalog = achievementsCatalog.await(),
                    earned = earnedAchievements,
                ),
                activityLevel = activityLevel.await(),
                news = news.await(),
                feed = buildHomeFeed(
                    me = user,
                    users = users.await(),
                    received = received.await(),
                    sent = sent.await(),
                    transactions = transactions.await(),
                ),
            )
        }
    }

    /** Необязательный блок главной (новости, лента): ошибка его просто скрывает. */
    private suspend fun <T> optionalList(block: suspend () -> List<T>): List<T> = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        emptyList()
    }

    companion object {
        /** Берём с запасом: завершённые квесты отфильтровываются. */
        const val QUESTS_PAGE_SIZE = 20
        const val HOME_QUESTS_LIMIT = 3

        /** Поощрения руководителя для ленты ищутся в последних 50 операциях, как в вебе. */
        const val FEED_TRANSACTIONS_TAKE = 50

        /** Период рейтинга (как значение по умолчанию на сервере). */
        const val LEADERBOARD_DAYS = 30

        /** Нижняя граница limit для лидерборда (значение по умолчанию на сервере). */
        const val LEADERBOARD_MIN_LIMIT = 10

        /** Верхняя граница limit на сервере (на 101 и больше — 400). */
        const val LEADERBOARD_MAX_LIMIT = 100

    }
}
