package com.example.apexhr.features.thanks.ui.models

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.profile.ui.models.userInfoLabels
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.MANAGER_BONUS_AMOUNTS
import com.example.apexhr.features.thanks.domain.ManagerBonusAccess
import com.example.apexhr.features.thanks.domain.PEER_THANKS_AMOUNT
import com.example.apexhr.features.thanks.domain.ThanksSendError

/** Ограничение длины сообщения, как в веб-версии. */
const val THANKS_MESSAGE_MAX_LENGTH = 120

/** Минимальная длина сообщения без пробелов по краям, как в веб-версии. */
const val THANKS_MESSAGE_MIN_LENGTH = 2

/** Введённые пользователем данные формы и состояние отправки. */
data class ThanksForm(
    val searchQuery: String = "",
    val selectedColleagueId: String? = null,
    val message: String = "",
    val isSending: Boolean = false,
    val notice: ThanksNotice? = null,
    /** Галочка «Поощрение с рабочего баланса»; действует только у руководителя. */
    val asManager: Boolean = false,
    val bonusAmount: Long? = null,
)

/**
 * Форма отправки по списку коллег (активные сотрудники, кроме текущего пользователя).
 * @param thankedRecently кого уже благодарили на этой неделе: им кнопка отправки недоступна.
 */
fun List<Colleague>.toUiState(
    form: ThanksForm,
    thankedRecently: Set<String> = emptySet(),
    access: ManagerBonusAccess = ManagerBonusAccess.None,
): ThanksUiState {
    val isManagerMode = access.canReward && form.asManager
    // Выбранного ищем среди всех коллег: поиск не должен сбрасывать получателя.
    val selected = firstOrNull { it.id == form.selectedColleagueId }
    return ThanksUiState(
        amount = PEER_THANKS_AMOUNT,
        isSelectedThankedRecently = selected != null && selected.id in thankedRecently,
        searchQuery = form.searchQuery,
        colleagues = search(form.searchQuery).map { it.toUiModel() },
        selectedColleagueId = selected?.id,
        selectedColleagueName = selected?.name,
        message = form.message,
        messageMinLength = THANKS_MESSAGE_MIN_LENGTH,
        messageMaxLength = THANKS_MESSAGE_MAX_LENGTH,
        isSending = form.isSending,
        notice = form.notice,
        canReward = access.canReward,
        isManagerMode = isManagerMode,
        bonusAmounts = if (isManagerMode) MANAGER_BONUS_AMOUNTS else emptyList(),
        selectedBonusAmount = form.bonusAmount.takeIf { isManagerMode },
        workBalance = access.workBalance,
    )
}

/**
 * Поиск без учёта регистра по имени, должности и отделу; каждое слово
 * запроса должно найтись хотя бы в одном из полей.
 */
fun List<Colleague>.search(query: String): List<Colleague> {
    val words = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.isEmpty()) return this
    return filter { colleague ->
        val fields = listOfNotNull(colleague.name, colleague.position, colleague.department)
            .map { it.lowercase() }
        words.all { word -> fields.any { it.contains(word) } }
    }
}

/** Сообщение достаточно длинное для отправки. */
fun String.isValidThanksMessage(): Boolean = trim().length >= THANKS_MESSAGE_MIN_LENGTH

/** Обрезает сообщение до допустимой длины. */
fun String.limitThanksMessage(): String = take(THANKS_MESSAGE_MAX_LENGTH)

fun Colleague.toUiModel() = ColleagueUiModel(
    id = id,
    name = name,
    position = userInfoLabels(position, department),
)

/** Сообщение об успешной отправке. */
val THANKS_SENT_NOTICE = ThanksNotice(textRes = R.string.thanks_sent, isError = false)

/** Поощрение руководителя отправлено, как тост в вебе. */
val MANAGER_BONUS_SENT_NOTICE = ThanksNotice(textRes = R.string.thanks_bonus_sent, isError = false)

/** Сумма поощрения не выбрана. */
val MANAGER_BONUS_AMOUNT_NOTICE = ThanksNotice(textRes = R.string.thanks_error_bonus_amount, isError = true)

/** Текст ошибки отправки; текст сервера (например, о недельном лимите) показывается как есть. */
fun ThanksSendError.toNotice(): ThanksNotice = when (this) {
    ThanksSendError.NotFound -> ThanksNotice(R.string.thanks_error_not_found, isError = true)
    ThanksSendError.Forbidden -> ThanksNotice(R.string.thanks_error_forbidden, isError = true)
    is ThanksSendError.Rejected -> if (serverMessage.isNullOrBlank()) {
        ThanksNotice(R.string.thanks_error_send, isError = true)
    } else {
        ThanksNotice(R.string.thanks_error_server_message, listOf(serverMessage), isError = true)
    }
    // «Не удалось загрузить данные» не подходит к отправке — свой общий текст.
    is ThanksSendError.Data -> if (error == DataError.Unknown) {
        ThanksNotice(R.string.thanks_error_send, isError = true)
    } else {
        ThanksNotice(error.messageRes(), isError = true)
    }
}
