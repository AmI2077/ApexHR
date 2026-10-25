package com.example.apexhr.features.activities.ui.event

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
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
import com.example.apexhr.features.activities.ui.event.models.EventAction
import com.example.apexhr.features.activities.ui.event.models.EventDetailsScreenCallbacks
import com.example.apexhr.features.activities.ui.event.models.EventDetailsScreenState
import com.example.apexhr.features.activities.ui.event.models.EventDetailsUiModel
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun EventDetailsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        EventDetailsScreen(
            screenState = EventDetailsScreenState.Content(EventDetailsUiModel.Preview, actionInProgress = false),
            callbacks = EventDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun EventDetailsScreenFullPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        EventDetailsScreen(
            screenState = EventDetailsScreenState.Content(
                EventDetailsUiModel.Preview.copy(
                    registeredCount = 5,
                    statusRes = null,
                    action = null,
                    noteRes = R.string.event_note_full,
                ),
                actionInProgress = false,
            ),
            callbacks = EventDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun EventDetailsScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        EventDetailsScreen(
            screenState = EventDetailsScreenState.Error(DataError.Network),
            callbacks = EventDetailsScreenCallbacks.Empty,
        )
    }
}

/**
 * Мероприятие: даты, место, места, награда, статус записи и одно доступное действие.
 * @param messages итог действий, показывается в snackbar.
 */
@Composable
fun EventDetailsScreen(
    screenState: EventDetailsScreenState,
    callbacks: EventDetailsScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<ActivityMessage> = emptyFlow(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ActivityMessagesEffect(messages = messages, snackbarHostState = snackbarHostState)

    ActivityDetailsLayout(
        title = stringResource(R.string.event_details_title),
        onBackClick = callbacks::onBackClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) {
        when (screenState) {
            EventDetailsScreenState.Loading -> LoadingState()
            is EventDetailsScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is EventDetailsScreenState.Content -> EventDetailsBody(
                event = screenState.event,
                actionInProgress = screenState.actionInProgress,
                onActionClick = callbacks::onActionClick,
            )
        }
    }
}

@Composable
private fun EventDetailsBody(
    event: EventDetailsUiModel,
    actionInProgress: Boolean,
    onActionClick: (EventAction) -> Unit,
) {
    ActivityDetailsContent {
        ActivityDetailsTitle(title = event.title)
        event.statusRes?.let { ActivityStatusText(text = stringResource(it)) }
        event.period?.let {
            ActivityInfoText(text = stringResource(R.string.event_details_period, it))
        }
        event.location?.let {
            ActivityInfoText(text = stringResource(R.string.event_details_location, it))
        }
        val participants = event.maxParticipants?.let {
            stringResource(R.string.event_details_participants_limited, event.registeredCount, it)
        } ?: stringResource(R.string.event_details_participants, event.registeredCount)
        ActivityInfoText(text = participants)
        event.reward?.let {
            ActivityInfoText(
                text = stringResource(
                    R.string.event_details_reward,
                    stringResource(R.string.activities_reward, it),
                ),
            )
        }
        event.description?.takeIf { it.isNotBlank() }?.let { ActivityDescription(text = it) }
        event.noteRes?.let { ActivityNoteText(text = stringResource(it)) }
        event.action?.let { action ->
            ActivityActionButton(
                text = stringResource(action.labelRes()),
                inProgress = actionInProgress,
                onClick = { onActionClick(action) },
            )
        }
    }
}

private fun EventAction.labelRes(): Int = when (this) {
    EventAction.Register -> R.string.event_action_register
    EventAction.CancelRegistration -> R.string.event_action_cancel
    EventAction.ClaimReward -> R.string.event_action_claim_reward
}
