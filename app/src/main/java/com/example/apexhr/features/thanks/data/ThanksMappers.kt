package com.example.apexhr.features.thanks.data

import com.example.apexhr.features.thanks.data.dto.ThankDto
import com.example.apexhr.features.thanks.data.dto.ThanksUserDto
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.Thank
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

private val UUID_REGEX =
    Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

fun ThanksUserDto.toColleague() = Colleague(
    id = id,
    name = displayName(),
    position = position.cleaned(),
    department = department.cleaned(),
)

/** «Имя Фамилия»; null, если нет ни того, ни другого. */
internal fun ThanksUserDto.displayName(): String? =
    listOfNotNull(firstName.cleaned(), lastName.cleaned())
        .joinToString(" ")
        .takeIf { it.isNotEmpty() }

/** @param namesById имена всех сотрудников из GET /api/users по id. */
fun ThankDto.toReceived(namesById: Map<String, String>) = toDomain(
    counterpartId = fromUserId.cleaned(),
    counterpartName = resolveName(fromUserId, fromUserName, namesById),
)

/** @param namesById имена всех сотрудников из GET /api/users по id. */
fun ThankDto.toSent(namesById: Map<String, String>) = toDomain(
    counterpartId = toUserId.cleaned(),
    counterpartName = resolveName(toUserId, toUserName, namesById),
)

private fun ThankDto.toDomain(counterpartId: String?, counterpartName: String?) = Thank(
    id = id,
    counterpartId = counterpartId,
    counterpartName = counterpartName,
    amount = amount,
    message = message.cleaned(),
    createdAt = createdAt.toInstantOrNull(),
)

/**
 * Имя из списка сотрудников. Если сотрудника там нет, берём имя из самой
 * благодарности, но только если это действительно имя: сейчас сервер кладёт
 * туда id пользователя.
 */
internal fun resolveName(userId: String?, userName: String?, namesById: Map<String, String>): String? {
    userId.cleaned()?.let { id ->
        namesById[id]?.let { return it }
        namesById.entries.firstOrNull { it.key.equals(id, ignoreCase = true) }?.let { return it.value }
    }
    return userName.cleaned()
        ?.takeUnless { it.equals(userId?.trim(), ignoreCase = true) }
        ?.takeUnless { UUID_REGEX.matches(it) }
}

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

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
