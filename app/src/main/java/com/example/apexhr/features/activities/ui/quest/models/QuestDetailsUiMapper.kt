package com.example.apexhr.features.activities.ui.quest.models

import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.ProofStatus
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.QuestStep
import com.example.apexhr.features.activities.domain.QuestStepType
import com.example.apexhr.features.activities.ui.models.formatActivityDate
import com.example.apexhr.features.activities.ui.models.labelRes
import com.example.apexhr.features.activities.ui.models.toRewardOrNull
import java.time.ZoneId

/**
 * Окончательно решает сервер: при отказе его текст покажется в snackbar.
 * @param zone часовой пояс, в котором показываем даты (по умолчанию — устройства).
 */
fun QuestDetails.toUiModel(zone: ZoneId = ZoneId.systemDefault()): QuestDetailsUiModel {
    val participating = participation?.status == QuestParticipationStatus.InProgress
    val (action, noteRes) = availableAction()
    return QuestDetailsUiModel(
        title = title,
        description = description,
        reward = rewardAmount.toRewardOrNull(),
        endAt = endAt?.formatActivityDate(zone),
        statusRes = participation?.status?.labelRes(),
        completedStepsCount = steps.count { it.isCompleted },
        steps = steps.map { it.toUiModel(participating) },
        action = action,
        noteRes = noteRes,
    )
}

private fun QuestDetails.availableAction(): Pair<QuestAction?, Int?> =
    when (participation?.status) {
        QuestParticipationStatus.InProgress ->
            if (steps.filter { it.isRequired }.all { it.isCompleted }) {
                QuestAction.Complete to null
            } else {
                null to R.string.quest_note_required_steps
            }

        QuestParticipationStatus.Completed, QuestParticipationStatus.Unknown -> null to null

        // Не участвует или участие отменено: можно присоединиться к активному квесту.
        null, QuestParticipationStatus.Cancelled ->
            if (isActive) QuestAction.Join to null else null to R.string.quest_note_inactive
    }

private fun QuestStep.toUiModel(participating: Boolean): QuestStepUiModel = QuestStepUiModel(
    id = id,
    title = title,
    description = description,
    isRequired = isRequired,
    isCompleted = isCompleted,
    canComplete = participating && !isCompleted && completionType == QuestStepType.Manual,
    stateRes = stateRes(),
    proofAction = if (participating) proofAction() else null,
    proofComment = proofComment.takeIf { completionType == QuestStepType.Proof },
)

/** Как в вебе: отправить, отправить заново после отказа или заменить то, что на проверке. */
private fun QuestStep.proofAction(): QuestProofAction? = when {
    isCompleted || completionType != QuestStepType.Proof -> null
    else -> when (proofStatus) {
        ProofStatus.Pending -> QuestProofAction.Update
        ProofStatus.Rejected -> QuestProofAction.Resubmit
        ProofStatus.Approved -> null
        ProofStatus.Unknown, null -> QuestProofAction.Submit
    }
}

/** Состояние шага; подтверждение отправляется отдельной кнопкой, см. [proofAction]. */
private fun QuestStep.stateRes(): Int? = when {
    isCompleted -> R.string.quest_step_completed
    completionType == QuestStepType.Manual -> null
    completionType == QuestStepType.Proof -> when (proofStatus) {
        ProofStatus.Pending -> R.string.quest_step_proof_pending
        ProofStatus.Rejected -> R.string.quest_step_proof_rejected
        ProofStatus.Approved -> R.string.quest_step_proof_approved
        ProofStatus.Unknown, null -> R.string.quest_step_proof_required
    }

    completionType == QuestStepType.Automatic -> R.string.quest_step_automatic
    else -> R.string.quest_step_unsupported
}
