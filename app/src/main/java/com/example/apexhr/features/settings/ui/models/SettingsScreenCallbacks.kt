package com.example.apexhr.features.settings.ui.models

interface SettingsScreenCallbacks {
    fun onBackClick()
    fun onDarkThemeChange(enabled: Boolean)

    /** Выход из аккаунта. По умолчанию ничего не делает. */
    fun onLogoutClick() = Unit

    companion object {
        val Empty = object : SettingsScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onDarkThemeChange(enabled: Boolean) = Unit
        }
    }
}
