package com.example.apexhr.features.thanks.domain

/**
 * Роль пользователя так, как её определяет веб (mapRole): по кодам ролей из
 * GET /api/users/me с приоритетом от главного администратора к руководителю.
 */
enum class AppRole { Chief, Admin, Hr, Merchandiser, Support, Manager, Employee }

fun appRoleOf(codes: List<String>, canAccessAdminPanel: Boolean = false): AppRole {
    val folded = codes.map { it.trim().lowercase() }
    fun has(pattern: String): Boolean {
        val regex = Regex(pattern)
        return folded.any { regex.containsMatchIn(it) }
    }
    return when {
        has("superadmin|super-admin|chief|owner") -> AppRole.Chief
        has("^admin$|administrator") -> AppRole.Admin
        has("humanresources|^hr$") -> AppRole.Hr
        has("merchandiser|^merch$") -> AppRole.Merchandiser
        has("supportoperator|support[-_ ]?operator|^support$|^operator$|helpdesk") -> AppRole.Support
        has("manager|lead|supervisor|руковод") -> AppRole.Manager
        canAccessAdminPanel -> AppRole.Admin
        else -> AppRole.Employee
    }
}

/**
 * Поощрять с рабочего баланса может только руководитель — как hasWorkBalance в вебе
 * (сервер пускает ещё superadmin, но веб ему эту форму не показывает).
 */
val AppRole.canRewardFromWorkBalance: Boolean
    get() = this == AppRole.Manager

/** Суммы поощрения руководителя, как awardAmounts в вебе. */
val MANAGER_BONUS_AMOUNTS: List<Long> = listOf(50L, 100L, 200L, 500L)

/**
 * Доступ к поощрению руководителя.
 * @param workBalance рабочий баланс; null — не удалось получить («нет доступа»).
 */
data class ManagerBonusAccess(
    val canReward: Boolean,
    val workBalance: Long?,
) {
    companion object {
        val None = ManagerBonusAccess(canReward = false, workBalance = null)
    }
}
