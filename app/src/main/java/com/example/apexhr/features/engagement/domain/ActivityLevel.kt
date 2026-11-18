package com.example.apexhr.features.engagement.domain

/**
 * Уровень активности текущего пользователя (GET /api/me/engagement), как в вебе.
 * Сервер сам считает очки за период и сам называет уровни («Искра», «Мерцание»…),
 * поэтому названия и пороги в приложении не хранятся.
 * @param title название уровня с сервера; null — сервер его не прислал.
 * @param rank номер уровня от 1 до 5.
 * @param nextTitle следующий уровень; null — уже высший.
 */
data class ActivityLevel(
    val code: String?,
    val title: String?,
    val rank: Int,
    val score: Double,
    val maxScore: Double,
    val nextTitle: String?,
    val pointsToNext: Double?,
)
