package com.example.apexhr.features.thanks.data.dto

import kotlinx.serialization.Serializable

/** Элемент GET /api/users и ответ GET /api/users/me (лишние поля игнорируются). */
@Serializable
data class ThanksUserDto(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val department: String? = null,
    val position: String? = null,
    val isActive: Boolean = true,
    /** Коды ролей; есть в ответе /api/users/me. */
    val roles: List<String> = emptyList(),
    val canAccessAdminPanel: Boolean = false,
)

/** Ответ GET /api/admin/balances/me (BalanceResponse); веб читает amount, иначе balance. */
@Serializable
data class WorkBalanceDto(
    val amount: Long? = null,
    val balance: Long? = null,
)

/** Тело POST /api/thanks/manager-bonus (CreateManagerBonusRequest в Swagger). */
@Serializable
data class CreateManagerBonusRequest(
    val toUserId: String,
    val amount: Long,
    val message: String? = null,
)

/**
 * Элемент GET /api/thanks/for-me и /api/thanks/from-me.
 * fromUserName/toUserName сервер сейчас заполняет id пользователя, а не именем.
 */
@Serializable
data class ThankDto(
    val id: Long,
    val fromUserId: String? = null,
    val fromUserName: String? = null,
    val toUserId: String? = null,
    val toUserName: String? = null,
    val amount: Long = 0,
    val message: String? = null,
    val createdAt: String? = null,
)

/**
 * Тело POST /api/thanks (CreateThankRequest в Swagger).
 * У isPublic нет значения по умолчанию: Json без encodeDefaults не отправил бы его.
 */
@Serializable
data class CreateThankRequest(
    val toUserId: String,
    val amount: Long,
    val message: String? = null,
    val isPublic: Boolean,
)
