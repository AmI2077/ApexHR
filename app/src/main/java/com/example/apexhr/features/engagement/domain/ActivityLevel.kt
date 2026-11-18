package com.example.apexhr.features.engagement.domain

/**
 * Уровень активности текущего пользователя (GET /api/me/engagement), как в вебе.
 * Сервер сам считает очки за период и сам называет уровни («Искра», «Мерцание»…),
 * поэтому названия и пороги в приложении не хранятся.
 * @param title название уровня с сервера; null — сервер его не прислал.
 * @param rank номер уровня от 1 до 5.
 * @param nextTitle следующий уровень; null — уже высший.
 * @param progress доля пути до следующего уровня от 0 до 1; у высшего уровня 1.
 * @param breakdown из чего сложились очки (мероприятия, квесты, благодарности…).
 */
data class ActivityLevel(
    val code: String?,
    val title: String?,
    val rank: Int,
    val score: Double,
    val maxScore: Double,
    val nextTitle: String?,
    val pointsToNext: Double?,
    val progress: Double = 1.0,
    val breakdown: List<ActivityLevelSource> = emptyList(),
)

/** Источник очков уровня: название с сервера, сколько действий и очков из максимума. */
data class ActivityLevelSource(
    val code: String?,
    val title: String?,
    val count: Int,
    val points: Double,
    val maxPoints: Double,
)
