package com.example.apexhr.features.profile.ui.models

import com.example.apexhr.features.profile.domain.TransactionFilter

interface ProfileScreenCallbacks {
    fun onBackClick()
    fun onTabClick(tab: ProfileTab)
    fun onAchievementClick(achievementId: String)

    /** Переход к настройкам. По умолчанию ничего не делает. */
    fun onSettingsClick() = Unit

    /** Переход к отправке благодарности. По умолчанию ничего не делает. */
    fun onSendThanksClick() = Unit

    /** Повторная загрузка после ошибки. По умолчанию ничего не делает. */
    fun onRetryClick() = Unit

    /** Фильтр по виду на вкладке «Операции». По умолчанию ничего не делает. */
    fun onOperationsFilterClick(filter: TransactionFilter) = Unit

    /** «Показать ещё» (и повтор после ошибки подгрузки) на вкладке «Операции». По умолчанию ничего не делает. */
    fun onOperationsLoadMoreClick() = Unit

    /** Повтор загрузки вкладки «Операции» после ошибки. По умолчанию ничего не делает. */
    fun onOperationsRetryClick() = Unit

    /** Выбрано новое фото профиля (content:// адрес). По умолчанию ничего не делает. */
    fun onAvatarPicked(imageUri: String) = Unit

    companion object {
        val Empty = object : ProfileScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onTabClick(tab: ProfileTab) = Unit
            override fun onAchievementClick(achievementId: String) = Unit
        }
    }
}
