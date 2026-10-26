package com.example.apexhr.features.activities.ui.quest

import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.ProofStatus
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.QuestParticipation
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.QuestStep
import com.example.apexhr.features.activities.domain.QuestStepType
import com.example.apexhr.features.activities.ui.quest.models.QuestAction
import com.example.apexhr.features.activities.ui.quest.models.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class QuestDetailsUiMapperTest {

    private val moscow = ZoneId.of("Europe/Moscow")

    private fun step(
        id: Long,
        type: QuestStepType = QuestStepType.Manual,
        isRequired: Boolean = true,
        isCompleted: Boolean = false,
        proofStatus: ProofStatus? = null,
    ) = QuestStep(
        id = id,
        title = "Шаг $id",
        description = null,
        completionType = type,
        isRequired = isRequired,
        isCompleted = isCompleted,
        proofStatus = proofStatus,
    )

    private fun quest(
        status: QuestParticipationStatus? = null,
        isActive: Boolean = true,
        steps: List<QuestStep> = listOf(step(1), step(2, QuestStepType.Proof)),
        reward: Long = 300,
    ) = QuestDetails(
        id = 1,
        title = "Тестовый квест",
        description = null,
        rewardAmount = reward,
        isActive = isActive,
        endAt = Instant.parse("2026-10-30T22:46:00Z"),
        participation = status?.let { QuestParticipation(it, rewardClaimed = false) },
        steps = steps,
    )

    @Test
    fun `maps fields and counts completed steps`() {
        val model = quest(steps = listOf(step(1, isCompleted = true), step(2))).toUiModel(moscow)

        assertEquals(300L, model.reward)
        assertEquals("31.10.2026 01:46", model.endAt)
        assertEquals(1, model.completedStepsCount)
        assertEquals(listOf(1L, 2L), model.steps.map { it.id })
        assertNull(model.statusRes)
        assertNull(quest(reward = 0).toUiModel(moscow).reward)
    }

    @Test
    fun `active quest can be joined and steps are not completable before joining`() {
        val model = quest().toUiModel(moscow)

        assertEquals(QuestAction.Join, model.action)
        assertNull(model.noteRes)
        assertFalse(model.steps.any { it.canComplete })
        assertEquals(QuestAction.Join, quest(QuestParticipationStatus.Cancelled).toUiModel(moscow).action)
    }

    @Test
    fun `inactive quest shows note`() {
        val model = quest(isActive = false).toUiModel(moscow)

        assertNull(model.action)
        assertEquals(R.string.quest_note_inactive, model.noteRes)
    }

    @Test
    fun `participant completes only manual unfinished steps`() {
        val model = quest(
            QuestParticipationStatus.InProgress,
            steps = listOf(
                step(1),
                step(2, isCompleted = true),
                step(3, QuestStepType.Proof),
                step(4, QuestStepType.Automatic),
            ),
        ).toUiModel(moscow)

        assertEquals(listOf(true, false, false, false), model.steps.map { it.canComplete })
        assertEquals(R.string.activities_status_quest_in_progress, model.statusRes)
    }

    @Test
    fun `complete is available when all required steps are done`() {
        val notReady = quest(QuestParticipationStatus.InProgress).toUiModel(moscow)
        assertNull(notReady.action)
        assertEquals(R.string.quest_note_required_steps, notReady.noteRes)

        val ready = quest(
            QuestParticipationStatus.InProgress,
            steps = listOf(step(1, isCompleted = true), step(2, isRequired = false)),
        ).toUiModel(moscow)
        assertEquals(QuestAction.Complete, ready.action)
        assertNull(ready.noteRes)
    }

    @Test
    fun `completed quest has no action`() {
        val model = quest(QuestParticipationStatus.Completed).toUiModel(moscow)

        assertNull(model.action)
        assertNull(model.noteRes)
        assertEquals(R.string.activities_status_quest_completed, model.statusRes)
    }

    @Test
    fun `step state shows proof, automatic and completed states`() {
        val states = quest(
            steps = listOf(
                step(1),
                step(2, isCompleted = true),
                step(3, QuestStepType.Proof, proofStatus = ProofStatus.Pending),
                step(4, QuestStepType.Proof, proofStatus = ProofStatus.Rejected),
                step(5, QuestStepType.Proof),
                step(6, QuestStepType.Automatic),
                step(7, QuestStepType.Unknown),
            ),
        ).toUiModel(moscow).steps.map { it.stateRes }

        assertEquals(
            listOf(
                null,
                R.string.quest_step_completed,
                R.string.quest_step_proof_pending,
                R.string.quest_step_proof_rejected,
                R.string.quest_step_proof_unavailable,
                R.string.quest_step_automatic,
                R.string.quest_step_unsupported,
            ),
            states,
        )
        assertTrue(quest().toUiModel(moscow).steps.all { it.isRequired })
    }
}
