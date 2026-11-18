package com.example.apexhr.features.activities.ui.quest.proof

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.core.ui.theme.transparent
import com.example.apexhr.features.activities.domain.ProofFileType
import com.example.apexhr.features.activities.ui.common.ActivityActionButton
import com.example.apexhr.features.activities.ui.common.ActivityDetailsContent
import com.example.apexhr.features.activities.ui.common.ActivityDetailsLayout
import com.example.apexhr.features.activities.ui.common.ActivityDetailsTitle
import com.example.apexhr.features.activities.ui.common.ActivityInfoText
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.common.ActivityMessagesEffect
import com.example.apexhr.features.activities.ui.common.ActivityNoteText
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofScreenCallbacks
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofScreenState
import com.example.apexhr.features.activities.ui.quest.proof.models.QuestStepProofUiState
import com.example.apexhr.features.activities.ui.quest.proof.models.sendButtonRes
import com.example.apexhr.features.activities.ui.quest.models.QuestProofAction
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun QuestStepProofScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        QuestStepProofScreen(
            screenState = QuestStepProofScreenState.Content(QuestStepProofUiState.Preview),
            callbacks = QuestStepProofScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun QuestStepProofScreenUpdatePreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        QuestStepProofScreen(
            screenState = QuestStepProofScreenState.Content(
                QuestStepProofUiState.Preview.copy(
                    action = QuestProofAction.Update,
                    existingStoragePath = "https://example.com/proof.pdf",
                    fileUri = null,
                )
            ),
            callbacks = QuestStepProofScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun QuestStepProofScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        QuestStepProofScreen(
            screenState = QuestStepProofScreenState.Error(DataError.Network),
            callbacks = QuestStepProofScreenCallbacks.Empty,
        )
    }
}

/** Типы файлов, которые предлагает системный выбор файла. */
private val PROOF_MIME_TYPES = ProofFileType.entries.map { it.mimeType }.toTypedArray()

/**
 * Подтверждение шага квеста: выбор файла, комментарий и отправка.
 * @param messages ошибки отправки, показываются в snackbar.
 */
@Composable
fun QuestStepProofScreen(
    screenState: QuestStepProofScreenState,
    callbacks: QuestStepProofScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<ActivityMessage> = emptyFlow(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ActivityMessagesEffect(messages = messages, snackbarHostState = snackbarHostState)

    ActivityDetailsLayout(
        title = stringResource(R.string.quest_proof_title),
        onBackClick = callbacks::onBackClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) {
        when (screenState) {
            QuestStepProofScreenState.Loading -> LoadingState()
            is QuestStepProofScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            QuestStepProofScreenState.Unavailable -> ActivityDetailsContent {
                ActivityNoteText(text = stringResource(R.string.quest_proof_unavailable))
            }

            is QuestStepProofScreenState.Content -> QuestStepProofBody(
                uiState = screenState.uiState,
                callbacks = callbacks,
            )
        }
    }
}

@Composable
private fun QuestStepProofBody(
    uiState: QuestStepProofUiState,
    callbacks: QuestStepProofScreenCallbacks,
) {
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { callbacks.onFileSelected(it.toString()) }
    }
    ActivityDetailsContent {
        ActivityDetailsTitle(title = uiState.stepTitle)
        uiState.stepDescription?.takeIf { it.isNotBlank() }?.let { ActivityInfoText(text = it) }
        if (uiState.action == QuestProofAction.Resubmit) {
            ActivityNoteText(text = stringResource(R.string.quest_step_proof_rejected))
        }
        ActivityNoteText(text = stringResource(R.string.quest_proof_file_hint))
        OutlinedButton(
            onClick = { pickFile.launch(PROOF_MIME_TYPES) },
            enabled = !uiState.isSending,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(imageVector = Icons.Outlined.AttachFile, contentDescription = null, tint = primaryBlueColor)
            Text(
                text = stringResource(
                    if (uiState.fileUri == null) R.string.quest_proof_pick_file else R.string.quest_proof_change_file
                ),
                color = primaryBlueColor,
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = ApexDimens.spacing8.dp),
            )
        }
        val fileText = when {
            uiState.fileUri != null -> stringResource(
                R.string.quest_proof_file_selected,
                rememberDisplayName(uiState.fileUri) ?: stringResource(R.string.quest_proof_file_unnamed),
            )

            uiState.keepsExistingFile -> stringResource(R.string.quest_proof_file_kept)
            else -> stringResource(R.string.quest_proof_file_none)
        }
        Text(text = fileText, color = primaryTextColor, fontSize = ApexDimens.text14.sp)
        TextField(
            value = uiState.comment,
            onValueChange = callbacks::onCommentChange,
            enabled = !uiState.isSending,
            minLines = 3,
            placeholder = {
                Text(
                    text = stringResource(R.string.quest_proof_comment_placeholder),
                    fontSize = ApexDimens.text14.sp,
                    color = secondaryTextColor,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(ApexDimens.radius12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = pillBackgroundColor,
                unfocusedContainerColor = pillBackgroundColor,
                disabledContainerColor = pillBackgroundColor,
                focusedIndicatorColor = transparent,
                unfocusedIndicatorColor = transparent,
                disabledIndicatorColor = transparent,
                cursorColor = primaryBlueColor,
                focusedTextColor = primaryTextColor,
                unfocusedTextColor = primaryTextColor,
            ),
        )
        ActivityActionButton(
            text = stringResource(uiState.action.sendButtonRes()),
            inProgress = uiState.isSending,
            enabled = uiState.canSend,
            onClick = callbacks::onSendClick,
        )
    }
}

/** Имя выбранного файла из ContentResolver; null — неизвестно. */
@Composable
private fun rememberDisplayName(uri: String): String? {
    val context = LocalContext.current
    return remember(uri) {
        runCatching {
            context.contentResolver.query(Uri.parse(uri), arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}