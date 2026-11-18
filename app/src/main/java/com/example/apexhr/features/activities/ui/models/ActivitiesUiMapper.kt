package com.example.apexhr.features.activities.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.Achievement
import com.example.apexhr.features.activities.domain.AchievementCondition
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventRegistrationStatus
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.Survey
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ACTIVITY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

/** Дата и время в часовом поясе [zone] (по умолчанию — устройства). */
fun Instant.formatActivityDate(zone: ZoneId = ZoneId.systemDefault()): String =
    atZone(zone).format(ACTIVITY_DATE_FORMAT)

/** Награда в апексах для карточки и деталей: пустую (0) не показываем. */
fun Long.toRewardOrNull(): Long? = takeIf { it > 0 }

/** Награду за опросы сервер не начисляет, поэтому её на карточке нет. */
fun Survey.toUiModel(): ActivityUiModel =
    ActivityUiModel(
        id = id,
        kind = ActivityKind.Survey,
        title = title,
        subtitle = ActivitySubtitle.Questions(questionsCount),
        reward = null,
        statusRes = if (hasResponded) R.string.activities_status_survey_answered else null,
        // Как в вебе: «Пройти» только пока ответ можно отправить.
        canParticipate = canRespond,
    )

fun Quest.toUiModel(): ActivityUiModel =
    ActivityUiModel(
        id = id,
        kind = ActivityKind.Quest,
        title = title,
        subtitle = ActivitySubtitle.Steps(stepsCount),
        reward = rewardAmount.toRewardOrNull(),
        statusRes = participationStatus?.labelRes(),
        // Как в вебе: «Участвовать», пока пользователь не в квесте (не в процессе и не завершил).
        canParticipate = participationStatus != QuestParticipationStatus.InProgress &&
            participationStatus != QuestParticipationStatus.Completed,
    )

fun Event.toUiModel(zone: ZoneId = ZoneId.systemDefault()): ActivityUiModel =
    ActivityUiModel(
        id = id,
        kind = ActivityKind.Event,
        title = title,
        subtitle = ActivitySubtitle.EventTime(
            startAt = startAt?.formatActivityDate(zone),
            location = location,
        ).takeIf { it.startAt != null || it.location != null },
        reward = rewardAmount.toRewardOrNull(),
        statusRes = registrationStatus?.labelRes(),
        // Как в вебе: записаться можно, если записи нет или она отменена.
        canParticipate = registrationStatus == null || registrationStatus == EventRegistrationStatus.Cancelled,
    )

/** Достижение в общем списке: награда, «Получено» и условие, как в карточке веба. */
fun Achievement.toUiModel(zone: ZoneId = ZoneId.systemDefault()): ActivityUiModel =
    ActivityUiModel(
        id = id,
        kind = ActivityKind.Achievement,
        title = name,
        subtitle = ActivitySubtitle.AchievementInfo(
            description = description,
            isEarned = isEarned,
            earnedAt = earnedAt?.formatActivityDate(zone)?.takeIf { isEarned },
            conditionRes = if (isEarned) null else conditionType.hintRes(),
        ),
        reward = rewardAmount.toRewardOrNull(),
        statusRes = if (isEarned) R.string.activities_status_achievement_earned else null,
        canParticipate = false,
    )

@StringRes
fun AchievementCondition.hintRes(): Int = when (this) {
    AchievementCondition.FirstOrder -> R.string.achievement_condition_first_order
    AchievementCondition.FirstThank -> R.string.achievement_condition_first_thank
    AchievementCondition.FirstActivity -> R.string.achievement_condition_first_activity
    AchievementCondition.Other -> R.string.achievement_condition_other
}

@StringRes
fun QuestParticipationStatus.labelRes(): Int? = when (this) {
    QuestParticipationStatus.InProgress -> R.string.activities_status_quest_in_progress
    QuestParticipationStatus.Completed -> R.string.activities_status_quest_completed
    QuestParticipationStatus.Cancelled -> R.string.activities_status_quest_cancelled
    QuestParticipationStatus.Unknown -> null
}

@StringRes
fun EventRegistrationStatus.labelRes(): Int? = when (this) {
    EventRegistrationStatus.Registered -> R.string.activities_status_event_registered
    EventRegistrationStatus.Attended -> R.string.activities_status_event_attended
    EventRegistrationStatus.Cancelled -> R.string.activities_status_event_cancelled
    EventRegistrationStatus.NoShow -> R.string.activities_status_event_no_show
    EventRegistrationStatus.Unknown -> null
}

/** Поиск по названию без учёта регистра; пустой запрос оставляет всё. */
fun List<ActivityUiModel>.filterByQuery(query: String): List<ActivityUiModel> {
    val needle = query.trim()
    if (needle.isEmpty()) return this
    return filter { it.title?.contains(needle, ignoreCase = true) == true }
}
