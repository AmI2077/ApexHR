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
 * по умолчанию: position = "Employee", department = "Unassigned". Коды ролей,
 * если они попадут в должность, переводим теми же названиями, что и веб
 * (ROLE_LABEL / roleCodeLabel в lib/labels.ts): сотрудник, руководитель, HR,
 * мерчендайзер, оператор поддержки, администратор, главный администратор.
 * Сравниваем без регистра, пробелов, «_» и «-».
 */
private val POSITION_LABELS: Map<String, Int> = mapOf(
    "employee" to R.string.role_employee,
    "user" to R.string.role_employee,
    "manager" to R.string.role_manager,
    "hr" to R.string.role_hr,
    "humanresources" to R.string.role_hr,
    "merchandiser" to R.string.role_merchandiser,
    "merch" to R.string.role_merchandiser,
    "support" to R.string.role_support,
    "supportoperator" to R.string.role_support,
    "operator" to R.string.role_support,
    "helpdesk" to R.string.role_support,
    "admin" to R.string.role_admin,
    "administrator" to R.string.role_admin,
    "superadmin" to R.string.role_chief,
    "superadministrator" to R.string.role_chief,
    "chief" to R.string.role_chief,
    "owner" to R.string.role_chief,
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
