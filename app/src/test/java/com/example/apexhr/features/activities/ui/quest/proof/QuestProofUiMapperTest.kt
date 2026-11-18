package com.example.apexhr.features.activities.ui.quest.proof

import com.example.apexhr.R
import com.example.apexhr.features.activities.domain.ProofStatus
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.QuestStepType
import com.example.apexhr.features.activities.ui.quest.models.QuestProofAction
import com.example.apexhr.features.activities.ui.quest.models.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Кнопка подтверждения на карточке шага в деталях квеста. */
class QuestProofUiMapperTest {

    private fun stepModel(
        status: ProofStatus? = null,
        participation: QuestParticipationStatus? = QuestParticipationStatus.InProgress,
        isCompleted: Boolean = false,
        type: QuestStepType = QuestStepType.Proof,
        comment: String? = null,
    ) = quest(
        status = participation,
        steps = listOf(proofStep(status, comment = comment).copy(isCompleted = isCompleted, completionType = type)),
    ).toUiModel().steps.single()

    @Test
    fun `proof button follows proof status`() {
        assertEquals(QuestProofAction.Submit, stepModel().proofAction)
        assertEquals(QuestProofAction.Resubmit, stepModel(ProofStatus.Rejected).proofAction)
        assertEquals(QuestProofAction.Update, stepModel(ProofStatus.Pending).proofAction)
        assertNull(stepModel(ProofStatus.Approved).proofAction)
    }

    @Test
    fun `no proof button outside of progress or for other steps`() {
        assertNull(stepModel(participation = null).proofAction)
        assertNull(stepModel(participation = QuestParticipationStatus.Completed).proofAction)
        assertNull(stepModel(isCompleted = true).proofAction)
        assertNull(stepModel(type = QuestStepType.Manual).proofAction)
    }

    @Test
    fun `proof state texts and comment`() {
        assertEquals(R.string.quest_step_proof_required, stepModel().stateRes)
        assertEquals(R.string.quest_step_proof_approved, stepModel(ProofStatus.Approved).stateRes)
        assertEquals("Были всей командой", stepModel(ProofStatus.Pending, comment = "Были всей командой").proofComment)
        assertNull(stepModel(type = QuestStepType.Manual, comment = "x").proofComment)
    }
}