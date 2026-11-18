package com.example.apexhr.features.news.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.apexhr.core.ui.composables.LoadMoreFooter
import com.example.apexhr.core.ui.composables.LoadingState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.features.activities.ui.common.ActivitySubscreenHeader
import com.example.apexhr.features.news.ui.composables.NewsCard
import com.example.apexhr.features.news.ui.models.NewsScreenCallbacks
import com.example.apexhr.features.news.ui.models.NewsScreenState
import com.example.apexhr.features.news.ui.models.NewsUiModel
import com.example.apexhr.features.news.ui.models.NewsUiState
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NewsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NewsScreen(
            screenState = NewsScreenState.Content(NewsUiState(NewsUiModel.Preview, hasNextPage = false)),
            callbacks = NewsScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NewsScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NewsScreen(screenState = NewsScreenState.Error(DataError.Network), callbacks = NewsScreenCallbacks.Empty)
    }
}

/** «Новости»: открываются по «Все» в блоке новостей на главной. */
@Composable
fun NewsScreen(
    screenState: NewsScreenState,
    callbacks: NewsScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        ActivitySubscreenHeader(
            title = stringResource(R.string.news_title),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        when (screenState) {
            NewsScreenState.Loading -> LoadingState()
            is NewsScreenState.Error -> ErrorState(error = screenState.error, onRetry = callbacks::onRetryClick)
            is NewsScreenState.Content -> if (screenState.uiState.items.isEmpty()) {
                EmptyState(text = stringResource(R.string.news_empty))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = ApexDimens.spacing20.dp,
                        end = ApexDimens.spacing20.dp,
                        bottom = ApexDimens.spacing16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
                ) {
                    items(screenState.uiState.items, key = { it.id }) { news ->
                        NewsCard(news = news, onClick = { callbacks.onNewsClick(news.id) }, showMeta = true)
                    }
                    // Бесконечная прокрутка: следующие 20 новостей грузятся у конца списка.
                    item(key = "load_more") {
                        LoadMoreFooter(state = screenState.uiState.loadMore, onLoadMore = callbacks::onLoadMore)
                    }
                }
            }
        }
    }
}
