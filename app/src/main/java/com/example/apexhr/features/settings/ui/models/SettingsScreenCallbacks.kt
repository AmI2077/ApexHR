package com.example.apexhr.features.settings.ui.models

import com.example.apexhr.features.settings.domain.ThemeChoice

interface SettingsScreenCallbacks {
    fun onBackClick()
    fun onThemeChoiceClick(choice: ThemeChoice)

    /** Выход из аккаунта. По умолчанию ничего не делает. */
    fun onLogoutClick() = Unit

    /** Переход к смене пароля. По умолчанию ничего не делает. */
    fun onChangePasswordClick() = Unit

    /** Переход в поддержку. По умолчанию ничего не делает. */
    fun onSupportClick() = Unit

    companion object {
        val Empty = object : SettingsScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onThemeChoiceClick(choice: ThemeChoice) = Unit
        }
    }
}
