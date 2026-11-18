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
import com.example.apexhr.features.home.ui.composables.HomeAchievementsList
import com.example.apexhr.features.home.ui.composables.HomeGreeting
import com.example.apexhr.features.home.ui.composables.HomeHeader
import com.example.apexhr.features.home.ui.composables.HomeLeaderboardCard
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
    unreadNotifications: Int = 0,
) {
    when (screenState) {
        is HomeScreenState.Content -> HomeScreenContent(
            uiState = screenState.uiState,
            callbacks = callbacks,
            modifier = modifier,
            unreadNotifications = unreadNotifications,
        )

        HomeScreenState.Loading -> HomeScreenFrame(balance = null, unreadNotifications, callbacks, modifier) {
            item { LoadingState() }
        }

        is HomeScreenState.Error -> HomeScreenFrame(balance = null, unreadNotifications, callbacks, modifier) {
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
    unreadNotifications: Int = 0,
) {
    HomeScreenFrame(balance = uiState.balance, unreadNotifications, callbacks, modifier) {
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
        item(key = "leaderboard") {
            HomeLeaderboardCard(
                entries = uiState.leaderboard,
                onEntryClick = callbacks::onLeaderboardEntryClick,
            )
        }
        item {
            SectionTitle(text = stringResource(R.string.home_achievements))
        }
        item {
            if (uiState.achievements.isEmpty()) {
                EmptyState(text = stringResource(R.string.home_achievements_empty))
            } else {
                HomeAchievementsList(
                    achievements = uiState.achievements,
                    onAchievementClick = callbacks::onAchievementClick,
                )
            }
        }
    }
}

/**
 * Общий каркас: фон, отступы и шапка с логотипом, колокольчиком уведомлений
 * и балансом (если он уже загружен).
 */
@Composable
private fun HomeScreenFrame(
    balance: Long?,
    unreadNotifications: Int,
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
                balance = balance,
                onPointsClick = callbacks::onPointsClick,
                unreadNotifications = unreadNotifications,
                onNotificationsClick = callbacks::onNotificationsClick,
            )
        }
        content()
    }
}
