package com.example.apexhr.features.activities.domain

import com.example.apexhr.core.domain.DataResult

/** Анкеты, квесты и мероприятия: списки постранично, детали и действия. */
interface ActivitiesRepository {
    /** @param page номер страницы с 1. */
    suspend fun getSurveys(page: Int = FIRST_PAGE): DataResult<ActivityPage<Survey>>
    suspend fun getQuests(page: Int = FIRST_PAGE): DataResult<ActivityPage<Quest>>
    suspend fun getEvents(page: Int = FIRST_PAGE): DataResult<ActivityPage<Event>>

    suspend fun getSurveyDetails(surveyId: Long): DataResult<SurveyDetails>
    suspend fun getQuestDetails(questId: Long): DataResult<QuestDetails>
    suspend fun getEventDetails(eventId: Long): DataResult<EventDetails>

    /** @param answers ответы по id вопроса; вопросы без ответа не отправляются. */
    suspend fun submitSurvey(surveyId: Long, answers: Map<Long, SurveyAnswer>): ActivityActionResult

    suspend fun joinQuest(questId: Long): ActivityActionResult

    /** Отмечает выполненным шаг типа manual. */
    suspend fun completeQuestStep(questId: Long, stepId: Long): ActivityActionResult

    /** Завершает квест и начисляет награду. */
    suspend fun completeQuest(questId: Long): ActivityActionResult

    suspend fun registerForEvent(eventId: Long): ActivityActionResult
    suspend fun cancelEventRegistration(eventId: Long): ActivityActionResult
    suspend fun claimEventReward(eventId: Long): ActivityActionResult

    companion object {
        const val FIRST_PAGE = 1
    }
}
