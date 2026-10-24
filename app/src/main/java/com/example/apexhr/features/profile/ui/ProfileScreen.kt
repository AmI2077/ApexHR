package com.example.apexhr.features.profile.ui

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
import com.example.apexhr.features.profile.ui.composables.ProfileAchievementRow
import com.example.apexhr.features.profile.ui.composables.ProfileBalanceBlock
import com.example.apexhr.features.profile.ui.composables.ProfileHeader
import com.example.apexhr.features.profile.ui.composables.ProfileTabs
import com.example.apexhr.features.profile.ui.composables.ProfileUserInfo
import com.example.apexhr.features.profile.ui.composables.TransactionRow
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.profile.ui.models.ProfileScreenState
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.ProfileUiState
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ProfileScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ProfileScreen(
            uiState = ProfileUiState.Preview,
            callbacks = ProfileScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ProfileScreenLoadingPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ProfileScreen(
            screenState = ProfileScreenState.Loading,
            callbacks = ProfileScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ProfileScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ProfileScreen(
            screenState = ProfileScreenState.Error(DataError.Network),
            callbacks = ProfileScreenCallbacks.Empty,
        )
    }
}

/** Экран профиля с учётом загрузки: шапка с выходом видна всегда. */
@Composable
fun ProfileScreen(
    screenState: ProfileScreenState,
    callbacks: ProfileScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    when (screenState) {
        is ProfileScreenState.Content -> ProfileScreenContent(
            uiState = screenState.uiState,
            callbacks = callbacks,
            modifier = modifier,
        )

        ProfileScreenState.Loading -> ProfileScreenFrame(callbacks, modifier) {
            item { LoadingState() }
        }

        is ProfileScreenState.Error -> ProfileScreenFrame(callbacks, modifier) {
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
fun ProfileScreen(
    uiState: ProfileUiState,
    callbacks: ProfileScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    ProfileScreenContent(
        uiState = uiState,
        callbacks = callbacks,
        modifier = modifier,
    )
}

@Composable
fun ProfileScreenContent(
    uiState: ProfileUiState,
    callbacks: ProfileScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    ProfileScreenFrame(callbacks, modifier) {
        item {
            ProfileUserInfo(
                name = uiState.name,
                department = uiState.department,
                avatarUrl = uiState.avatarUrl,
            )
        }
        item {
            ProfileBalanceBlock(
                balance = uiState.balance,
                hint = stringResource(uiState.balanceHintRes),
            )
        }
        item {
            ProfileTabs(
                selectedTab = uiState.selectedTab,
                onTabClick = callbacks::onTabClick,
            )
        }
        if (uiState.selectedTab == ProfileTab.Thanks) {
            if (uiState.transactions.isEmpty()) {
                item {
                    EmptyState(text = stringResource(R.string.profile_transactions_empty))
                }
            }
            items(uiState.transactions, key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onClick = { callbacks.onTransactionClick(transaction.id) },
                )
            }
        } else {
            if (uiState.achievements.isEmpty()) {
                item {
                    EmptyState(text = stringResource(R.string.profile_achievements_empty))
                }
            }
            items(uiState.achievements, key = { it.id }) { achievement ->
                ProfileAchievementRow(
                    achievement = achievement,
                    onClick = { callbacks.onAchievementClick(achievement.id) },
                )
            }
        }
    }
}

/** Общий каркас: фон, отступы и шапка с кнопкой выхода. */
@Composable
private fun ProfileScreenFrame(
    callbacks: ProfileScreenCallbacks,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentPadding = PaddingValues(
            horizontal = ApexDimens.spacing20.dp,
            vertical = ApexDimens.spacing8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp),
    ) {
        item {
            ProfileHeader(
                onBackClick = callbacks::onBackClick,
                onHistoryClick = callbacks::onHistoryClick,
                onLogoutClick = callbacks::onLogoutClick,
            )
        }
        content()
    }
}
