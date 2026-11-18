package com.example.apexhr.features.home.ui.models

interface HomeScreenCallbacks {
    fun onPointsClick()
    fun onChallengeClick(challengeId: String)
    fun onLeaderboardEntryClick(entryId: String)
    fun onAchievementClick(achievementId: String)
    fun onRetryClick() = Unit

    /** Колокольчик в шапке: экран уведомлений открывает навигация. */
    fun onNotificationsClick() = Unit

    /** Ссылка «Моя активность» в карточке уровня: экран открывает навигация. */
    fun onMyActivityClick() = Unit

    /** Блок новостей: «Все» открывает ленту, карточка — новость целиком. */
    fun onAllNewsClick() = Unit
    fun onNewsClick(newsId: Long) = Unit

    /** «Поблагодарить» в ленте благодарностей: экран благодарности открывает навигация. */
    fun onSendThanksClick() = Unit

    companion object {
        val Empty = object : HomeScreenCallbacks {
            override fun onPointsClick() = Unit
            override fun onChallengeClick(challengeId: String) = Unit
            override fun onLeaderboardEntryClick(entryId: String) = Unit
            override fun onAchievementClick(achievementId: String) = Unit
        }
    }
}
