package com.example.apexhr.features.home.data

import com.example.apexhr.features.home.data.dto.HomeAchievementDto
import com.example.apexhr.features.home.data.dto.HomeUserDto
import com.example.apexhr.features.home.data.dto.LeaderboardEntryDto
import com.example.apexhr.features.home.data.dto.QuestListItemDto
import com.example.apexhr.features.home.domain.HomeAchievement
import com.example.apexhr.features.home.domain.LeaderboardEntry
import com.example.apexhr.features.home.domain.Quest
import com.example.apexhr.features.profile.data.dto.UserAchievementDto

fun QuestListItemDto.toDomain() = Quest(
    id = id,
    title = title.cleaned(),
    rewardAmount = rewardAmount,
)

fun LeaderboardEntryDto.toDomain() = LeaderboardEntry(
    rank = rank,
    userId = userId.cleaned(),
    name = name.cleaned(),
    thanksCount = thanksCount,
)

/** «Имя Фамилия» сотрудника; null, если нет ни того, ни другого. */
internal fun HomeUserDto.displayName(): String? =
    listOfNotNull(firstName.cleaned(), lastName.cleaned())
        .joinToString(" ")
        .takeIf { it.isNotEmpty() }

/**
 * Полный лидерборд: строки с сервера плюс активные сотрудники, которых там нет,
 * с нулём благодарностей.
 *
 * - Сотрудник, уже попавший в ответ лидерборда, второй раз не добавляется
 *   (id сравниваются без учёта регистра); повторы в списке сотрудников тоже отбрасываются.
 * - Неактивные и сотрудники без id не добавляются.
 * - Если сервер не прислал имя, оно берётся из списка сотрудников.
 * - Порядок: по убыванию благодарностей; при равенстве — как прислал сервер
 *   (по месту), добавленные нулевые — в конце, по имени.
 * - Места пересчитываются подряд: 1, 2, 3…
 */
internal fun mergeLeaderboard(
    entries: List<LeaderboardEntry>,
    users: List<HomeUserDto>,
): List<LeaderboardEntry> {
    val namesById = users
        .mapNotNull { user -> user.id.cleaned()?.let { id -> user.displayName()?.let { id.lowercase() to it } } }
        .toMap()
    val ranked = entries
        .sortedBy { it.rank }
        .map { entry ->
            if (entry.name != null) entry
            else entry.copy(name = entry.userId?.let { namesById[it.lowercase()] })
        }
    val rankedIds = ranked.mapNotNull { it.userId?.lowercase() }.toSet()
    val withoutThanks = users
        .filter { it.isActive }
        .mapNotNull { user -> user.id.cleaned()?.let { id -> id to user } }
        .distinctBy { (id, _) -> id.lowercase() }
        .filter { (id, _) -> id.lowercase() !in rankedIds }
        .map { (id, user) ->
            LeaderboardEntry(rank = 0, userId = id, name = user.displayName(), thanksCount = 0)
        }
        .sortedWith(compareBy(nullsLast<String>()) { it.name })
    return (ranked + withoutThanks)
        .sortedByDescending { it.thanksCount }
        .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
}

/**
 * Все достижения для главной: каталог плюс отметка, получено ли достижение.
 *
 * - Полученное определяется по achievementId (если его нет — по id
 *   вложенного achievement) из GET /api/achievements/users/{userId}.
 * - Неактивные достижения каталога показываются, только если уже получены:
 *   получить неактивное нельзя.
 * - Полученное, которого почему-то нет в каталоге, всё равно показывается —
 *   с названием из ответа о полученных.
 * - Порядок: сначала полученные, потом остальные; внутри — как в каталоге.
 */
internal fun mergeAchievements(
    catalog: List<HomeAchievementDto>,
    earned: List<UserAchievementDto>,
): List<HomeAchievement> {
    val earnedById = earned
        .mapNotNull { item -> item.earnedAchievementId()?.let { it to item } }
        .distinctBy { (id, _) -> id }
        .toMap()
    val fromCatalog = catalog
        .distinctBy { it.id }
        .filter { it.isActive || it.id in earnedById }
        .map { achievement ->
            HomeAchievement(
                id = achievement.id,
                title = achievement.name.cleaned(),
                description = achievement.description.cleaned(),
                isEarned = achievement.id in earnedById,
            )
        }
    val catalogIds = catalog.map { it.id }.toSet()
    val missingInCatalog = earnedById
        .filterKeys { it !in catalogIds }
        .map { (id, item) ->
            HomeAchievement(
                id = id,
                title = item.achievement?.name.cleaned(),
                description = item.achievement?.description.cleaned(),
                isEarned = true,
            )
        }
    return (fromCatalog + missingInCatalog).sortedByDescending { it.isEarned }
}

private fun UserAchievementDto.earnedAchievementId(): Int? =
    achievementId.takeIf { it != 0 } ?: achievement?.id?.takeIf { it != 0 }

/**
 * Квест показывается в «Активных вызовах», если пользователь его ещё не
 * завершил и не отменил. Сервер и так отдаёт только квесты в пределах дат.
 */
internal fun QuestListItemDto.isActiveForUser(): Boolean =
    participationStatus !in FINISHED_STATUSES

private val FINISHED_STATUSES = setOf("completed", "cancelled")

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
