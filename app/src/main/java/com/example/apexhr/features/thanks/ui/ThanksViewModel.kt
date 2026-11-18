package com.example.apexhr.features.thanks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apexhr.core.domain.DataResult
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.MANAGER_BONUS_AMOUNTS
import com.example.apexhr.features.thanks.domain.ManagerBonusAccess
import com.example.apexhr.features.thanks.domain.PEER_THANKS_AMOUNT
import com.example.apexhr.features.thanks.domain.Thank
import com.example.apexhr.features.thanks.domain.recipientsThankedRecently
import com.example.apexhr.features.thanks.domain.ThanksRepository
import com.example.apexhr.features.thanks.domain.ThanksSendResult
import com.example.apexhr.features.thanks.ui.models.MANAGER_BONUS_AMOUNT_NOTICE
import com.example.apexhr.features.thanks.ui.models.MANAGER_BONUS_SENT_NOTICE
import com.example.apexhr.features.thanks.ui.models.THANKS_MESSAGE_MAX_LENGTH
import com.example.apexhr.features.thanks.ui.models.THANKS_SENT_NOTICE
import com.example.apexhr.features.thanks.ui.models.ThanksForm
import com.example.apexhr.features.thanks.ui.models.ThanksScreenCallbacks
import com.example.apexhr.features.thanks.ui.models.ThanksScreenState
import com.example.apexhr.features.thanks.ui.models.isValidThanksMessage
import com.example.apexhr.features.thanks.ui.models.limitThanksMessage
import com.example.apexhr.features.thanks.ui.models.toNotice
import com.example.apexhr.features.thanks.ui.models.toUiState
import java.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Форма отправки благодарности коллеге. После успешной отправки форма
 * очищается; историю благодарностей показывает и обновляет профиль.
 * Назад обрабатывает навигация (onBackClick переопределяется в AppNavigation).
 */
class ThanksViewModel(
    private val repository: ThanksRepository,
    private val clock: () -> Instant = Instant::now,
) : ViewModel(), ThanksScreenCallbacks {

    private val _state = MutableStateFlow<ThanksScreenState>(ThanksScreenState.Loading)
    val state: StateFlow<ThanksScreenState> = _state.asStateFlow()

    private var colleagues: List<Colleague>? = null

    /**
     * Мои отправленные благодарности для недельного лимита, как в вебе. Если историю
     * загрузить не удалось, лимит заранее не проверяется — его проверит сервер.
     */
    private var sentThanks: List<Thank> = emptyList()
    private var form = ThanksForm()

    /** Поощрение руководителя; без доступа или при ошибке — только благодарности. */
    private var access = ManagerBonusAccess.None
    private var loadJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.value = ThanksScreenState.Loading
        loadJob = viewModelScope.launch {
            val history = async { repository.loadHistory() }
            val bonusAccess = async { repository.loadManagerBonusAccess() }
            when (val result = repository.loadColleagues()) {
                is DataResult.Success -> {
                    sentThanks = (history.await() as? DataResult.Success)?.data?.sent.orEmpty()
                    access = (bonusAccess.await() as? DataResult.Success)?.data ?: ManagerBonusAccess.None
                    colleagues = result.data
                    publish()
                }
                is DataResult.Failure -> _state.value = ThanksScreenState.Error(result.error)
            }
        }
    }

    override fun onRetryClick() = load()

    override fun onSearchQueryChange(query: String) = updateForm { copy(searchQuery = query) }

    override fun onColleagueClick(colleagueId: String) =
        updateForm { copy(selectedColleagueId = colleagueId, notice = null) }

    override fun onQuickThanksClick(colleagueId: String) = onColleagueClick(colleagueId)

    override fun onMessageChange(message: String) =
        updateForm { copy(message = message.limitThanksMessage()) }

    override fun onManagerModeChange(enabled: Boolean) {
        if (!access.canReward) return
        updateForm { copy(asManager = enabled, notice = null) }
    }

    override fun onBonusAmountClick(amount: Long) {
        if (amount !in MANAGER_BONUS_AMOUNTS) return
        updateForm { copy(bonusAmount = amount, notice = null) }
    }

    override fun onSendClick() {
        val toUserId = form.selectedColleagueId ?: return
        if (form.isSending || colleagues == null) return
        if (access.canReward && form.asManager) {
            sendManagerBonus(toUserId)
            return
        }
        // Кнопка уже неактивна, но проверяем и здесь: короткое сообщение сервер не примет.
        if (!form.message.isValidThanksMessage()) return
        if (toUserId in thankedRecently()) return
        updateForm { copy(isSending = true, notice = null) }
        viewModelScope.launch {
            val result = repository.sendThanks(
                toUserId = toUserId,
                message = form.message,
            )
            when (result) {
                ThanksSendResult.Success -> {
                    sentThanks = sentThanks + Thank(
                        id = 0,
                        counterpartId = toUserId,
                        counterpartName = null,
                        amount = PEER_THANKS_AMOUNT,
                        message = null,
                        createdAt = clock(),
                    )
                    form = form.copy(
                        selectedColleagueId = null,
                        message = "",
                        isSending = false,
                        notice = THANKS_SENT_NOTICE,
                    )
                    publish()
                }
                is ThanksSendResult.Failure -> updateForm {
                    copy(isSending = false, notice = result.error.toNotice())
                }
            }
        }
    }

    /** Поощрение с рабочего баланса: сумма обязательна, сообщение — нет, лимита по неделе нет. */
    private fun sendManagerBonus(toUserId: String) {
        val amount = form.bonusAmount
        if (amount == null) {
            updateForm { copy(notice = MANAGER_BONUS_AMOUNT_NOTICE) }
            return
        }
        if (form.message.trim().length > THANKS_MESSAGE_MAX_LENGTH) return
        updateForm { copy(isSending = true, notice = null) }
        viewModelScope.launch {
            when (val result = repository.sendManagerBonus(toUserId, amount, form.message)) {
                ThanksSendResult.Success -> {
                    access = access.copy(workBalance = access.workBalance?.let { (it - amount).coerceAtLeast(0) })
                    // Как в вебе: сообщение и сумма очищаются, режим поощрения остаётся.
                    form = form.copy(
                        message = "",
                        bonusAmount = null,
                        isSending = false,
                        notice = MANAGER_BONUS_SENT_NOTICE,
                    )
                    publish()
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
        val data = colleagues ?: return
        _state.value = ThanksScreenState.Content(data.toUiState(form, thankedRecently(), access))
    }

    private fun thankedRecently(): Set<String> = sentThanks.recipientsThankedRecently(clock())
}
