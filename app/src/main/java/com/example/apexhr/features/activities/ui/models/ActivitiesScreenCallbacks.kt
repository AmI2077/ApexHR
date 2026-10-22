package com.example.apexhr.features.activities.ui.models

interface ActivitiesScreenCallbacks {
    fun onSearchQueryChange(query: String)
    fun onCategoryClick(categoryId: String)
    fun onActivityClick(activityId: String)
    fun onParticipateClick(activityId: String)

    companion object {
        val Empty = object : ActivitiesScreenCallbacks {
            override fun onSearchQueryChange(query: String) = Unit
            override fun onCategoryClick(categoryId: String) = Unit
            override fun onActivityClick(activityId: String) = Unit
            override fun onParticipateClick(activityId: String) = Unit
        }
    }
}
