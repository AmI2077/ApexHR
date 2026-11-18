package com.example.apexhr.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.core.domain.map
import com.example.apexhr.features.profile.domain.AvatarChangeResult
import com.example.apexhr.features.profile.domain.AvatarRepository
import com.example.apexhr.features.profile.domain.Profile
import com.example.apexhr.features.profile.domain.ProfileRepository
import com.example.apexhr.features.profile.domain.TransactionFilter
import com.example.apexhr.features.profile.domain.TransactionHistoryRepository
import com.example.apexhr.features.profile.ui.history.models.LoadedTransactions
import com.example.apexhr.features.profile.ui.history.models.TransactionHistoryState
import com.example.apexhr.features.profile.ui.history.models.toUiState
import com.example.apexhr.features.profile.ui.models.ProfileScreenCallbacks
import com.example.apexhr.features.profile.ui.models.ProfileScreenState
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.ProfileUiState
import com.example.apexhr.core.ui.model.pageSlice
import com.example.apexhr.features.profile.ui.models.messageRes
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
 * Профиль: шапка с балансом и вкладки «Благодарности», «Достижения», «Операции».
 * Профиль и история благодарностей загружаются сразу; операции — постранично
 * при первом открытии вкладки. После отправки благодарности (экран остаётся
 * в стеке под формой) данные тихо перечитываются, и к возврату список
 * отправленных, баланс и операции уже обновлены.
 * Переход к настройкам обрабатывает навигация (onSettingsClick переопределяется
 * в AppNavigation), здесь его нет.
 * Новое фото профиля загружается в хранилище, после чего профиль перечитывается.
 */
class ProfileViewModel(
    private val repository: ProfileRepository,
    private val thanksRepository: ThanksRepository,
    private val historyRepository: TransactionHistoryRepository,
    private val avatarRepository: AvatarRepository,
) : ViewModel(), ProfileScreenCallbacks {

    private val _state = MutableStateFlow<ProfileScreenState>(ProfileScreenState.Loading)
    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()

    private var selectedTab = ProfileTab.Thanks
    private var receivedPage = 1
    private var sentPage = 1
    private var data: Pair<Profile, ThanksHistory>? = null
    private var loadJob: Job? = null

    // Вкладка «Операции».
    private var operations: TransactionHistoryState = TransactionHistoryState.Loading
    private var operationsRequested = false
    private var loaded = LoadedTransactions.Empty
    private var filter = TransactionFilter.All
    private var isLoadingMore = false
    private var loadMoreError: DataError? = null
    private var operationsJob: Job? = null

    // Смена фото.
    private var isAvatarUploading = false
    private var avatarErrorRes: Int? = null

    init {
        load()
        viewModelScope.launch {
            thanksRepository.thanksSent.collect {
                refresh()
                if (operationsRequested) loadOperations()
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        data = null
        _state.value = ProfileScreenState.Loading
        loadJob = viewModelScope.launch {
            when (val result = loadAll()) {
                is DataResult.Success -> {
                    data = result.data
                    publish()
                }
                is DataResult.Failure -> _state.value = ProfileScreenState.Error(result.error)
            }
        }
    }

    /** Перечитывает данные без экрана загрузки; ошибку молча игнорирует. */
    private fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val result = loadAll()
            if (result is DataResult.Success) {
                data = result.data
                publish()
            }
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

    /** Первая страница операций; фильтр сохраняется. */
    private fun loadOperations() {
        operationsRequested = true
        operationsJob?.cancel()
        loaded = LoadedTransactions.Empty
        isLoadingMore = false
        loadMoreError = null
        operations = TransactionHistoryState.Loading
        publish()
        operationsJob = viewModelScope.launch {
            when (val result = historyRepository.loadPage(skip = 0)) {
                is DataResult.Success -> {
                    loaded += result.data
                    publishOperations()
                }
                is DataResult.Failure -> {
                    operations = TransactionHistoryState.Error(result.error)
                    publish()
                }
            }
        }
    }

    override fun onRetryClick() = load()

    override fun onTabClick(tab: ProfileTab) {
        selectedTab = tab
        if (tab == ProfileTab.Operations && !operationsRequested) {
            loadOperations()
        } else {
            publish()
        }
    }

    override fun onOperationsRetryClick() = loadOperations()

    override fun onOperationsLoadMoreClick() {
        if (operations !is TransactionHistoryState.Content) return
        if (isLoadingMore || !loaded.hasMore) return
        isLoadingMore = true
        loadMoreError = null
        publishOperations()
        operationsJob = viewModelScope.launch {
            val result = historyRepository.loadPage(skip = loaded.nextSkip)
            isLoadingMore = false
            when (result) {
                is DataResult.Success -> loaded += result.data
                is DataResult.Failure -> loadMoreError = result.error
            }
            publishOperations()
        }
    }

    override fun onOperationsFilterClick(filter: TransactionFilter) {
        this.filter = filter
        if (operations is TransactionHistoryState.Content) publishOperations()
    }

    override fun onAvatarPicked(imageUri: String) {
        if (isAvatarUploading || data == null) return
        isAvatarUploading = true
        avatarErrorRes = null
        publish()
        viewModelScope.launch {
            when (val result = avatarRepository.changeAvatar(imageUri)) {
                is AvatarChangeResult.Success -> {
                    // Перечитываем профиль с новой ссылкой; индикатор держим до конца.
                    loadJob?.cancel()
                    val reloaded = loadAll()
                    if (reloaded is DataResult.Success) data = reloaded.data
                }
                is AvatarChangeResult.Failure -> avatarErrorRes = result.error.messageRes()
            }
            isAvatarUploading = false
            publish()
        }
    }

    // Экрана деталей достижения пока нет.
    override fun onAchievementClick(achievementId: String) = Unit

    override fun onThanksPageChange(received: Boolean, page: Int) {
        val (_, thanks) = data ?: return
        val list = if (received) thanks.received else thanks.sent
        val safePage = list.pageSlice(page, ProfileUiState.THANKS_PAGE_SIZE).page
        if (received) receivedPage = safePage else sentPage = safePage
        publish()
    }

    private fun publishOperations() {
        operations = TransactionHistoryState.Content(
            loaded.toUiState(
                filter = filter,
                isLoadingMore = isLoadingMore,
                loadMoreError = loadMoreError,
            )
        )
        publish()
    }

    /** Показывает текущее состояние; пока профиль не загружен, экран остаётся в загрузке или ошибке. */
    private fun publish() {
        val (profile, thanks) = data ?: return
        _state.value = ProfileScreenState.Content(
            profile.toUiState(selectedTab, thanks).copy(
                operations = operations,
                isAvatarUploading = isAvatarUploading,
                avatarErrorRes = avatarErrorRes,
                receivedPage = receivedPage,
                sentPage = sentPage,
            )
        )
    }
}
