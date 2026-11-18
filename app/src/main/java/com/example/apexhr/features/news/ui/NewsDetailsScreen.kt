package com.example.apexhr.features.news.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.news.ui.composables.NEWS_COVER_RATIO
import com.example.apexhr.features.news.ui.composables.NewsCover
import com.example.apexhr.features.news.ui.models.NewsDetailsScreenCallbacks
import com.example.apexhr.features.news.ui.models.NewsDetailsScreenState
import com.example.apexhr.features.news.ui.models.NewsUiModel
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NewsDetailsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NewsDetailsScreen(
            screenState = NewsDetailsScreenState.Content(
                NewsUiModel.Preview.first().copy(content = "Полный текст новости."),
            ),
            callbacks = NewsDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
private fun NewsDetailsScreenDarkPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = true) {
        NewsDetailsScreen(
            screenState = NewsDetailsScreenState.Content(
                NewsUiModel.Preview.first().copy(content = "Полный текст новости."),
            ),
            callbacks = NewsDetailsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NewsDetailsScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NewsDetailsScreen(
            screenState = NewsDetailsScreenState.Error(DataError.Timeout),
            callbacks = NewsDetailsScreenCallbacks.Empty,
        )
    }
}

/**
 * Новость целиком — как окно новости в вебе, на мобильном во весь экран: сверху
 * обложка во всю ширину, справа над текстом круглая кнопка «×», дальше крупный
 * заголовок, дата («18 ноя 2026, 01:59»), краткое описание и полный текст.
 * Прокручивается всё вместе. «×» закрывает экран (onBackClick задаёт навигация).
 */
@Composable
fun NewsDetailsScreen(
    screenState: NewsDetailsScreenState,
    callbacks: NewsDetailsScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    when (screenState) {
        is NewsDetailsScreenState.Content -> NewsDetailsContent(
            news = screenState.news,
            onCloseClick = callbacks::onBackClick,
            modifier = modifier,
        )

        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor),
        ) {
            NewsCloseButton(
                onClick = callbacks::onBackClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(ApexDimens.spacing16.dp),
            )
            if (screenState is NewsDetailsScreenState.Error) {
                ErrorState(error = screenState.error, onRetry = callbacks::onRetryClick)
            } else {
                LoadingState()
            }
        }
    }
}

@Composable
private fun NewsDetailsContent(
    news: NewsUiModel,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState()),
    ) {
        NewsCover(
            imageUrl = news.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(NEWS_COVER_RATIO),
        )
        Column(
            modifier = Modifier.padding(
                start = ApexDimens.spacing20.dp,
                end = ApexDimens.spacing16.dp,
                top = ApexDimens.spacing16.dp,
                bottom = ApexDimens.spacing24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = news.title.ifBlank { stringResource(R.string.news_untitled) },
                    color = primaryTextColor,
                    fontSize = ApexDimens.text24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                NewsCloseButton(onClick = onCloseClick)
            }
            if (news.date != null) {
                Text(text = news.date, color = secondaryTextColor, fontSize = ApexDimens.text13.sp)
            }
            if (news.summary != null) {
                Text(
                    text = news.summary,
                    // Приглушённый сине-серый, как краткое описание в окне новости веба.
                    color = lerp(secondaryTextColor, primaryBlueColor, 0.35f),
                    fontSize = ApexDimens.text16.sp,
                    modifier = Modifier.padding(top = ApexDimens.spacing4.dp),
                )
            }
            if (news.content != null) {
                Text(
                    text = news.content,
                    color = primaryTextColor,
                    fontSize = ApexDimens.text16.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(top = ApexDimens.spacing8.dp),
                )
            }
        }
    }
}

/** Круглая тёмная кнопка «×», как в окне новости веба. */
@Composable
private fun NewsCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(pillBackgroundColor)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(R.string.news_close),
            tint = primaryTextColor,
            modifier = Modifier.size(ApexDimens.icon20.dp),
        )
    }
}
