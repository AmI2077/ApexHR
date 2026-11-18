package com.example.apexhr.features.home.data

import com.example.apexhr.features.home.data.dto.HomeUserDto
import com.example.apexhr.features.home.domain.FeedItem
import com.example.apexhr.features.profile.data.dto.TransactionDto
import com.example.apexhr.features.profile.domain.UserProfile
import com.example.apexhr.features.thanks.data.dto.ThankDto
import java.time.Instant
import java.time.OffsetDateTime

/**
 * Лента благодарностей, как feed в вебе: полученные и отправленные благодарности
 * (GET /api/thanks/for-me и /from-me) и поощрения руководителя из моих операций
 * (relatedEntityType «manager_bonus»: плюс — мне от relatedUserId, минус — я отправил).
 * Новые сверху. Имена — из списка сотрудников: сервер в fromUserName кладёт id.
 */
internal fun buildHomeFeed(
    me: UserProfile,
    users: List<HomeUserDto>,
    received: List<ThankDto>,
    sent: List<ThankDto>,
    transactions: List<TransactionDto>,
): List<FeedItem> {
    val names = users
        .mapNotNull { user -> user.id?.trim()?.takeIf { it.isNotEmpty() }?.let { id -> user.displayName()?.let { id.lowercase() to it } } }
        .toMap() + (me.id.lowercase() to listOfNotNull(me.firstName, me.lastName).joinToString(" ").takeIf { it.isNotBlank() })
    fun nameOf(id: String?): String? = id?.let { names[it.lowercase()] }

    val thanks = (received + sent).distinctBy { it.id }.map { thank ->
        FeedItem(
            id = "thank_${thank.id}",
            fromId = thank.fromUserId,
            toId = thank.toUserId,
            fromName = nameOf(thank.fromUserId),
            toName = nameOf(thank.toUserId),
            amount = thank.amount,
            message = thank.message?.trim()?.takeIf { it.isNotEmpty() },
            isManagerBonus = false,
            createdAt = thank.createdAt.toFeedInstantOrNull(),
        )
    }
    val bonuses = transactions.filter { it.isManagerBonus() && it.amount != 0L }.map { tx ->
        val incoming = tx.amount > 0
        val fromId = if (incoming) tx.relatedUserId else me.id
        val toId = if (incoming) me.id else tx.relatedUserId
        FeedItem(
            id = "bonus_${tx.id}",
            fromId = fromId,
            toId = toId,
            fromName = nameOf(fromId),
            toName = nameOf(toId),
            amount = kotlin.math.abs(tx.amount),
            message = tx.description?.trim()?.takeIf { it.isNotEmpty() },
            isManagerBonus = true,
            createdAt = tx.createdAt.toFeedInstantOrNull(),
        )
    }
    return (thanks + bonuses).sortedWith(compareByDescending(nullsFirst()) { it.createdAt })
}

/** Как в вебе: «manager_bonus», «ManagerBonus», «manager-bonus» — одно и то же. */
private fun TransactionDto.isManagerBonus(): Boolean =
    relatedEntityType.orEmpty().lowercase().replace(Regex("[\\s_-]"), "") == "managerbonus"

private fun String?.toFeedInstantOrNull(): Instant? {
    val value = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
}
