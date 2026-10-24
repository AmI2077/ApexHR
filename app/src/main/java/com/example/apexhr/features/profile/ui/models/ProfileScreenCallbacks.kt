package com.example.apexhr.features.profile.ui.models

interface ProfileScreenCallbacks {
    fun onBackClick()
    fun onHistoryClick()
    fun onTabClick(tab: ProfileTab)
    fun onTransactionClick(transactionId: String)
    fun onAchievementClick(achievementId: String)

    /** Выход из аккаунта. По умолчанию ничего не делает. */
    fun onLogoutClick() = Unit

    /** Повторная загрузка после ошибки. По умолчанию ничего не делает. */
    fun onRetryClick() = Unit

    companion object {
        val Empty = object : ProfileScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onHistoryClick() = Unit
            override fun onTabClick(tab: ProfileTab) = Unit
            override fun onTransactionClick(transactionId: String) = Unit
            override fun onAchievementClick(achievementId: String) = Unit
        }
    }
}
