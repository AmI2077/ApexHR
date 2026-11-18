package com.example.apexhr.features.support.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import com.example.apexhr.features.support.data.dto.CreateSupportTicketRequest
import com.example.apexhr.features.support.data.dto.SendSupportMessageRequest
import com.example.apexhr.features.support.data.dto.SupportMessageDto
import com.example.apexhr.features.support.data.dto.SupportTicketDetailsDto
import com.example.apexhr.features.support.data.dto.SupportTicketsPageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface SupportApi {
    suspend fun getTickets(page: Int, pageSize: Int): SupportTicketsPageDto
    suspend fun getTicket(id: Long): SupportTicketDetailsDto
    suspend fun createTicket(request: CreateSupportTicketRequest): SupportTicketDetailsDto
    suspend fun sendMessage(ticketId: Long, request: SendSupportMessageRequest): SupportMessageDto
}

/**
 * @param client авторизованный клиент (AUTHORIZED_HTTP_CLIENT): подставляет
 * токен и на 401 завершает сессию.
 */
class KtorSupportApi(
    private val client: HttpClient,
) : SupportApi {
    override suspend fun getTickets(page: Int, pageSize: Int): SupportTicketsPageDto =
        client.get(NetworkConfig.SUPPORT_TICKETS_URL) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.requireSuccess().body()

    override suspend fun getTicket(id: Long): SupportTicketDetailsDto =
        client.get(NetworkConfig.supportTicketUrl(id)).requireSuccess().body()

    override suspend fun createTicket(request: CreateSupportTicketRequest): SupportTicketDetailsDto =
        client.post(NetworkConfig.SUPPORT_TICKETS_URL) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireSuccess().body()

    override suspend fun sendMessage(ticketId: Long, request: SendSupportMessageRequest): SupportMessageDto =
        client.post(NetworkConfig.supportTicketMessagesUrl(ticketId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireSuccess().body()
}