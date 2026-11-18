package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.activities.data.dto.ActivityPageDto
import com.example.apexhr.features.activities.data.dto.CompleteQuestStepRequest
import com.example.apexhr.features.activities.data.dto.SubmitSurveyRequest
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.activities.domain.ActivityActionResult
import com.example.apexhr.features.activities.domain.ActivityPage
import com.example.apexhr.features.activities.domain.Event
import com.example.apexhr.features.activities.domain.EventDetails
import com.example.apexhr.features.activities.domain.Quest
import com.example.apexhr.features.activities.domain.QuestDetails
import com.example.apexhr.features.activities.domain.Survey
import com.example.apexhr.features.activities.domain.SurveyAnswer
import com.example.apexhr.features.activities.domain.SurveyDetails

/** Списки грузятся страницами по [PAGE_SIZE], как в вебе (pageSize=50). */
class ActivitiesRepositoryImpl(
    private val api: ActivitiesApi,
) : ActivitiesRepository {

    override suspend fun getSurveys(page: Int): DataResult<ActivityPage<Survey>> = safeApiCall {
        api.getSurveys(page, PAGE_SIZE).toDomainPage(page) { it.toDomain() }
    }

    override suspend fun getQuests(page: Int): DataResult<ActivityPage<Quest>> = safeApiCall {
        api.getQuests(page, PAGE_SIZE).toDomainPage(page) { it.toDomain() }
    }

    override suspend fun getEvents(page: Int): DataResult<ActivityPage<Event>> = safeApiCall {
        api.getEvents(page, PAGE_SIZE).toDomainPage(page) { it.toDomain() }
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

    /** Номер страницы берём запрошенный: сервер может не прислать page. */
    private inline fun <T, R> ActivityPageDto<T>.toDomainPage(requestedPage: Int, map: (T) -> R) =
        ActivityPage(items = items.map(map), page = requestedPage, hasNextPage = hasNextPage)

    companion object {
        const val PAGE_SIZE = 50
    }
}
