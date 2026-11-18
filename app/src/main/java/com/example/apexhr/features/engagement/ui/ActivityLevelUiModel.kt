package com.example.apexhr.features.engagement.ui

import com.example.apexhr.features.engagement.domain.ActivityLevel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Карточка уровня активности, как ActivityLevelCard в вебе.
 * @param title название уровня; null — сервер не прислал, UI подставит «Уровень».
 * @param score очки и [maxScore] уже отформатированы («7,6», «100»).
 * @param progress доля полосы прогресса от 0 до 1.
 * @param nextTitle следующий уровень; null — уровень высший.
 * @param pointsToNext сколько очков осталось до [nextTitle].
 */
data class ActivityLevelUiModel(
    val title: String?,
    val score: String,
    val maxScore: String,
    val progress: Float,
    val nextTitle: String?,
    val pointsToNext: String,
    val breakdown: List<ActivityLevelSourceUiModel> = emptyList(),
) {
    companion object {
        val Preview = ActivityLevelUiModel(
            title = "Искра",
            score = "7,6",
            maxScore = "100",
            progress = 0.42f,
            nextTitle = "Мерцание",
            pointsToNext = "10,4",
            breakdown = listOf(
                ActivityLevelSourceUiModel(title = "Пройденные опросы", points = "2,4"),
                ActivityLevelSourceUiModel(title = "Отправленные благодарности", points = "2,4"),
            ),
        )
    }
}

/** Строка «из чего сложились очки»; title null — UI подставит «Источник». */
data class ActivityLevelSourceUiModel(
    val title: String?,
    val points: String,
)

fun ActivityLevel.toUiModel(): ActivityLevelUiModel = ActivityLevelUiModel(
    title = title,
    score = formatScore(score),
    maxScore = formatScore(maxScore),
    progress = progress.toFloat().coerceIn(0f, 1f),
    nextTitle = nextTitle,
    pointsToNext = formatScore(pointsToNext ?: 0.0),
    breakdown = breakdown.map { ActivityLevelSourceUiModel(title = it.title ?: it.code, points = formatScore(it.points)) },
)

/** Очки уровня дробные: «7,6», «10», «12,5» — как formatNumber в вебе для ru-RU. */
fun formatScore(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("ru-RU"))
    return DecimalFormat("#,##0.#", symbols).format(value).replace('\u00A0', ' ')
}
