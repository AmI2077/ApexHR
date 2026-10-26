package com.example.apexhr.features.thanks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.thanks.domain.ThanksOverview
import com.example.apexhr.features.thanks.domain.ThanksRepository
import com.example.apexhr.features.thanks.domain.ThanksSendResult
import com.example.apexhr.features.thanks.ui.models.THANKS_AMOUNTS
import com.example.apexhr.features.thanks.ui.models.THANKS_SENT_NOTICE
import com.example.apexhr.features.thanks.ui.models.ThanksForm
import com.example.apexhr.features.thanks.ui.models.ThanksScreenCallbacks
import com.example.apexhr.features.thanks.ui.models.ThanksScreenState
import com.example.apexhr.features.thanks.ui.models.limitThanksMessage
import com.example.apexhr.features.thanks.ui.models.toNotice
import com.example.apexhr.features.thanks.ui.models.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Отправка благодарности коллеге и списки полученных и отправленных.
 * После успешной отправки форма очищается, а данные тихо перечитываются.
 * Назад обрабатывает навигация (onBackClick переопределяется в AppNavigation).
 */
class ThanksViewModel(
    private val repository: ThanksRepository,
) : ViewModel(), ThanksScreenCallbacks {

    private val _state = MutableStateFlow<ThanksScreenState>(ThanksScreenState.Loading)
    val state: StateFlow<ThanksScreenState> = _state.asStateFlow()

    private var overview: ThanksOverview? = null
    private var form = ThanksForm()
    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = ThanksScreenState.Loading
        loadJob = viewModelScope.launch {
            when (val result = repository.loadThanks()) {
                is DataResult.Success -> {
                    overview = result.data
                    publish()
                }
                is DataResult.Failure -> _state.value = ThanksScreenState.Error(result.error)
            }
        }
    }

    /** Перечитывает данные без экрана загрузки; ошибку молча игнорирует. */
    private fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val result = repository.loadThanks()
            if (result is DataResult.Success) {
                overview = result.data
                publish()
            }
        }
    }

    override fun onRetryClick() = load()

    override fun onSearchQueryChange(query: String) = updateForm { copy(searchQuery = query) }

    override fun onColleagueClick(colleagueId: String) =
        updateForm { copy(selectedColleagueId = colleagueId, notice = null) }

    override fun onQuickThanksClick(colleagueId: String) = onColleagueClick(colleagueId)

    override fun onAmountClick(amount: Long) {
        if (amount !in THANKS_AMOUNTS) return
        updateForm { copy(selectedAmount = amount, notice = null) }
    }

    override fun onMessageChange(message: String) =
        updateForm { copy(message = message.limitThanksMessage()) }

    override fun onSendClick() {
        val toUserId = form.selectedColleagueId ?: return
        if (form.isSending || overview == null) return
        updateForm { copy(isSending = true, notice = null) }
        viewModelScope.launch {
            val result = repository.sendThanks(
                toUserId = toUserId,
                amount = form.selectedAmount,
                message = form.message,
            )
            when (result) {
                ThanksSendResult.Success -> {
                    form = form.copy(
                        selectedColleagueId = null,
                        message = "",
                        isSending = false,
                        notice = THANKS_SENT_NOTICE,
                    )
                    publish()
                    refresh()
                }
                is ThanksSendResult.Failure -> updateForm {
                    copy(isSending = false, notice = result.error.toNotice())
                }
            }
        }
    }

    override fun onBackClick() = Unit

    private fun updateForm(change: ThanksForm.() -> ThanksForm) {
        form = form.change()
        publish()
    }

    private fun publish() {
        val data = overview ?: return
        _state.value = ThanksScreenState.Content(data.toUiState(form))
    }
}
