package com.example.apexhr.features.activities.ui.my

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.activities.ui.common.ActivitySubscreenHeader
import com.example.apexhr.features.activities.ui.my.models.MyAchievementUiModel
import com.example.apexhr.features.activities.ui.my.models.MyActivityRowUiModel
import com.example.apexhr.features.activities.ui.my.models.MyActivityScreenCallbacks
import com.example.apexhr.features.activities.ui.my.models.MyActivityScreenState
import com.example.apexhr.features.activities.ui.my.models.MyActivityUiState
import com.example.apexhr.features.engagement.ui.ActivityLevelCard
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MyActivityScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        MyActivityScreen(
            screenState = MyActivityScreenState.Content(MyActivityUiState.Preview),
            callbacks = MyActivityScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun MyActivityScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        MyActivityScreen(
            screenState = MyActivityScreenState.Error(DataError.Network),
            callbacks = MyActivityScreenCallbacks.Empty,
        )
    }
}

/**
 * «Моя активность»: открывается ссылкой из карточки уровня на главной, как в вебе.
 * Данные перечитываются при каждом показе (после квеста или опроса статус меняется).
 */
@Composable
fun MyActivityScreen(
    screenState: MyActivityScreenState,
    callbacks: MyActivityScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(callbacks) { callbacks.onScreenShown() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        ActivitySubscreenHeader(
            title = stringResource(R.string.my_activity_title),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        when (screenState) {
            MyActivityScreenState.Loading -> LoadingState()
            is MyActivityScreenState.Error -> ErrorState(error = screenState.error, onRetry = callbacks::onRetryClick)
            is MyActivityScreenState.Content -> MyActivityContent(uiState = screenState.uiState, callbacks = callbacks)
        }
    }
}

@Composable
private fun MyActivityContent(
    uiState: MyActivityUiState,
    callbacks: MyActivityScreenCallbacks,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ApexDimens.spacing20.dp,
            end = ApexDimens.spacing20.dp,
            bottom = ApexDimens.spacing16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp),
    ) {
        item(key = "subtitle") {
            Text(
                text = stringResource(R.string.my_activity_subtitle),
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
            )
        }
        item(key = "level") {
            ActivityLevelCard(level = uiState.level, detailed = true)
        }
        item(key = "quests") {
            MyActivityBlock(
                title = stringResource(R.string.my_activity_quests),
                emptyText = stringResource(R.string.my_activity_quests_empty),
                rows = uiState.quests,
                onRowClick = callbacks::onQuestClick,
            )
        }
        item(key = "surveys") {
            MyActivityBlock(
                title = stringResource(R.string.my_activity_surveys),
                emptyText = stringResource(R.string.my_activity_surveys_empty),
                rows = uiState.surveys,
                onRowClick = callbacks::onSurveyClick,
            )
        }
        item(key = "events") {
            MyActivityBlock(
                title = stringResource(R.string.my_activity_events),
                emptyText = stringResource(R.string.my_activity_events_empty),
                rows = uiState.events,
                onRowClick = callbacks::onEventClick,
            )
        }
        item(key = "achievements") {
            MyAchievementsBlock(achievements = uiState.achievements)
        }
        if (uiState.isEmpty) {
            item(key = "all_activities") {
                Text(
                    text = stringResource(R.string.my_activity_all_activities),
                    color = primaryBlueColor,
                    fontSize = ApexDimens.text14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(ApexDimens.radius8.dp))
                        .clickable(onClick = callbacks::onAllActivitiesClick)
                        .padding(vertical = ApexDimens.spacing4.dp),
                )
            }
        }
    }
}

/** Карточка раздела: заголовок и строки или текст «пока пусто». */
@Composable
private fun MyActivityBlockFrame(
    title: String,
    emptyText: String,
    isEmpty: Boolean,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(pillBackgroundColor)
            .padding(ApexDimens.spacing16.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
    ) {
        Text(
            text = title,
            color = primaryTextColor,
            fontSize = ApexDimens.text16.sp,
            fontWeight = FontWeight.Bold,
        )
        if (isEmpty) {
            Text(text = emptyText, color = secondaryTextColor, fontSize = ApexDimens.text14.sp)
        } else {
            content()
        }
    }
}

@Composable
private fun MyActivityBlock(
    title: String,
    emptyText: String,
    rows: List<MyActivityRowUiModel>,
    onRowClick: (Long) -> Unit,
) {
    MyActivityBlockFrame(title = title, emptyText = emptyText, isEmpty = rows.isEmpty()) {
        rows.forEach { row ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ApexDimens.radius12.dp))
                    .background(backgroundColor)
                    .clickable { onRowClick(row.id) }
                    .padding(horizontal = ApexDimens.spacing12.dp, vertical = ApexDimens.spacing8.dp),
            ) {
                Text(
                    text = row.title ?: stringResource(R.string.my_activity_untitled),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
                ) {
                    Text(
                        text = stringResource(row.statusRes) + if (row.reward != null) " ·" else "",
                        color = secondaryTextColor,
                        fontSize = ApexDimens.text12.sp,
                    )
                    if (row.reward != null) {
                        ApexAmount(amount = row.reward, color = secondaryTextColor, fontSize = ApexDimens.text12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MyAchievementsBlock(achievements: List<MyAchievementUiModel>) {
    MyActivityBlockFrame(
        title = stringResource(R.string.my_activity_achievements),
        emptyText = stringResource(R.string.my_activity_achievements_empty),
        isEmpty = achievements.isEmpty(),
    ) {
        achievements.forEach { achievement ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ApexDimens.radius12.dp))
                    .background(backgroundColor)
                    .padding(horizontal = ApexDimens.spacing12.dp, vertical = ApexDimens.spacing8.dp),
            ) {
                Text(
                    text = achievement.title ?: stringResource(R.string.my_activity_untitled),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!achievement.description.isNullOrBlank()) {
                    Text(
                        text = achievement.description,
                        color = secondaryTextColor,
                        fontSize = ApexDimens.text12.sp,
                    )
                }
            }
        }
    }
}
