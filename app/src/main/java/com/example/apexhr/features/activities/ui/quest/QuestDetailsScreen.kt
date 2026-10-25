package com.example.apexhr.features.activities.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.features.activities.ui.common.ActivityActionButton
import com.example.apexhr.features.activities.ui.common.ActivityDescription
import com.example.apexhr.features.activities.ui.common.ActivityDetailsContent
import com.example.apexhr.features.activities.ui.common.ActivityDetailsLayout
import com.example.apexhr.features.activities.ui.common.ActivityDetailsTitle
import com.example.apexhr.features.activities.ui.common.ActivityInfoText
import com.example.apexhr.features.activities.ui.common.ActivityMessage
import com.example.apexhr.features.activities.ui.common.ActivityMessagesEffect
import com.example.apexhr.features.activities.ui.common.ActivityNoteText
import com.example.apexhr.features.activities.ui.common.ActivityStatusText
import com.example.apexhr.features.activities.ui.quest.models.QuestAction
import com.example.apexhr.features.activities.ui.quest.models.QuestDetailsScreenCallbacks
import com.example.apexhr.features.activities.ui.quest.models.QuestDetailsScreenState
import com.example.apexhr.features.activities.ui.quest.models.QuestDetailsUiModel
import com.example.apexhr.features.activities.ui.quest.models.QuestStepUiModel
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun QuestDetailsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        QuestDetailsScreen(
            screenState = QuestDetailsScreenState.Content(QuestDetailsUiModel.Preview, pendingAction = null),
            callbacks = QuestDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun QuestDetailsScreenJoinPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        QuestDetailsScreen(
            screenState = QuestDetailsScreenState.Content(
                QuestDetailsUiModel.Preview.copy(
                    statusRes = null,
                    action = QuestAction.Join,
                    noteRes = null,
                ),
                pendingAction = QuestAction.Join,
            ),
            callbacks = QuestDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun QuestDetailsScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        QuestDetailsScreen(
            screenState = QuestDetailsScreenState.Error(DataError.Network),
            callbacks = QuestDetailsScreenCallbacks.Empty,
        )
    }
}

/**
 * Квест: награда, срок, статус участия и шаги. Ручной шаг отмечается кнопкой,
 * остальные только показывают состояние.
 * @param messages итог действий, показывается в snackbar.
 */
@Composable
fun QuestDetailsScreen(
    screenState: QuestDetailsScreenState,
    callbacks: QuestDetailsScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<ActivityMessage> = emptyFlow(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ActivityMessagesEffect(messages = messages, snackbarHostState = snackbarHostState)

    ActivityDetailsLayout(
        title = stringResource(R.string.quest_details_title),
        onBackClick = callbacks::onBackClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) {
        when (screenState) {
            QuestDetailsScreenState.Loading -> LoadingState()
            is QuestDetailsScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is QuestDetailsScreenState.Content -> QuestDetailsBody(
                quest = screenState.quest,
                pendingAction = screenState.pendingAction,
                onActionClick = callbacks::onActionClick,
            )
        }
    }
}

@Composable
private fun QuestDetailsBody(
    quest: QuestDetailsUiModel,
    pendingAction: QuestAction?,
    onActionClick: (QuestAction) -> Unit,
) {
    ActivityDetailsContent {
        ActivityDetailsTitle(title = quest.title)
        quest.statusRes?.let { ActivityStatusText(text = stringResource(it)) }
        quest.reward?.let {
            ActivityInfoText(
                text = stringResource(
                    R.string.quest_details_reward,
                    stringResource(R.string.activities_reward, it),
                ),
            )
        }
        quest.endAt?.let {
            ActivityInfoText(text = stringResource(R.string.quest_details_end, it))
        }
        quest.description?.takeIf { it.isNotBlank() }?.let { ActivityDescription(text = it) }
        if (quest.steps.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.quest_details_steps,
                    quest.completedStepsCount,
                    quest.steps.size,
                ),
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            quest.steps.forEach { step ->
                QuestStepCard(
                    step = step,
                    pendingAction = pendingAction,
                    onCompleteClick = { onActionClick(QuestAction.CompleteStep(step.id)) },
                )
            }
        }
        quest.noteRes?.let { ActivityNoteText(text = stringResource(it)) }
        quest.action?.let { action ->
            ActivityActionButton(
                text = stringResource(
                    if (action == QuestAction.Join) R.string.quest_action_join else R.string.quest_action_complete,
                ),
                inProgress = pendingAction == action,
                enabled = pendingAction == null,
                onClick = { onActionClick(action) },
            )
        }
    }
}

@Composable
private fun QuestStepCard(
    step: QuestStepUiModel,
    pendingAction: QuestAction?,
    onCompleteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(ApexDimens.spacing12.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing6.dp),
    ) {
        Text(
            text = step.title ?: stringResource(R.string.activities_untitled),
            color = primaryTextColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        step.description?.takeIf { it.isNotBlank() }?.let { ActivityInfoText(text = it) }
        if (!step.isRequired) {
            ActivityNoteText(text = stringResource(R.string.quest_step_optional))
        }
        step.stateRes?.let {
            if (step.isCompleted) ActivityStatusText(text = stringResource(it)) else ActivityNoteText(text = stringResource(it))
        }
        if (step.canComplete) {
            ActivityActionButton(
                text = stringResource(R.string.quest_action_complete_step),
                inProgress = pendingAction == QuestAction.CompleteStep(step.id),
                enabled = pendingAction == null,
                onClick = onCompleteClick,
            )
        }
    }
}
