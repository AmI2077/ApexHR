package com.example.apexhr.features.support.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SupportTicketSummaryDto(
    val id: Long? = null,
    val subject: String? = null,
    val status: String? = null,
    val messagesCount: Int? = null,
    val lastMessageAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class SupportTicketsPageDto(
    val items: List<SupportTicketSummaryDto>? = null,
    val page: Int = 1,
    val totalPages: Int = 0,
    val hasNextPage: Boolean? = null,
)

@Serializable
data class SupportMessageDto(
    val id: Long? = null,
    val userId: String? = null,
    val userName: String? = null,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val userMiddleName: String? = null,
    val message: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class SupportTicketDetailsDto(
    val id: Long? = null,
    val subject: String? = null,
    val status: String? = null,
    val userId: String? = null,
    val messages: List<SupportMessageDto>? = null,
)

@Serializable
data class CreateSupportTicketRequest(
    val subject: String,
    val message: String,
)

@Serializable
data class SendSupportMessageRequest(
    val message: String,
)