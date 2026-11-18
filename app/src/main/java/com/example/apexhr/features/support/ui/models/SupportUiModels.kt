package com.example.apexhr.features.support.ui.models

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.support.domain.SupportRepository
import com.example.apexhr.features.support.domain.SupportTicketStatus

/** Обращение в списке. */
data class SupportTicketItemUiModel(
    val id: Long,
    val subject: String?,
    val status: SupportTicketStatus,
    val messagesCount: Int,
    val updatedAt: String?,
)

data class SupportListUiState(
    val items: List<SupportTicketItemUiModel>,
    val hasNextPage: Boolean,
    val isLoadingMore: Boolean = false,
) {
    companion object {
        val Preview = SupportListUiState(
            items = listOf(
                SupportTicketItemUiModel(1, "Не начислились апексы за квест", SupportTicketStatus.InProgress, 3, "12.11.2026 14:05"),
                SupportTicketItemUiModel(2, "Как сменить аватар", SupportTicketStatus.Resolved, 2, "02.11.2026 09:40"),
            ),
            hasNextPage = false,
        )
    }
}

sealed interface SupportListScreenState {
    data object Loading : SupportListScreenState
    data class Error(val error: DataError) : SupportListScreenState
    data class Content(val uiState: SupportListUiState) : SupportListScreenState
}

/** Сообщение переписки; [isFromAuthor] — написано автором обращения, а не поддержкой. */
data class SupportMessageUiModel(
    val id: Long,
    val authorName: String?,
    val text: String,
    val createdAt: String?,
    val isFromAuthor: Boolean,
)

data class SupportTicketUiState(
    val id: Long,
    val subject: String?,
    val status: SupportTicketStatus,
    val messages: List<SupportMessageUiModel>,
    val reply: String = "",
    val isSending: Boolean = false,
) {
    /** Поле ответа показывается только у открытых обращений и обращений в работе. */
    val canReply: Boolean
        get() = status.acceptsMessages

    val canSend: Boolean
        get() = canReply && reply.isNotBlank() && !isSending

    companion object {
        val Preview = SupportTicketUiState(
            id = 1,
            subject = "Не начислились апексы за квест",
            status = SupportTicketStatus.InProgress,
            messages = listOf(
                SupportMessageUiModel(1, "Иванова Анна", "Прошла квест, а апексы не пришли.", "12.11.2026 14:05", true),
                SupportMessageUiModel(2, "Петров Олег", "Проверяем, напишем сегодня.", "12.11.2026 15:20", false),
            ),
        )
    }
}

sealed interface SupportTicketScreenState {
    data object Loading : SupportTicketScreenState
    data class Error(val error: DataError) : SupportTicketScreenState
    data class Content(val uiState: SupportTicketUiState) : SupportTicketScreenState
}

data class SupportNewTicketUiState(
    val subject: String = "",
    val message: String = "",
    val isSending: Boolean = false,
) {
    val canSend: Boolean
        get() = subject.isNotBlank() && message.isNotBlank() && !isSending

    companion object {
        const val SUBJECT_MAX_LENGTH = SupportRepository.SUBJECT_MAX_LENGTH
        const val MESSAGE_MAX_LENGTH = SupportRepository.MESSAGE_MAX_LENGTH
    }
}