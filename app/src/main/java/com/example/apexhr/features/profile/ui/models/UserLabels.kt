package com.example.apexhr.features.profile.ui.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.example.apexhr.R

/**
 * Часть строки «Должность · Отдел»: текст с сервера как есть или перевод
 * служебного английского значения из строковых ресурсов.
 */
sealed interface UserLabel {
    data class Text(val value: String) : UserLabel
    data class Resource(@StringRes val resId: Int) : UserLabel
}

/**
 * Должность и отдел — свободный текст, но сервер подставляет английские значения
 * по умолчанию: position = "Employee", department = "Unassigned". Коды ролей
 * из Swagger и /api/users (employee, manager, admin, superadmin) переводим так же,
 * если они попадут в должность. Сравниваем без регистра, пробелов, «_» и «-».
 */
private val POSITION_LABELS: Map<String, Int> = mapOf(
    "employee" to R.string.profile_position_employee,
    "manager" to R.string.profile_position_manager,
    "admin" to R.string.profile_position_admin,
    "administrator" to R.string.profile_position_admin,
    "superadmin" to R.string.profile_position_superadmin,
    "superadministrator" to R.string.profile_position_superadmin,
    "unassigned" to R.string.profile_position_unassigned,
)

private val DEPARTMENT_LABELS: Map<String, Int> = mapOf(
    "unassigned" to R.string.profile_department_unassigned,
)

/** «Должность · Отдел» по частям; пустые части пропускаются. */
fun userInfoLabels(position: String?, department: String?): List<UserLabel> = listOfNotNull(
    position.toLabel(POSITION_LABELS),
    department.toLabel(DEPARTMENT_LABELS),
)

private fun String?.toLabel(known: Map<String, Int>): UserLabel? {
    val value = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val key = value.lowercase().filter { it.isLetterOrDigit() }
    return known[key]?.let { UserLabel.Resource(it) } ?: UserLabel.Text(value)
}

@Composable
@ReadOnlyComposable
fun UserLabel.text(): String = when (this) {
    is UserLabel.Text -> value
    is UserLabel.Resource -> stringResource(resId)
}

/** Части через « · », например «Сотрудник · Без отдела». */
@Composable
@ReadOnlyComposable
fun List<UserLabel>.joinedText(): String = map { it.text() }.joinToString(" · ")
