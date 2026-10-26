package com.example.apexhr.features.profile.ui

import androidx.annotation.StringRes
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
import com.example.apexhr.features.profile.ui.composables.ProfileSendThanksButton
import com.example.apexhr.features.profile.ui.composables.ProfileTabs
import com.example.apexhr.features.profile.ui.composables.ProfileUserInfo
import com.example.apexhr.features.profile.ui.composables.ProfileSectionTitle
import com.example.apexhr.features.profile.ui.composables.ProfileThankRow
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.profile.ui.models.ProfileScreenState
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.ProfileUiState
import com.example.apexhr.features.profile.ui.models.ThankUiModel
import com.example.apexhr.features.profile.ui.models.joinedText
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
                department = uiState.department.joinedText(),
                avatarUrl = uiState.avatarUrl,
            )
        }
        item {
            ProfileBalanceBlock(balance = uiState.balance)
        }
        item {
            ProfileTabs(
                selectedTab = uiState.selectedTab,
                onTabClick = callbacks::onTabClick,
            )
        }
        if (uiState.selectedTab == ProfileTab.Thanks) {
            item {
                ProfileSendThanksButton(onClick = callbacks::onSendThanksClick)
            }
            thanksSection(
                key = "received",
                titleRes = R.string.thanks_received_section,
                emptyTextRes = R.string.thanks_received_empty,
                thanks = uiState.received,
                received = true,
            )
            thanksSection(
                key = "sent",
                titleRes = R.string.thanks_sent_section,
                emptyTextRes = R.string.thanks_sent_empty,
                thanks = uiState.sent,
                received = false,
            )
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

/** Блок «Полученные» или «Отправленные»: заголовок и карточки либо текст «пока нет». */
private fun LazyListScope.thanksSection(
    key: String,
    @StringRes titleRes: Int,
    @StringRes emptyTextRes: Int,
    thanks: List<ThankUiModel>,
    received: Boolean,
) {
    item(key = "${key}_title") {
        ProfileSectionTitle(text = stringResource(titleRes))
    }
    if (thanks.isEmpty()) {
        item(key = "${key}_empty") {
            EmptyState(text = stringResource(emptyTextRes))
        }
    }
    items(thanks, key = { "${key}_${it.id}" }) { thank ->
        ProfileThankRow(thank = thank, received = received)
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
