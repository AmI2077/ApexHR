package com.example.apexhr.features.profile.data

import com.example.apexhr.features.profile.data.dto.CurrentUserProfileDto
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.data.dto.UserAchievementDto
import com.example.apexhr.features.profile.domain.EarnedAchievement
import com.example.apexhr.features.profile.domain.HistoryTransaction
import com.example.apexhr.features.profile.domain.TransactionType
import com.example.apexhr.features.profile.domain.UserProfile
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

fun CurrentUserProfileDto.toDomain() = UserProfile(
    id = id,
    firstName = firstName.cleaned(),
    lastName = lastName.cleaned(),
    middleName = middleName.cleaned(),
    avatarUrl = avatarUrl.cleaned(),
    department = department.cleaned(),
    position = position.cleaned(),
)

fun TransactionDto.toHistoryDomain() = HistoryTransaction(
    id = id,
    amount = amount,
    description = description.cleaned(),
    type = transactionTypeOf(relatedEntityType, amount),
    createdAt = createdAt.toInstantOrNull(),
)

/**
 * Вид операции по relatedEntityType. На сервере встречаются order, thank,
 * achievement, event и null; остальные значения взяты из TransactionType
 * Swagger (ManagerBonus, Refund, AdminAdjustment, DailyBonus…) и пишутся
 * по-разному, поэтому сравниваем без регистра, «_» и «-».
 * null — ручное начисление админом (у него нет связанной сущности).
 * Описание не смотрим: «Получено достижение "Первая благодарность"» — не благодарность.
 */
internal fun transactionTypeOf(relatedEntityType: String?, amount: Long): TransactionType {
    val type = relatedEntityType
        ?.lowercase()
        ?.filter { it.isLetterOrDigit() }
        ?.takeIf { it.isNotEmpty() }
        ?: return TransactionType.Adjustment
    return when (type) {
        "thank", "thanks", "thanksreward" -> TransactionType.Thanks
        "managerbonus", "bonus", "managerreward" -> TransactionType.Bonus
        // Положительная операция по заказу — возврат за отменённый заказ.
        "order", "orders", "purchase" -> if (amount < 0) TransactionType.Purchase else TransactionType.Refund
        "refund" -> TransactionType.Refund
        "event", "quest", "survey", "activity", "activityreward",
        "achievement", "achievementreward", "dailybonus" -> TransactionType.Activity
        "admin", "adjustment", "adminadjustment" -> TransactionType.Adjustment
        else -> TransactionType.Other
    }
}

/** Сервер отдаёт даты в ISO-8601 с «Z»; на всякий случай принимаем и смещение. */
private fun String?.toInstantOrNull(): Instant? {
    val value = cleaned() ?: return null
    return try {
        Instant.parse(value)
    } catch (e: DateTimeParseException) {
        try {
            OffsetDateTime.parse(value).toInstant()
        } catch (e: DateTimeParseException) {
            null
        }
    }
}

fun UserAchievementDto.toDomain() = EarnedAchievement(
    id = id,
    title = achievement?.name.cleaned(),
    description = achievement?.description.cleaned(),
    iconUrl = achievement?.iconUrl.cleaned(),
)

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
