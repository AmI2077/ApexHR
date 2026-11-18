package com.example.apexhr.features.colleague.ui.models

interface ColleagueProfileScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()

    /** «Поблагодарить коллегу»: экран благодарностей открывает навигация. */
    fun onThankClick(colleagueId: String) = Unit

    /** Свой профиль открыт как профиль коллеги — переход на вкладку «Профиль». */
    fun onOpenOwnProfileClick() = Unit

    companion object {
        val Empty = object : ColleagueProfileScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
        }
    }
}