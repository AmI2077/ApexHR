package com.example.apexhr.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.domain.map
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.ProfileRepository
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.profile.ui.models.ProfileScreenState
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.toUiState
import com.example.apexhr.features.thanks.domain.ThanksHistory
import com.example.apexhr.features.thanks.domain.ThanksRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Загружает профиль и историю благодарностей из API. После отправки
 * благодарности (экран остаётся в стеке под формой) данные тихо
 * перечитываются, и к возврату список отправленных уже обновлён.
 * Выход из аккаунта обрабатывает навигация (onLogoutClick переопределяется
 * в AppNavigation), здесь его нет.
 */
class ProfileViewModel(
    private val repository: ProfileRepository,
    private val thanksRepository: ThanksRepository,
) : ViewModel(), ProfileScreenCallbacks {

    private val _state = MutableStateFlow<ProfileScreenState>(ProfileScreenState.Loading)
    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()

    private var selectedTab = ProfileTab.Thanks
    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            thanksRepository.thanksSent.collect { refresh() }
        }
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = ProfileScreenState.Loading
        loadJob = viewModelScope.launch {
            _state.value = when (val result = loadAll()) {
                is DataResult.Success -> content(result.data)
                is DataResult.Failure -> ProfileScreenState.Error(result.error)
            }
        }
    }

    /** Перечитывает данные без экрана загрузки; ошибку молча игнорирует. */
    private fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val result = loadAll()
            if (result is DataResult.Success) _state.value = content(result.data)
        }
    }

    /** Профиль и благодарности независимы — грузим параллельно; ошибка любой части — ошибка экрана. */
    private suspend fun loadAll(): DataResult<Pair<Profile, ThanksHistory>> = coroutineScope {
        val thanks = async { thanksRepository.loadHistory() }
        val profile = repository.loadProfile()
        val history = thanks.await()
        when (profile) {
            is DataResult.Failure -> profile
            is DataResult.Success -> history.map { profile.data to it }
        }
    }

    private fun content(data: Pair<Profile, ThanksHistory>) =
        ProfileScreenState.Content(data.first.toUiState(selectedTab, data.second))

    override fun onRetryClick() = load()

    override fun onTabClick(tab: ProfileTab) {
        selectedTab = tab
        val current = _state.value
        if (current is ProfileScreenState.Content) {
            _state.value = current.copy(uiState = current.uiState.copy(selectedTab = tab))
        }
    }

    // История открывается навигацией (onHistoryClick переопределяется в AppNavigation).
    override fun onHistoryClick() = Unit

    // Экранов назад и деталей достижений пока нет.
    override fun onBackClick() = Unit
    override fun onAchievementClick(achievementId: String) = Unit
}
