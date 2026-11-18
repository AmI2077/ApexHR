package com.example.apexhr.features.activities.ui.quest.proof.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.activities.domain.QuestProofError
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.common.toActivityMessage
import com.example.apexhr.features.activities.ui.quest.models.QuestProofAction

/** Максимум символов комментария к подтверждению. */
const val PROOF_COMMENT_MAX_LENGTH = 1000

/**
 * @param action отправить, отправить заново или заменить подтверждение на проверке.
 * @param existingStoragePath уже отправленный файл: при замене его можно оставить.
 * @param fileUri выбранный новый файл (content://).
 */
data class QuestStepProofUiState(
    val stepTitle: String?,
    val stepDescription: String?,
    val action: QuestProofAction,
    val existingStoragePath: String?,
    val fileUri: String? = null,
    val comment: String = "",
    val isSending: Boolean = false,
) {
    /** При замене без нового файла остаётся прежний. */
    val keepsExistingFile: Boolean
        get() = fileUri == null && action == QuestProofAction.Update && existingStoragePath != null

    val canSend: Boolean
        get() = !isSending && (fileUri != null || keepsExistingFile)

    companion object {
        val Preview = QuestStepProofUiState(
            stepTitle = "Фото с командного завтрака",
            stepDescription = "Приложите фото или PDF",
            action = QuestProofAction.Submit,
            existingStoragePath = null,
            fileUri = "content://preview/photo.jpg",
            comment = "Были всей командой",
        )
    }
}

sealed interface QuestStepProofScreenState {
    data object Loading : QuestStepProofScreenState
    data class Error(val error: DataError) : QuestStepProofScreenState

    /** Шаг сейчас не принимает подтверждение (выполнен, принят или вы не участвуете). */
    data object Unavailable : QuestStepProofScreenState
    data class Content(val uiState: QuestStepProofUiState) : QuestStepProofScreenState
}

interface QuestStepProofScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()

    /** @param uri выбранный файл (content://). */
    fun onFileSelected(uri: String)
    fun onCommentChange(text: String)
    fun onSendClick()

    companion object {
        val Empty = object : QuestStepProofScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onFileSelected(uri: String) = Unit
            override fun onCommentChange(text: String) = Unit
            override fun onSendClick() = Unit
        }
    }
}

@StringRes
fun QuestProofAction.sendButtonRes(): Int = when (this) {
    QuestProofAction.Submit -> R.string.quest_action_send_proof
    QuestProofAction.Resubmit -> R.string.quest_action_resend_proof
    QuestProofAction.Update -> R.string.quest_action_update_proof
}

/** Текст ошибки отправки подтверждения. */
fun QuestProofError.toActivityMessage(): ActivityMessage = when (this) {
    QuestProofError.NoFile -> ActivityMessage(R.string.quest_proof_error_no_file)
    QuestProofError.FileUnreadable -> ActivityMessage(R.string.quest_proof_error_unreadable)
    QuestProofError.UnsupportedType -> ActivityMessage(R.string.quest_proof_error_type)
    QuestProofError.FileTooLarge -> ActivityMessage(R.string.quest_proof_error_too_large)
    QuestProofError.SessionMismatch -> ActivityMessage(R.string.quest_proof_error_session)
    QuestProofError.UploadRejected -> ActivityMessage(R.string.quest_proof_error_upload_rejected)
    is QuestProofError.Action -> error.toActivityMessage()
}