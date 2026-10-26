package com.example.apexhr.features.thanks.domain

import java.time.Instant

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
