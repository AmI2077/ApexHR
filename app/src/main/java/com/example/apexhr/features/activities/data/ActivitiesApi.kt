package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.activities.data.dto.ActivityPageDto
import com.example.apexhr.features.activities.data.dto.ActivityQuestListItemDto
import com.example.apexhr.features.activities.data.dto.CompleteQuestStepRequest
import com.example.apexhr.features.activities.data.dto.EventDetailsDto
import com.example.apexhr.features.activities.data.dto.EventListItemDto
import com.example.apexhr.features.activities.data.dto.QuestDetailsDto
import com.example.apexhr.features.activities.data.dto.SubmitSurveyRequest
import com.example.apexhr.features.activities.data.dto.SurveyDetailsDto
import com.example.apexhr.features.activities.data.dto.SurveyListItemDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Анкеты, квесты и мероприятия. Загрузка при ответе не 2xx бросает
 * [com.example.apexhr.core.data.network.ApiStatusException], действия —
 * [ActivitiesApiException] с текстом ошибки сервера.
 * Тела ответов действий не разбираем: после действия экран перечитывает детали.
 */
interface ActivitiesApi {
    suspend fun getSurveys(page: Int, pageSize: Int): ActivityPageDto<SurveyListItemDto>
    suspend fun getQuests(page: Int, pageSize: Int): ActivityPageDto<ActivityQuestListItemDto>
    suspend fun getEvents(page: Int, pageSize: Int): ActivityPageDto<EventListItemDto>

    suspend fun getSurveyDetails(surveyId: Long): SurveyDetailsDto
    suspend fun getQuestDetails(questId: Long): QuestDetailsDto
    suspend fun getEventDetails(eventId: Long): EventDetailsDto

    suspend fun submitSurvey(surveyId: Long, request: SubmitSurveyRequest)
    suspend fun joinQuest(questId: Long)
    suspend fun completeQuestStep(questId: Long, stepId: Long, request: CompleteQuestStepRequest)
    suspend fun completeQuest(questId: Long)
    suspend fun registerForEvent(eventId: Long)
    suspend fun cancelEventRegistration(eventId: Long)
    suspend fun claimEventReward(eventId: Long)
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorActivitiesApi(
    private val client: HttpClient,
) : ActivitiesApi {
    override suspend fun getSurveys(page: Int, pageSize: Int): ActivityPageDto<SurveyListItemDto> =
        client.get(NetworkConfig.SURVEYS_URL) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.requireSuccess().body()

    override suspend fun getQuests(page: Int, pageSize: Int): ActivityPageDto<ActivityQuestListItemDto> =
        client.get(NetworkConfig.QUESTS_URL) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.requireSuccess().body()

    override suspend fun getEvents(page: Int, pageSize: Int): ActivityPageDto<EventListItemDto> =
        client.get(NetworkConfig.EVENTS_URL) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.requireSuccess().body()

    override suspend fun getSurveyDetails(surveyId: Long): SurveyDetailsDto =
        client.get(NetworkConfig.surveyUrl(surveyId)).requireSuccess().body()

    override suspend fun getQuestDetails(questId: Long): QuestDetailsDto =
        client.get(NetworkConfig.questUrl(questId)).requireSuccess().body()

    override suspend fun getEventDetails(eventId: Long): EventDetailsDto =
        client.get(NetworkConfig.eventUrl(eventId)).requireSuccess().body()

    override suspend fun submitSurvey(surveyId: Long, request: SubmitSurveyRequest) {
        client.post(NetworkConfig.surveyResponsesUrl(surveyId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireActionSuccess()
    }

    override suspend fun joinQuest(questId: Long) {
        client.post(NetworkConfig.questJoinUrl(questId)).requireActionSuccess()
    }

    override suspend fun completeQuestStep(
        questId: Long,
        stepId: Long,
        request: CompleteQuestStepRequest,
    ) {
        client.post(NetworkConfig.questStepCompleteUrl(questId, stepId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireActionSuccess()
    }

    override suspend fun completeQuest(questId: Long) {
        client.post(NetworkConfig.questCompleteUrl(questId)).requireActionSuccess()
    }

    override suspend fun registerForEvent(eventId: Long) {
        client.post(NetworkConfig.eventRegisterUrl(eventId)).requireActionSuccess()
    }

    override suspend fun cancelEventRegistration(eventId: Long) {
        client.delete(NetworkConfig.eventRegistrationUrl(eventId)).requireActionSuccess()
    }

    override suspend fun claimEventReward(eventId: Long) {
        client.post(NetworkConfig.eventRewardUrl(eventId)).requireActionSuccess()
    }
}
