package com.example.apexhr.features.colleague.domain

import com.example.apexhr.features.profile.domain.EarnedAchievement
import java.time.Instant

/** Роль сотрудника, как её показывает веб. */
enum class UserRole {
    Employee,
    Manager,
    Hr,
    Merchandiser,
    Support,
    Admin,
    Chief,
}

/** Благодарность коллеге из ленты текущего пользователя. */
data class ColleagueThank(
    val id: Long,
    /** Отправил текущий пользователь. */
    val fromMe: Boolean,
    val fromName: String?,
    val amount: Long,
    val message: String?,
    val createdAt: Instant?,
)

/**
 * Профиль коллеги.
 * @param thanksScore благодарности в рейтинге за [LEADERBOARD_DAYS] дней;
 * null, если рейтинг не загрузился или коллега в него не попал из-за лимита.
 * @param receivedThanks благодарности коллеге из ленты текущего пользователя
 * (свои полученные, если это собственный профиль), новые сверху.
 */
data class ColleagueProfile(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUrl: String?,
    val position: String?,
    val department: String?,
    val role: UserRole,
    val isActive: Boolean,
    val isMe: Boolean,
    val thanksScore: Long?,
    val receivedThanks: List<ColleagueThank>,
    val achievements: List<EarnedAchievement>,
) {
    companion object {
        /** Период рейтинга, как на главной. */
        const val LEADERBOARD_DAYS = 30
    }
}