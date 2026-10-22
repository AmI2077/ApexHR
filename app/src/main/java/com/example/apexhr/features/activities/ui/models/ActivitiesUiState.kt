package com.example.apexhr.features.activities.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R

data class ActivitiesUiState(
    val searchQuery: String,
    val selectedCategoryId: String,
    val categories: List<ActivityCategoryUiModel>,
    val activities: List<ActivityUiModel>,
) {
    companion object {
        const val CATEGORY_SURVEYS = "surveys"
        const val CATEGORY_MINI_GAMES = "mini_games"
        const val CATEGORY_EVENTS = "events"

        val Preview = ActivitiesUiState(
            searchQuery = "",
            selectedCategoryId = CATEGORY_SURVEYS,
            categories = listOf(
                ActivityCategoryUiModel(CATEGORY_SURVEYS, R.string.activities_category_surveys),
                ActivityCategoryUiModel(CATEGORY_MINI_GAMES, R.string.activities_category_mini_games),
                ActivityCategoryUiModel(CATEGORY_EVENTS, R.string.activities_category_events),
            ),
            activities = listOf(
                ActivityUiModel(
                    id = "hype",
                    title = "Сколько в тебе хайпа?",
                    questionsCount = 12,
                    rewardPoints = 10,
                )
            ),
        )
    }
}

data class ActivityCategoryUiModel(
    val id: String,
    @StringRes val titleRes: Int,
)

data class ActivityUiModel(
    val id: String,
    val title: String,
    val questionsCount: Int,
    val rewardPoints: Int,
)
