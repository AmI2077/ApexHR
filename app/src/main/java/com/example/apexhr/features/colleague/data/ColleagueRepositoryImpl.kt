package com.example.apexhr.features.colleague.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.colleague.domain.ColleagueProfile
import com.example.apexhr.features.colleague.domain.ColleagueRepository
import com.example.apexhr.features.colleague.domain.ColleagueThank
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.profile.data.jwtSubject
import com.example.apexhr.features.profile.data.toDomain
import com.example.apexhr.features.thanks.domain.ThanksHistory
import com.example.apexhr.features.thanks.domain.ThanksRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ColleagueRepositoryImpl(
    private val api: ColleagueApi,
    private val profileApi: ProfileApi,
    private val thanksRepository: ThanksRepository,
    private val tokenStorage: TokenStorage,
) : ColleagueRepository {

    override suspend fun loadColleague(userId: String): DataResult<ColleagueProfile> = safeApiCall {
        coroutineScope {
            // Рейтинг, достижения и лента необязательны: при ошибке экран
            // показывает профиль без них, как веб.
            val leaderboard = async {
                safeApiCall { api.getLeaderboard(ColleagueProfile.LEADERBOARD_DAYS, LEADERBOARD_LIMIT) }
            }
            val achievements = async { safeApiCall { profileApi.getUserAchievements(userId) } }
            val history = async { thanksRepository.loadHistory() }
            val user = api.getUser(userId)

            val myId = tokenStorage.getAccessToken()?.let(::jwtSubject)
            val isMe = myId != null && myId.equals(user.id, ignoreCase = true)

            ColleagueProfile(
                id = user.id,
                firstName = user.firstName?.trim()?.takeIf { it.isNotEmpty() },
                lastName = user.lastName?.trim()?.takeIf { it.isNotEmpty() },
                avatarUrl = user.avatarUrl,
                position = user.position,
                department = user.department,
                role = userRoleOf(user.roles, user.canAccessAdminPanel),
                isActive = user.isActive,
                isMe = isMe,
                thanksScore = (leaderboard.await() as? DataResult.Success)?.data
                    ?.thanksScoreOf(user.id, LEADERBOARD_LIMIT),
                receivedThanks = receivedThanks(history.await(), user.id, isMe),
                achievements = (achievements.await() as? DataResult.Success)?.data
                    ?.map { it.toDomain() }
                    .orEmpty(),
            )
        }
    }

    /** Как в вебе: из своей ленты — что получил я сам или что я отправил коллеге. */
    private fun receivedThanks(
        history: DataResult<ThanksHistory>,
        userId: String,
        isMe: Boolean,
    ): List<ColleagueThank> {
        val data = (history as? DataResult.Success)?.data ?: return emptyList()
        val thanks = if (isMe) {
            data.received.map { it.toColleagueThank(fromMe = false) }
        } else {
            data.sent
                .filter { it.counterpartId.equals(userId, ignoreCase = true) }
                .map { it.toColleagueThank(fromMe = true) }
        }
        return thanks
            .sortedByDescending { it.createdAt }
            .take(RECEIVED_THANKS_LIMIT)
    }

    companion object {
        /**
         * Сколько строк рейтинга запрашиваем, чтобы найти в нём коллегу:
         * сервер принимает limit от 1 до 100, на большее отвечает 400.
         */
        const val LEADERBOARD_LIMIT = 100

        /** Сколько благодарностей показываем, как в вебе. */
        const val RECEIVED_THANKS_LIMIT = 8
    }
}