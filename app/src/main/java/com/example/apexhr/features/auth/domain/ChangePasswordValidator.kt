package com.example.apexhr.features.auth.domain

enum class NewPasswordError { Empty, TooShort }

enum class ConfirmPasswordError { Empty, Mismatch }

data class ChangePasswordValidationResult(
    val newPasswordError: NewPasswordError? = null,
    val confirmPasswordError: ConfirmPasswordError? = null,
) {
    val isValid: Boolean
        get() = newPasswordError == null && confirmPasswordError == null
}

/**
 * Проверка формы смены пароля до отправки на сервер.
 * Пароль не обрезаем, как и при входе: пробелы могут быть его частью,
 * но пароль только из пробелов считаем пустым.
 */
object ChangePasswordValidator {

    /** Как в веб-версии; если у сервера правило строже, покажем его ответ. */
    const val MIN_PASSWORD_LENGTH = 6

    fun validate(newPassword: String, confirmPassword: String): ChangePasswordValidationResult {
        val newPasswordError = when {
            newPassword.isBlank() -> NewPasswordError.Empty
            newPassword.length < MIN_PASSWORD_LENGTH -> NewPasswordError.TooShort
            else -> null
        }

        val confirmPasswordError = when {
            confirmPassword.isEmpty() -> ConfirmPasswordError.Empty
            confirmPassword != newPassword -> ConfirmPasswordError.Mismatch
            else -> null
        }

        return ChangePasswordValidationResult(newPasswordError, confirmPasswordError)
    }
}