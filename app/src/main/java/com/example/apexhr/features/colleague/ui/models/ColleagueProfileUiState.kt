package com.example.apexhr.features.colleague.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.profile.ui.models.ProfileAchievementUiModel
import com.example.apexhr.features.profile.ui.models.UserLabel

/** Благодарность коллеге; [fromMe] — отправил текущий пользователь («Вы»). */
data class ColleagueThankUiModel(
    val id: String,
    val fromMe: Boolean,
    val fromName: String?,
    val amount: Long,
    val message: String?,
    val date: String?,
)

data class ColleagueProfileUiState(
    val id: String,
    /** «Имя Фамилия»; пустая строка, если сервер не прислал ни того, ни другого. */
    val name: String,
    val avatarUrl: String?,
    /** «Должность · Отдел». */
    val info: List<UserLabel>,
    @StringRes val roleRes: Int,
    /** null — рейтинг недоступен, строка не показывается. */
    val thanksScore: Long?,
    val isActive: Boolean,
    val isMe: Boolean,
    val receivedThanks: List<ColleagueThankUiModel>,
    val achievements: List<ProfileAchievementUiModel>,
) {
    /** Поблагодарить можно только активного коллегу, не себя. */
    val canThank: Boolean
        get() = isActive && !isMe

    companion object {
        val Preview = ColleagueProfileUiState(
            id = "anna",
            name = "Анна Петрова",
            avatarUrl = null,
            info = listOf(UserLabel.Text("Менеджер по маркетингу"), UserLabel.Text("Маркетинг")),
            roleRes = R.string.role_employee,
            thanksScore = 12,
            isActive = true,
            isMe = false,
            receivedThanks = listOf(
                ColleagueThankUiModel("1", fromMe = true, fromName = null, amount = 10, message = "Спасибо за помощь с отчётом", date = "12.11.2026"),
            ),
            achievements = listOf(
                ProfileAchievementUiModel(id = "1", title = "Первая благодарность"),
            ),
        )
    }
}

sealed interface ColleagueProfileScreenState {
    data object Loading : ColleagueProfileScreenState
    data class Error(val error: DataError) : ColleagueProfileScreenState
    data class Content(val uiState: ColleagueProfileUiState) : ColleagueProfileScreenState
}