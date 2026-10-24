package com.example.apexhr.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.profile.domain.ProfileRepository
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.profile.ui.models.ProfileScreenState
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Загружает профиль из API. Выход из аккаунта обрабатывает навигация
 * (onLogoutClick переопределяется в AppNavigation), здесь его нет.
 */
class ProfileViewModel(
    private val repository: ProfileRepository,
) : ViewModel(), ProfileScreenCallbacks {

    private val _state = MutableStateFlow<ProfileScreenState>(ProfileScreenState.Loading)
    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()

    private var selectedTab = ProfileTab.Thanks
    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = ProfileScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = repository.loadProfile()) {
                is DataResult.Success ->
                    ProfileScreenState.Content(result.data.toUiState(selectedTab))
                is DataResult.Failure -> ProfileScreenState.Error(result.error)
            }
        }
    }

    override fun onRetryClick() = load()

    override fun onTabClick(tab: ProfileTab) {
        selectedTab = tab
        val current = _state.value
        if (current is ProfileScreenState.Content) {
            _state.value = current.copy(uiState = current.uiState.copy(selectedTab = tab))
        }
    }

    // Экранов истории, назад и деталей операций/достижений пока нет.
    override fun onBackClick() = Unit
    override fun onHistoryClick() = Unit
    override fun onTransactionClick(transactionId: String) = Unit
    override fun onAchievementClick(achievementId: String) = Unit
}
