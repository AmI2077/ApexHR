package com.example.apexhr.features.home.ui.models

interface HomeScreenCallbacks {
    fun onPointsClick()
    fun onChallengeClick(challengeId: String)
    fun onLeaderboardEntryClick(entryId: String)
    fun onAchievementClick(achievementId: String)
    fun onRetryClick() = Unit

    companion object {
        val Empty = object : HomeScreenCallbacks {
            override fun onPointsClick() = Unit
            override fun onChallengeClick(challengeId: String) = Unit
            override fun onLeaderboardEntryClick(entryId: String) = Unit
            override fun onAchievementClick(achievementId: String) = Unit
        }
    }
}
