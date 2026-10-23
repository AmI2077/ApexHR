package com.example.apexhr.features.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.features.profile.ui.composables.ProfileAchievementRow
import com.example.apexhr.features.profile.ui.composables.ProfileBalanceBlock
import com.example.apexhr.features.profile.ui.composables.ProfileHeader
import com.example.apexhr.features.profile.ui.composables.ProfileTabs
import com.example.apexhr.features.profile.ui.composables.ProfileUserInfo
import com.example.apexhr.features.profile.ui.composables.TransactionRow
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
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
        item {
            ProfileUserInfo(
                name = uiState.name,
                department = uiState.department,
            )
        }
        item {
            ProfileBalanceBlock(
                balance = uiState.balance,
                hint = uiState.balanceHint,
            )
        }
        item {
            ProfileTabs(
                selectedTab = uiState.selectedTab,
                onTabClick = callbacks::onTabClick,
            )
        }
        if (uiState.selectedTab == ProfileTab.Thanks) {
            items(uiState.transactions, key = { it.id }) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onClick = { callbacks.onTransactionClick(transaction.id) },
                )
            }
        } else {
            items(uiState.achievements, key = { it.id }) { achievement ->
                ProfileAchievementRow(
                    achievement = achievement,
                    onClick = { callbacks.onAchievementClick(achievement.id) },
                )
            }
        }
    }
}
