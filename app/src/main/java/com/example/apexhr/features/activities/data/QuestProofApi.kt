package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.features.activities.data.dto.CompleteQuestStepRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Отправка подтверждения шага квеста; при отказе — [ActivitiesApiException] с текстом сервера. */
interface QuestProofApi {
    /** Первое подтверждение или повторное после отказа: POST …/steps/{stepId}/complete. */
    suspend fun completeStepWithProof(questId: Long, stepId: Long, request: CompleteQuestStepRequest)

    /** Замена подтверждения, которое ещё на проверке: PUT …/steps/{stepId}/proof. */
    suspend fun updateProof(questId: Long, stepId: Long, request: CompleteQuestStepRequest)
}

/** @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT). */
class KtorQuestProofApi(
    private val client: HttpClient,
) : QuestProofApi {
    override suspend fun completeStepWithProof(questId: Long, stepId: Long, request: CompleteQuestStepRequest) {
        client.post(NetworkConfig.questStepCompleteUrl(questId, stepId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireActionSuccess()
    }

    override suspend fun updateProof(questId: Long, stepId: Long, request: CompleteQuestStepRequest) {
        client.put(NetworkConfig.questStepProofUrl(questId, stepId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireActionSuccess()
    }
}