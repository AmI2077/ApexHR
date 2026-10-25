package com.example.apexhr.features.activities.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.apexhr.features.activities.ui.composables.ActivitiesHeader
import com.example.apexhr.features.activities.ui.composables.ActivitiesPopularTitle
import com.example.apexhr.features.activities.ui.composables.ActivitiesSearchField
import com.example.apexhr.features.activities.ui.composables.ActivityCard
import com.example.apexhr.features.activities.ui.composables.ActivityCategoryTabs
import com.example.apexhr.features.activities.ui.models.ActivitiesContentState
import com.example.apexhr.features.activities.ui.models.ActivitiesScreenCallbacks
import com.example.apexhr.features.activities.ui.models.ActivitiesUiState
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ActivitiesScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ActivitiesScreen(
            uiState = ActivitiesUiState.Preview,
            callbacks = ActivitiesScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ActivitiesScreenEmptyPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ActivitiesScreen(
            uiState = ActivitiesUiState.Preview.copy(
                content = ActivitiesContentState.Content(emptyList(), isFiltered = false),
            ),
            callbacks = ActivitiesScreenCallbacks.Empty,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ActivitiesScreenErrorPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ActivitiesScreen(
            uiState = ActivitiesUiState.Preview.copy(
                content = ActivitiesContentState.Error(DataError.Network),
            ),
            callbacks = ActivitiesScreenCallbacks.Empty,
        )
    }
}

@Composable
fun ActivitiesScreen(
    uiState: ActivitiesUiState,
    callbacks: ActivitiesScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    // Экран снова в композиции при входе на вкладку и при возврате с деталей.
    LaunchedEffect(callbacks) {
        callbacks.onScreenShown()
    }
    ActivitiesScreenContent(
        uiState = uiState,
        callbacks = callbacks,
        modifier = modifier,
    )
}

@Composable
fun ActivitiesScreenContent(
    uiState: ActivitiesUiState,
    callbacks: ActivitiesScreenCallbacks,
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
            ActivitiesHeader()
        }
        item {
            ActivitiesSearchField(
                query = uiState.searchQuery,
                onQueryChange = callbacks::onSearchQueryChange,
            )
        }
        item {
            ActivityCategoryTabs(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategoryClick = callbacks::onCategoryClick,
            )
        }
        when (val content = uiState.content) {
            ActivitiesContentState.Loading -> item {
                LoadingState()
            }

            is ActivitiesContentState.Error -> item {
                ErrorState(
                    error = content.error,
                    onRetry = callbacks::onRetryClick,
                )
            }

            is ActivitiesContentState.Content -> if (content.activities.isEmpty()) {
                item {
                    EmptyState(
                        text = stringResource(
                            if (content.isFiltered) R.string.activities_not_found else R.string.activities_empty,
                        ),
                    )
                }
            } else {
                item {
                    ActivitiesPopularTitle()
                }
                items(content.activities, key = { "${it.kind}_${it.id}" }) { activity ->
                    ActivityCard(
                        activity = activity,
                        onClick = { callbacks.onActivityClick(activity) },
                        onParticipateClick = { callbacks.onParticipateClick(activity) },
                    )
                }
            }
        }
    }
}
