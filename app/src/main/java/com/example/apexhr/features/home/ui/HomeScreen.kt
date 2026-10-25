package com.example.apexhr.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.composables.EmptyState
import com.example.apexhr.core.ui.composables.ErrorState
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.features.home.ui.composables.ChallengeCard
import com.example.apexhr.features.home.ui.composables.HomeAchievementsRow
import com.example.apexhr.features.home.ui.composables.HomeGreeting
import com.example.apexhr.features.home.ui.composables.HomeHeader
import com.example.apexhr.features.home.ui.composables.LeaderboardRow
import com.example.apexhr.features.home.ui.composables.SectionTitle
import com.example.apexhr.features.home.ui.models.HomeScreenCallbacks
import com.example.apexhr.features.home.ui.models.HomeScreenState
import com.example.apexhr.features.home.ui.models.HomeUiState
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun HomeScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        HomeScreen(
            uiState = HomeUiState.Preview,
            callbacks = HomeScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun HomeScreenEmptyPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        HomeScreen(
            uiState = HomeUiState.Preview.copy(
                challenges = emptyList(),
                leaderboard = emptyList(),
                achievements = emptyList(),
            ),
            callbacks = HomeScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun HomeScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        HomeScreen(
            screenState = HomeScreenState.Error(DataError.Network),
            callbacks = HomeScreenCallbacks.Empty,
        )
    }
}

@Composable
fun HomeScreen(
    screenState: HomeScreenState,
    callbacks: HomeScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    when (screenState) {
        is HomeScreenState.Content -> HomeScreenContent(
            uiState = screenState.uiState,
            callbacks = callbacks,
            modifier = modifier,
        )

        HomeScreenState.Loading -> HomeScreenFrame(points = null, callbacks, modifier) {
            item { LoadingState() }
        }

        is HomeScreenState.Error -> HomeScreenFrame(points = null, callbacks, modifier) {
            item {
                ErrorState(
                    error = screenState.error,
                    onRetry = callbacks::onRetryClick,
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    callbacks: HomeScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    HomeScreenContent(
        uiState = uiState,
        callbacks = callbacks,
        modifier = modifier,
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    callbacks: HomeScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    HomeScreenFrame(points = uiState.points, callbacks, modifier) {
        item {
            HomeGreeting(userName = uiState.userName)
        }
        item {
            SectionTitle(text = stringResource(R.string.home_active_challenges))
        }
        if (uiState.challenges.isEmpty()) {
            item { EmptyState(text = stringResource(R.string.home_challenges_empty)) }
        }
        items(uiState.challenges, key = { "challenge_${it.id}" }) { challenge ->
            ChallengeCard(
                challenge = challenge,
                onClick = { callbacks.onChallengeClick(challenge.id) },
            )
        }
        item {
            SectionTitle(text = stringResource(R.string.home_leaderboard))
        }
        if (uiState.leaderboard.isEmpty()) {
            item { EmptyState(text = stringResource(R.string.home_leaderboard_empty)) }
        }
        items(uiState.leaderboard, key = { "leader_${it.id}" }) { entry ->
            LeaderboardRow(
                entry = entry,
                onClick = { callbacks.onLeaderboardEntryClick(entry.id) },
            )
        }
        item {
            SectionTitle(text = stringResource(R.string.home_achievements))
        }
        item {
            if (uiState.achievements.isEmpty()) {
                EmptyState(text = stringResource(R.string.home_achievements_empty))
            } else {
                HomeAchievementsRow(
                    achievements = uiState.achievements,
                    onAchievementClick = callbacks::onAchievementClick,
                )
            }
        }
    }
}

/** Общий каркас: фон, отступы и шапка с логотипом и балансом (если он уже загружен). */
@Composable
private fun HomeScreenFrame(
    points: String?,
    callbacks: HomeScreenCallbacks,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentPadding = PaddingValues(
            horizontal = ApexDimens.spacing20.dp,
            vertical = ApexDimens.spacing16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp),
    ) {
        item {
            HomeHeader(
                points = points,
                onPointsClick = callbacks::onPointsClick,
            )
        }
        content()
    }
}
