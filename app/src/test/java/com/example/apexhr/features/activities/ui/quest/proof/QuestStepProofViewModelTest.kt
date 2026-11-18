package com.example.apexhr.features.activities.ui.quest.proof

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventDetails
import com.example.apexhr.features.activities.domain.ProofStatus
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.QuestParticipation
import com.example.apexhr.features.activities.domain.QuestParticipationStatus
import com.example.apexhr.features.activities.domain.QuestProofError
import com.example.apexhr.features.activities.domain.QuestProofRepository
import com.example.apexhr.features.activities.domain.QuestProofResult
import com.example.apexhr.features.activities.domain.QuestStep
import com.example.apexhr.features.activities.domain.QuestStepType
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.activities.domain.SurveyAnswer
import com.example.apexhr.features.activities.domain.SurveyDetails
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.quest.models.QuestProofAction
import com.example.apexhr.features.activities.ui.quest.proof.models.PROOF_COMMENT_MAX_LENGTH
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofScreenState
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuestStepProofViewModelTest {

    private var details: DataResult<QuestDetails> = DataResult.Success(quest())
    private val activities = ProofActivitiesRepository { details }
    private val proofs = FakeQuestProofRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = QuestStepProofViewModel(2, 4, activities, proofs)

    private fun QuestStepProofViewModel.content(): QuestStepProofUiState =
        (state.value as QuestStepProofScreenState.Content).uiState

    @Test
    fun `step state decides the action`() {
        assertEquals(QuestProofAction.Submit, content(proofStep()).action)
        assertEquals(QuestProofAction.Resubmit, content(proofStep(ProofStatus.Rejected)).action)

        val update = content(proofStep(ProofStatus.Pending, path = "https://x/old.jpg", comment = "Старый"))
        assertEquals(QuestProofAction.Update, update.action)
        assertEquals("https://x/old.jpg", update.existingStoragePath)
        assertEquals("Старый", update.comment)
        assertTrue(update.canSend)
    }

    @Test
    fun `form is unavailable when step cannot take proof`() {
        val unavailable = QuestStepProofScreenState.Unavailable
        assertEquals(unavailable, quest(steps = listOf(proofStep(ProofStatus.Approved))).toProofState(4))
        assertEquals(unavailable, quest(steps = listOf(proofStep().copy(isCompleted = true))).toProofState(4))
        assertEquals(unavailable, quest(steps = listOf(proofStep().copy(completionType = QuestStepType.Manual))).toProofState(4))
        assertEquals(unavailable, quest(status = null).toProofState(4))
        assertEquals(unavailable, quest(status = QuestParticipationStatus.Completed).toProofState(4))
        assertEquals(unavailable, quest().toProofState(99))
    }

    @Test
    fun `load error is shown`() {
        details = DataResult.Failure(DataError.Network)

        assertEquals(QuestStepProofScreenState.Error(DataError.Network), viewModel().state.value)
    }

    @Test
    fun `file is required for new proof and comment is limited`() {
        val viewModel = viewModel()
        assertFalse(viewModel.content().canSend)

        viewModel.onSendClick()
        assertTrue(proofs.calls.isEmpty())

        viewModel.onFileSelected("content://photo")
        viewModel.onCommentChange("я".repeat(PROOF_COMMENT_MAX_LENGTH + 5))

        assertTrue(viewModel.content().canSend)
        assertEquals(PROOF_COMMENT_MAX_LENGTH, viewModel.content().comment.length)
    }

    @Test
    fun `successful send emits sent`() {
        val viewModel = viewModel()
        var sent = 0
        viewModel.sent.onEach { sent++ }.launchIn(CoroutineScope(Dispatchers.Main))

        viewModel.onFileSelected("content://photo")
        viewModel.onCommentChange("Готово")
        viewModel.onSendClick()

        assertEquals(listOf(Call("content://photo", null, "Готово", update = false)), proofs.calls)
        assertEquals(1, sent)
        assertFalse(viewModel.content().canSend)
    }

    @Test
    fun `pending proof is replaced keeping old file`() {
        details = DataResult.Success(quest(steps = listOf(proofStep(ProofStatus.Pending, path = "https://x/old.jpg"))))
        val viewModel = viewModel()

        viewModel.onSendClick()

        assertEquals(listOf(Call(null, "https://x/old.jpg", "", update = true)), proofs.calls)
    }

    @Test
    fun `failure shows message and allows retry`() {
        proofs.result = QuestProofResult.Failure(QuestProofError.UploadRejected)
        val viewModel = viewModel()
        val messages = mutableListOf<ActivityMessage>()
        viewModel.messages.onEach { messages += it }.launchIn(CoroutineScope(Dispatchers.Main))

        viewModel.onFileSelected("content://photo")
        viewModel.onSendClick()

        assertEquals(listOf(ActivityMessage(R.string.quest_proof_error_upload_rejected)), messages)
        assertTrue(viewModel.content().canSend)
    }

    private fun content(step: QuestStep): QuestStepProofUiState {
        details = DataResult.Success(quest(steps = listOf(step)))
        return viewModel().content()
    }
}

private data class Call(val fileUri: String?, val existing: String?, val comment: String?, val update: Boolean)

private class FakeQuestProofRepository : QuestProofRepository {
    val calls = mutableListOf<Call>()
    var result: QuestProofResult = QuestProofResult.Success

    override suspend fun submitProof(
        questId: Long,
        stepId: Long,
        fileUri: String?,
        existingStoragePath: String?,
        comment: String?,
        update: Boolean,
    ): QuestProofResult {
        calls += Call(fileUri, existingStoragePath, comment, update)
        return result
    }
}

private class ProofActivitiesRepository(
    private val details: () -> DataResult<QuestDetails>,
) : ActivitiesRepository {
    override suspend fun getQuestDetails(questId: Long): DataResult<QuestDetails> = details()

    override suspend fun getSurveys(): DataResult<List<Survey>> = error("не нужен")
    override suspend fun getQuests(): DataResult<List<Quest>> = error("не нужен")
    override suspend fun getEvents(): DataResult<List<Event>> = error("не нужен")
    override suspend fun getSurveyDetails(surveyId: Long): DataResult<SurveyDetails> = error("не нужен")
    override suspend fun getEventDetails(eventId: Long): DataResult<EventDetails> = error("не нужен")
    override suspend fun submitSurvey(surveyId: Long, answers: Map<Long, SurveyAnswer>): ActivityActionResult =
        error("не нужен")
    override suspend fun joinQuest(questId: Long): ActivityActionResult = error("не нужен")
    override suspend fun completeQuestStep(questId: Long, stepId: Long): ActivityActionResult = error("не нужен")
    override suspend fun completeQuest(questId: Long): ActivityActionResult = error("не нужен")
    override suspend fun registerForEvent(eventId: Long): ActivityActionResult = error("не нужен")
    override suspend fun cancelEventRegistration(eventId: Long): ActivityActionResult = error("не нужен")
    override suspend fun claimEventReward(eventId: Long): ActivityActionResult = error("не нужен")
}

internal fun proofStep(status: ProofStatus? = null, path: String? = null, comment: String? = null) = QuestStep(
    id = 4,
    title = "Фото с завтрака",
    description = null,
    completionType = QuestStepType.Proof,
    isRequired = true,
    isCompleted = false,
    proofStatus = status,
    proofStoragePath = path,
    proofComment = comment,
)

internal fun quest(
    status: QuestParticipationStatus? = QuestParticipationStatus.InProgress,
    steps: List<QuestStep> = listOf(proofStep()),
) = QuestDetails(
    id = 2,
    title = "Квест",
    description = null,
    rewardAmount = 100,
    isActive = true,
    endAt = null,
    participation = status?.let { QuestParticipation(it, rewardClaimed = false) },
    steps = steps,
)