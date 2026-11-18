package com.example.apexhr.features.profile.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.PageSlice
import com.example.apexhr.core.ui.model.pageSlice
import com.example.apexhr.features.profile.ui.history.models.TransactionHistoryState

enum class ProfileTab {
    Thanks,
    Achievements,
    Operations,
}

data class ProfileUiState(
    val name: String,
    /** «Должность · Отдел» по частям; служебные английские значения переведены. */
    val department: List<UserLabel>,
    val balance: Long,
    val selectedTab: ProfileTab,
    /** Полученные благодарности, от новых к старым. */
    val received: List<ThankUiModel>,
    /** Отправленные благодарности, от новых к старым. */
    val sent: List<ThankUiModel>,
    val achievements: List<ProfileAchievementUiModel>,
    val avatarUrl: String? = null,
    /** Вкладка «Операции»: загружается при первом открытии вкладки. */
    val operations: TransactionHistoryState = TransactionHistoryState.Loading,
    /** Идёт загрузка нового фото профиля. */
    val isAvatarUploading: Boolean = false,
    /** Почему не удалось сменить фото; null — ошибки нет. */
    @StringRes val avatarErrorRes: Int? = null,
    /** Название уровня активности («Искра»…); null — уровень не загрузился. */
    val activityLevel: String? = null,
    /** Страницы благодарностей, как в вебе: по [THANKS_PAGE_SIZE], у каждого списка своя. */
    val receivedPage: Int = 1,
    val sentPage: Int = 1,
) {
    val receivedSlice: PageSlice<ThankUiModel> get() = received.pageSlice(receivedPage, THANKS_PAGE_SIZE)
    val sentSlice: PageSlice<ThankUiModel> get() = sent.pageSlice(sentPage, THANKS_PAGE_SIZE)

    companion object {
        /** Благодарностей на странице профиля, как в вебе. */
        const val THANKS_PAGE_SIZE = 5

        val Preview = ProfileUiState(
            name = "Olivia Martinez",
            department = listOf(
                UserLabel.Resource(R.string.role_employee),
                UserLabel.Text("Marketing Department"),
            ),
            balance = 1_250,
            selectedTab = ProfileTab.Thanks,
            received = listOf(
                ThankUiModel(
                    id = "1",
                    name = "Иван Соколов",
                    amount = 30,
                    message = "Спасибо за помощь с релизом",
                    date = "25.10.2026",
                ),
                ThankUiModel(id = "2", name = null, amount = 10, message = null, date = "24.10.2026"),
            ),
            sent = emptyList(),
            achievements = listOf(
                ProfileAchievementUiModel(id = "steps", title = "10K шагов"),
                ProfileAchievementUiModel(id = "activity", title = "Активность"),
                ProfileAchievementUiModel(
                    id = "goal",
                    title = "Целеустремлённость",
                    description = "Выполните 5 активностей за месяц",
                    isEarned = false,
                ),
            ),
        )
    }
}

/**
 * Благодарность в списке полученных или отправленных.
 * @param name отправитель или получатель; null, если сотрудник не найден.
 * @param date дата в часовом поясе устройства; null, если сервер не прислал.
 */
data class ThankUiModel(
    val id: String,
    val name: String?,
    val amount: Long,
    val message: String?,
    val date: String?,
)

data class ProfileAchievementUiModel(
    val id: String,
    val title: String,
    /** Условие получения; показывается только у неполученного достижения. */
    val description: String? = null,
    val isEarned: Boolean = true,
)

/** Состояние экрана профиля с учётом загрузки данных. */
sealed interface ProfileScreenState {
    data object Loading : ProfileScreenState
    data class Error(val error: DataError) : ProfileScreenState
    data class Content(val uiState: ProfileUiState) : ProfileScreenState
}
