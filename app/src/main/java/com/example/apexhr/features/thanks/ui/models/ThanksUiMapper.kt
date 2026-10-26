package com.example.apexhr.features.thanks.ui.models

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.features.profile.ui.models.userInfoLabels
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.ThanksSendError

/** Суммы благодарности на выбор; первая — по умолчанию. */
val THANKS_AMOUNTS: List<Long> = listOf(10, 20, 30, 50)

/** Ограничение длины сообщения, как в веб-версии. */
const val THANKS_MESSAGE_MAX_LENGTH = 120

/** Введённые пользователем данные формы и состояние отправки. */
data class ThanksForm(
    val searchQuery: String = "",
    val selectedColleagueId: String? = null,
    val selectedAmount: Long = THANKS_AMOUNTS.first(),
    val message: String = "",
    val isSending: Boolean = false,
    val notice: ThanksNotice? = null,
)

/** Форма отправки по списку коллег (активные сотрудники, кроме текущего пользователя). */
fun List<Colleague>.toUiState(form: ThanksForm): ThanksUiState {
    // Выбранного ищем среди всех коллег: поиск не должен сбрасывать получателя.
    val selected = firstOrNull { it.id == form.selectedColleagueId }
    return ThanksUiState(
        searchQuery = form.searchQuery,
        colleagues = search(form.searchQuery).map { it.toUiModel() },
        selectedColleagueId = selected?.id,
        selectedColleagueName = selected?.name,
        amountOptions = THANKS_AMOUNTS,
        selectedAmount = form.selectedAmount,
        message = form.message,
        messageMaxLength = THANKS_MESSAGE_MAX_LENGTH,
        isSending = form.isSending,
        notice = form.notice,
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

/** Обрезает сообщение до допустимой длины. */
fun String.limitThanksMessage(): String = take(THANKS_MESSAGE_MAX_LENGTH)

fun Colleague.toUiModel() = ColleagueUiModel(
    id = id,
    name = name,
    position = userInfoLabels(position, department),
)

/** Сообщение об успешной отправке. */
val THANKS_SENT_NOTICE = ThanksNotice(textRes = R.string.thanks_sent, isError = false)

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
