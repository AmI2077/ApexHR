package com.example.apexhr.features.notifications.ui.models

interface NotificationsScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()

    /** Нажатие на уведомление отмечает его прочитанным. */
    fun onNotificationClick(id: Long)
    fun onReadAllClick()
    fun onLoadMoreClick()

    companion object {
        val Empty = object : NotificationsScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onNotificationClick(id: Long) = Unit
            override fun onReadAllClick() = Unit
            override fun onLoadMoreClick() = Unit
        }
    }
}
