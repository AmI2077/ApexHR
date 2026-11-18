package com.example.apexhr.features.colleague.data

import com.example.apexhr.features.colleague.data.dto.ColleagueLeaderboardEntryDto
import com.example.apexhr.features.colleague.domain.ColleagueThank
import com.example.apexhr.features.colleague.domain.UserRole
import com.example.apexhr.features.thanks.domain.Thank

/**
 * Роль по кодам ролей с сервера — в том же порядке, что и в вебе: главный
 * администратор, администратор, HR, мерчендайзер, поддержка, руководитель.
 * Без подходящего кода, но с доступом к админке — администратор, иначе сотрудник.
 */
fun userRoleOf(codes: List<String>?, canAccessAdminPanel: Boolean): UserRole {
    val folded = codes.orEmpty().map { it.trim().lowercase() }
    fun has(pattern: Regex) = folded.any { pattern.containsMatchIn(it) }
    return when {
        has(Regex("superadmin|super-admin|chief|owner")) -> UserRole.Chief
        has(Regex("^admin$|administrator")) -> UserRole.Admin
        has(Regex("humanresources|^hr$")) -> UserRole.Hr
        has(Regex("merchandiser|^merch$")) -> UserRole.Merchandiser
        has(Regex("supportoperator|support[-_ ]?operator|^support$|^operator$|helpdesk")) -> UserRole.Support
        has(Regex("manager|lead|supervisor|руковод")) -> UserRole.Manager
        canAccessAdminPanel -> UserRole.Admin
        else -> UserRole.Employee
    }
}

/**
 * Благодарности сотрудника в рейтинге. Если его нет в ответе, а ответ меньше
 * лимита, — у него 0; если ответ упёрся в лимит, — неизвестно (null).
 */
fun List<ColleagueLeaderboardEntryDto>.thanksScoreOf(userId: String, limit: Int): Long? =
    firstOrNull { it.userId.equals(userId, ignoreCase = true) }?.thanksCount?.coerceAtLeast(0)
        ?: if (size < limit) 0L else null

fun Thank.toColleagueThank(fromMe: Boolean): ColleagueThank = ColleagueThank(
    id = id,
    fromMe = fromMe,
    fromName = if (fromMe) null else counterpartName,
    amount = amount,
    message = message,
    createdAt = createdAt,
)