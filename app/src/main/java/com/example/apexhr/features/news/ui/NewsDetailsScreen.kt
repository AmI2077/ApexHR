package com.example.apexhr.features.news.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.activities.ui.common.ActivitySubscreenHeader
import com.example.apexhr.features.news.ui.composables.newsMeta
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

/** Новость целиком: заголовок, автор и дата, краткое описание и текст. */
@Composable
fun NewsDetailsScreen(
    screenState: NewsDetailsScreenState,
    callbacks: NewsDetailsScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        ActivitySubscreenHeader(
            title = stringResource(R.string.news_details_title),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        when (screenState) {
            NewsDetailsScreenState.Loading -> LoadingState()
            is NewsDetailsScreenState.Error -> ErrorState(error = screenState.error, onRetry = callbacks::onRetryClick)
            is NewsDetailsScreenState.Content -> {
                val news = screenState.news
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = ApexDimens.spacing20.dp,
                            end = ApexDimens.spacing20.dp,
                            bottom = ApexDimens.spacing16.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
                ) {
                    Text(
                        text = news.title.ifBlank { stringResource(R.string.news_untitled) },
                        color = primaryTextColor,
                        fontSize = ApexDimens.text22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    newsMeta(news)?.let {
                        Text(text = it, color = secondaryTextColor, fontSize = ApexDimens.text13.sp)
                    }
                    if (news.summary != null) {
                        Text(
                            text = news.summary,
                            color = primaryTextColor,
                            fontSize = ApexDimens.text16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (news.content != null) {
                        Text(text = news.content, color = primaryTextColor, fontSize = ApexDimens.text14.sp)
                    }
                }
            }
        }
    }
}
