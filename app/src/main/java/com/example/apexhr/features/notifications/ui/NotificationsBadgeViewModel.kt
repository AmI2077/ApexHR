package com.example.apexhr.features.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.notifications.domain.NotificationsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Бейдж непрочитанных уведомлений на главной. [refresh] вызывается каждый раз,
 * когда главная снова на экране (например, после возврата из уведомлений).
 * Ошибка загрузки бейдж не сбрасывает и на экран не выводится.
 */
class NotificationsBadgeViewModel(
    private val repository: NotificationsRepository,
) : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var refreshJob: Job? = null

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val result = repository.getUnreadCount()
            if (result is DataResult.Success) _unreadCount.value = result.data
        }
    }
}
