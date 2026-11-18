package com.example.apexhr.features.colleague.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.colleague.ui.models.ColleagueProfileScreenCallbacks
import com.example.apexhr.features.colleague.ui.models.ColleagueProfileScreenState
import com.example.apexhr.features.colleague.ui.models.ColleagueProfileUiState
import com.example.apexhr.features.profile.ui.composables.ProfileAchievementRow
import com.example.apexhr.features.profile.ui.composables.ProfileSectionTitle
import com.example.apexhr.features.profile.ui.composables.ProfileSendThanksButton
import com.example.apexhr.features.profile.ui.composables.ProfileThankRow
import com.example.apexhr.features.profile.ui.composables.ProfileUserInfo
import com.example.apexhr.features.profile.ui.models.ThankUiModel
import com.example.apexhr.features.profile.ui.models.joinedText
import com.example.apexhr.features.shop.ui.cart.composables.ShopSubscreenHeader
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ColleagueProfileScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ColleagueProfileScreen(
            screenState = ColleagueProfileScreenState.Content(ColleagueProfileUiState.Preview),
            callbacks = ColleagueProfileScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ColleagueProfileScreenBlockedPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ColleagueProfileScreen(
            screenState = ColleagueProfileScreenState.Content(
                ColleagueProfileUiState.Preview.copy(
                    isActive = false,
                    thanksScore = null,
                    receivedThanks = emptyList(),
                    achievements = emptyList(),
                )
            ),
            callbacks = ColleagueProfileScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ColleagueProfileScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ColleagueProfileScreen(
            screenState = ColleagueProfileScreenState.Error(DataError.Network),
            callbacks = ColleagueProfileScreenCallbacks.Empty,
        )
    }
}

/**
 * Профиль коллеги: фото, должность и отдел, роль, благодарности в рейтинге,
 * благодарности из ленты пользователя и достижения.
 */
@Composable
fun ColleagueProfileScreen(
    screenState: ColleagueProfileScreenState,
    callbacks: ColleagueProfileScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        ShopSubscreenHeader(
            title = stringResource(R.string.colleague_title),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        when (screenState) {
            ColleagueProfileScreenState.Loading -> LoadingState()

            is ColleagueProfileScreenState.Error -> ErrorState(
                error = screenState.error,
                onRetry = callbacks::onRetryClick,
            )

            is ColleagueProfileScreenState.Content -> ColleagueProfileContent(
                uiState = screenState.uiState,
                callbacks = callbacks,
            )
        }
    }
}

@Composable
private fun ColleagueProfileContent(
    uiState: ColleagueProfileUiState,
    callbacks: ColleagueProfileScreenCallbacks,
) {
    val youLabel = stringResource(R.string.colleague_from_me)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ApexDimens.spacing20.dp,
            end = ApexDimens.spacing20.dp,
            bottom = ApexDimens.spacing24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
    ) {
        item(key = "user") {
            Column(verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp)) {
                ProfileUserInfo(
                    name = uiState.name,
                    department = uiState.info.joinedText(),
                    avatarUrl = uiState.avatarUrl,
                )
                Text(
                    text = stringResource(uiState.roleRes),
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                )
                uiState.thanksScore?.let { score ->
                    Text(
                        text = stringResource(R.string.colleague_score, score),
                        color = primaryTextColor,
                        fontSize = ApexDimens.text14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (!uiState.isActive) {
                    Text(
                        text = stringResource(R.string.colleague_blocked),
                        color = negativeRedColor,
                        fontSize = ApexDimens.text14.sp,
                    )
                }
            }
        }
        if (uiState.isMe) {
            item(key = "own_profile") {
                OutlinedButton(
                    onClick = callbacks::onOpenOwnProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ApexDimens.buttonHeight.dp),
                ) {
                    Text(
                        text = stringResource(R.string.colleague_open_own_profile),
                        color = primaryBlueColor,
                        fontSize = ApexDimens.text14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        } else if (uiState.canThank) {
            item(key = "thank") {
                ProfileSendThanksButton(onClick = { callbacks.onThankClick(uiState.id) })
            }
        }
        item(key = "thanks_title") {
            Column(
                modifier = Modifier.padding(top = ApexDimens.spacing8.dp),
                verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
            ) {
                ProfileSectionTitle(text = stringResource(R.string.colleague_thanks_title))
                if (!uiState.isMe) {
                    Text(
                        text = stringResource(R.string.colleague_thanks_hint),
                        color = secondaryTextColor,
                        fontSize = ApexDimens.text12.sp,
                    )
                }
            }
        }
        if (uiState.receivedThanks.isEmpty()) {
            item(key = "thanks_empty") {
                Text(
                    text = stringResource(R.string.colleague_thanks_empty),
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                )
            }
        }
        items(uiState.receivedThanks, key = { "thank_${it.id}" }) { thank ->
            ProfileThankRow(
                thank = ThankUiModel(
                    id = thank.id,
                    name = if (thank.fromMe) youLabel else thank.fromName,
                    amount = thank.amount,
                    message = thank.message,
                    date = thank.date,
                ),
                received = true,
            )
        }
        item(key = "achievements_title") {
            ProfileSectionTitle(
                text = stringResource(R.string.colleague_achievements_title),
                modifier = Modifier.padding(top = ApexDimens.spacing8.dp),
            )
        }
        if (uiState.achievements.isEmpty()) {
            item(key = "achievements_empty") {
                Text(
                    text = stringResource(R.string.colleague_achievements_empty),
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                )
            }
        }
        items(uiState.achievements, key = { "achievement_${it.id}" }) { achievement ->
            ProfileAchievementRow(achievement = achievement, onClick = {})
        }
    }
}