package com.example.apexhr.features.thanks.ui.models

interface ThanksScreenCallbacks {
    fun onBackClick()
    fun onSearchQueryChange(query: String)
    fun onColleagueClick(colleagueId: String)
    fun onQuickThanksClick(colleagueId: String)
    fun onMessageChange(message: String)
    fun onSendClick()
    fun onRetryClick()

    companion object {
        val Empty = object : ThanksScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onSearchQueryChange(query: String) = Unit
            override fun onColleagueClick(colleagueId: String) = Unit
            override fun onQuickThanksClick(colleagueId: String) = Unit
            override fun onMessageChange(message: String) = Unit
            override fun onSendClick() = Unit
            override fun onRetryClick() = Unit
        }
    }
}
