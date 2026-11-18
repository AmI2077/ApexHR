package com.example.apexhr.features.thanks.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.profile.ui.models.UserLabel

sealed interface ThanksScreenState {
    data object Loading : ThanksScreenState
    data class Error(val error: DataError) : ThanksScreenState
    data class Content(val uiState: ThanksUiState) : ThanksScreenState
}

data class ThanksUiState(
    /** Сколько апексов получит коллега. */
    val amount: Long,
    /** Выбранному коллеге уже отправлена благодарность на этой неделе. */
    val isSelectedThankedRecently: Boolean,
    val searchQuery: String,
    /** Коллеги, подходящие под поиск. */
    val colleagues: List<ColleagueUiModel>,
    val selectedColleagueId: String?,
    /** Имя выбранного коллеги; null, если не выбран или имя неизвестно. */
    val selectedColleagueName: String?,
    val message: String,
    /** Минимум символов без пробелов по краям. */
    val messageMinLength: Int,
    val messageMaxLength: Int,
    val isSending: Boolean,
    /** Итог последней отправки; null, пока ничего не отправляли. */
    val notice: ThanksNotice?,
) {
    val canSend: Boolean
        get() = selectedColleagueId != null && !isSelectedThankedRecently && !isSending &&
            message.trim().length >= messageMinLength

    /** Список пуст из-за поиска, а не потому, что коллег нет. */
    val isNothingFound: Boolean
        get() = colleagues.isEmpty() && searchQuery.isNotBlank()

    companion object {
        val Preview = ThanksUiState(
            amount = 25,
            isSelectedThankedRecently = false,
            searchQuery = "",
            colleagues = listOf(
                ColleagueUiModel(
                    id = "anna",
                    name = "Анна Петрова",
                    position = listOf(UserLabel.Text("Менеджер по маркетингу")),
                ),
                ColleagueUiModel(
                    id = "ivan",
                    name = "Иван Соколов",
                    position = listOf(UserLabel.Text("Руководитель разработки")),
                ),
                ColleagueUiModel(
                    id = "maria",
                    name = "Мария Лебедева",
                    position = listOf(UserLabel.Text("Специалист по работе с клиентами")),
                ),
                ColleagueUiModel(
                    id = "dmitry",
                    name = null,
                    position = listOf(UserLabel.Text("Аналитик")),
                ),
            ),
            selectedColleagueId = "anna",
            selectedColleagueName = "Анна Петрова",
            message = "Спасибо за помощь!",
            messageMinLength = 2,
            messageMaxLength = 120,
            isSending = false,
            notice = ThanksNotice(
                textRes = R.string.thanks_error_server_message,
                args = listOf("Этому коллеге благодарность уже отправлена на этой неделе"),
                isError = true,
            ),
        )
    }
}

data class ColleagueUiModel(
    val id: String,
    /** null, если сервер не прислал имя: экран показывает заглушку. */
    val name: String?,
    /** «Должность · Отдел» по частям, как в профиле. */
    val position: List<UserLabel>,
)

/**
 * Итог отправки под кнопкой.
 * @param args аргументы строки [textRes].
 */
data class ThanksNotice(
    @StringRes val textRes: Int,
    val args: List<Any> = emptyList(),
    val isError: Boolean,
)
