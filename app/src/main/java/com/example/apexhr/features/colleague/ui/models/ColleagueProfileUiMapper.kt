package com.example.apexhr.features.colleague.ui.models

import androidx.annotation.StringRes
import com.example.apexhr.R
import com.example.apexhr.core.ui.model.takeIfHttpUrl
import com.example.apexhr.features.colleague.domain.ColleagueProfile
import com.example.apexhr.features.colleague.domain.ColleagueThank
import com.example.apexhr.features.colleague.domain.UserRole
import com.example.apexhr.features.profile.ui.models.ProfileAchievementUiModel
import com.example.apexhr.features.profile.ui.models.userInfoLabels
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val COLLEAGUE_THANK_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@StringRes
fun UserRole.labelRes(): Int = when (this) {
    UserRole.Employee -> R.string.role_employee
    UserRole.Manager -> R.string.role_manager
    UserRole.Hr -> R.string.role_hr
    UserRole.Merchandiser -> R.string.role_merchandiser
    UserRole.Support -> R.string.role_support
    UserRole.Admin -> R.string.role_admin
    UserRole.Chief -> R.string.role_chief
}

fun ColleagueThank.toUiModel(zone: ZoneId = ZoneId.systemDefault()) = ColleagueThankUiModel(
    id = id.toString(),
    fromMe = fromMe,
    fromName = fromName,
    amount = amount,
    message = message,
    date = createdAt?.atZone(zone)?.format(COLLEAGUE_THANK_DATE_FORMAT),
)

fun ColleagueProfile.toUiState(zone: ZoneId = ZoneId.systemDefault()) = ColleagueProfileUiState(
    id = id,
    name = listOfNotNull(firstName, lastName).joinToString(" "),
    avatarUrl = avatarUrl.takeIfHttpUrl(),
    info = userInfoLabels(position, department),
    roleRes = role.labelRes(),
    thanksScore = thanksScore,
    isActive = isActive,
    isMe = isMe,
    receivedThanks = receivedThanks.map { it.toUiModel(zone) },
    achievements = achievements.map { achievement ->
        ProfileAchievementUiModel(
            id = achievement.id.toString(),
            title = achievement.title.orEmpty(),
            description = achievement.description,
            isEarned = true,
        )
    },
)