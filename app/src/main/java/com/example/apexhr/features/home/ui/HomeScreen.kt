package com.example.apexhr.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.features.home.ui.composables.ChallengeCard
import com.example.apexhr.features.home.ui.composables.HomeAchievementsRow
import com.example.apexhr.features.home.ui.composables.HomeGreeting
import com.example.apexhr.features.home.ui.composables.HomeHeader
import com.example.apexhr.features.home.ui.composables.LeaderboardRow
import com.example.apexhr.features.home.ui.composables.SectionTitle
import com.example.apexhr.features.home.ui.models.HomeScreenCallbacks
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
                points = uiState.points,
                onPointsClick = callbacks::onPointsClick,
            )
        }
        item {
            HomeGreeting(userName = uiState.userName)
        }
        item {
            SectionTitle(text = stringResource(R.string.home_active_challenges))
        }
        items(uiState.challenges, key = { it.id }) { challenge ->
            ChallengeCard(
                challenge = challenge,
                onClick = { callbacks.onChallengeClick(challenge.id) },
            )
        }
        item {
            SectionTitle(text = stringResource(R.string.home_leaderboard))
        }
        items(uiState.leaderboard, key = { it.id }) { entry ->
            LeaderboardRow(
                entry = entry,
                onClick = { callbacks.onLeaderboardEntryClick(entry.id) },
            )
        }
        item {
            SectionTitle(text = stringResource(R.string.home_achievements))
        }
        item {
            HomeAchievementsRow(
                achievements = uiState.achievements,
                onAchievementClick = callbacks::onAchievementClick,
            )
        }
    }
}
