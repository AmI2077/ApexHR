package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.data.dto.CompleteQuestStepRequest
import com.example.apexhr.features.activities.data.dto.SubmitSurveyRequest
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventDetails
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.activities.domain.SurveyAnswer
import com.example.apexhr.features.activities.domain.SurveyDetails

/** Списки — только первая страница: активностей у компании немного. */
class ActivitiesRepositoryImpl(
    private val api: ActivitiesApi,
) : ActivitiesRepository {

    override suspend fun getSurveys(): DataResult<List<Survey>> = safeApiCall {
        api.getSurveys(FIRST_PAGE, PAGE_SIZE).items.map { it.toDomain() }
    }

    override suspend fun getQuests(): DataResult<List<Quest>> = safeApiCall {
        api.getQuests(FIRST_PAGE, PAGE_SIZE).items.map { it.toDomain() }
    }

    override suspend fun getEvents(): DataResult<List<Event>> = safeApiCall {
        api.getEvents(FIRST_PAGE, PAGE_SIZE).items.map { it.toDomain() }
    }

    override suspend fun getSurveyDetails(surveyId: Long): DataResult<SurveyDetails> = safeApiCall {
        api.getSurveyDetails(surveyId).toDomain()
    }

    override suspend fun getQuestDetails(questId: Long): DataResult<QuestDetails> = safeApiCall {
        api.getQuestDetails(questId).toDomain()
    }

    override suspend fun getEventDetails(eventId: Long): DataResult<EventDetails> = safeApiCall {
        api.getEventDetails(eventId).toDomain()
    }

    override suspend fun submitSurvey(
        surveyId: Long,
        answers: Map<Long, SurveyAnswer>,
    ): ActivityActionResult = action {
        api.submitSurvey(surveyId, SubmitSurveyRequest(answers.toAnswerDtos()))
    }

    override suspend fun joinQuest(questId: Long): ActivityActionResult =
        action { api.joinQuest(questId) }

    override suspend fun completeQuestStep(questId: Long, stepId: Long): ActivityActionResult =
        action { api.completeQuestStep(questId, stepId, CompleteQuestStepRequest()) }

    override suspend fun completeQuest(questId: Long): ActivityActionResult =
        action { api.completeQuest(questId) }

    override suspend fun registerForEvent(eventId: Long): ActivityActionResult =
        action { api.registerForEvent(eventId) }

    override suspend fun cancelEventRegistration(eventId: Long): ActivityActionResult =
        action { api.cancelEventRegistration(eventId) }

    override suspend fun claimEventReward(eventId: Long): ActivityActionResult =
        action { api.claimEventReward(eventId) }

    private suspend fun action(block: suspend () -> Unit): ActivityActionResult =
        safeActivityAction(block).fold(
            onSuccess = { ActivityActionResult.Success },
            onFailure = { ActivityActionResult.Failure(it.toActivityActionError()) },
        )

    private companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 50
    }
}
