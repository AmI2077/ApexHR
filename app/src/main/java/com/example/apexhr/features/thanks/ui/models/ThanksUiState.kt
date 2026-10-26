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
    val searchQuery: String,
    /** Коллеги, подходящие под поиск. */
    val colleagues: List<ColleagueUiModel>,
    val selectedColleagueId: String?,
    /** Имя выбранного коллеги; null, если не выбран или имя неизвестно. */
    val selectedColleagueName: String?,
    val amountOptions: List<Long>,
    val selectedAmount: Long,
    val message: String,
    val messageMaxLength: Int,
    val isSending: Boolean,
    /** Итог последней отправки; null, пока ничего не отправляли. */
    val notice: ThanksNotice?,
    val received: List<ThankUiModel>,
    val sent: List<ThankUiModel>,
) {
    val canSend: Boolean
        get() = selectedColleagueId != null && !isSending

    /** Список пуст из-за поиска, а не потому, что коллег нет. */
    val isNothingFound: Boolean
        get() = colleagues.isEmpty() && searchQuery.isNotBlank()

    companion object {
        val Preview = ThanksUiState(
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
            amountOptions = listOf(10, 20, 30, 50),
            selectedAmount = 20,
            message = "",
            messageMaxLength = 120,
            isSending = false,
            notice = ThanksNotice(
                textRes = R.string.thanks_error_server_message,
                args = listOf("Weekly thanks limit exceeded"),
                isError = true,
            ),
            received = listOf(
                ThankUiModel(
                    id = "1",
                    name = "Иван Соколов",
                    amount = 30,
                    message = "Спасибо за помощь с релизом",
                    date = "25.10.2026",
                ),
            ),
            sent = emptyList(),
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
 * Благодарность в списке полученных или отправленных.
 * @param name отправитель или получатель; null, если сотрудник не найден.
 */
data class ThankUiModel(
    val id: String,
    val name: String?,
    val amount: Long,
    val message: String?,
    val date: String?,
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
