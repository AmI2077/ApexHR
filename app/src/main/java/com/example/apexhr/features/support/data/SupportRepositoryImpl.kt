package com.example.apexhr.features.support.data

import com.example.apexhr.core.data.network.safeApiCall
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.support.data.dto.CreateSupportTicketRequest
import com.example.apexhr.features.support.data.dto.SendSupportMessageRequest
import com.example.apexhr.features.support.domain.SupportMessage
import com.example.apexhr.features.support.domain.SupportRepository
import com.example.apexhr.features.support.domain.SupportTicket
import com.example.apexhr.features.support.domain.SupportTicketsPage

class SupportRepositoryImpl(
    private val api: SupportApi,
) : SupportRepository {

    override suspend fun getTickets(page: Int, pageSize: Int): DataResult<SupportTicketsPage> =
        safeApiCall { api.getTickets(page, pageSize).toDomain() }

    override suspend fun getTicket(id: Long): DataResult<SupportTicket> =
        safeApiCall { api.getTicket(id).toDomain() ?: error("Обращение без id") }

    override suspend fun createTicket(subject: String, message: String): DataResult<SupportTicket> =
        safeApiCall {
            api.createTicket(CreateSupportTicketRequest(subject = subject, message = message)).toDomain()
                ?: error("Обращение без id")
        }

    override suspend fun sendMessage(ticketId: Long, message: String): DataResult<SupportMessage> =
        safeApiCall {
            api.sendMessage(ticketId, SendSupportMessageRequest(message = message)).toDomain()
                ?: error("Сообщение без id")
        }
}