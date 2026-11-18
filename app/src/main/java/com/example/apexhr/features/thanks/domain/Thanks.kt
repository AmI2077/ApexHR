package com.example.apexhr.features.thanks.domain

import java.time.Duration
import java.time.Instant

/**
 * Сколько апексов получает коллега за благодарность, как PEER_THANKS_AMOUNT в вебе.
 * Отправителю благодарность ничего не стоит.
 */
const val PEER_THANKS_AMOUNT = 25L

/** Одному коллеге — не чаще раза в неделю: веб считает последние 7 суток. */
val PEER_THANKS_PERIOD: Duration = Duration.ofDays(7)

/**
 * Кого текущий пользователь уже благодарил за последние [PEER_THANKS_PERIOD] к моменту [now]
 * (по отправленным благодарностям, как thankedThisWeek в вебе).
 */
fun List<Thank>.recipientsThankedRecently(now: Instant): Set<String> {
    val from = now.minus(PEER_THANKS_PERIOD)
    return filter { thank -> thank.createdAt?.let { it >= from } == true }
        .mapNotNull { it.counterpartId }
        .toSet()
}

/** Коллега, которому можно отправить благодарность. */
data class Colleague(
    val id: String,
    /** Имя и фамилия; null, если сервер не прислал ни того, ни другого. */
    val name: String?,
    val position: String?,
    val department: String?,
)

/**
 * Благодарность.
 * @param counterpartId отправитель для полученных и получатель для отправленных.
 * @param counterpartName имя из списка сотрудников; null, если сотрудник не найден.
 */
data class Thank(
    val id: Long,
    val counterpartId: String?,
    val counterpartName: String?,
    val amount: Long,
    val message: String?,
    val createdAt: Instant?,
)

/** Полученные и отправленные благодарности текущего пользователя. */
data class ThanksHistory(
    val received: List<Thank>,
    val sent: List<Thank>,
)
