package com.example.apexhr.features.auth.domain

enum class EmailError { Empty, InvalidFormat }

enum class PasswordError { Empty }

data class LoginValidationResult(
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
) {
    val isValid: Boolean
        get() = emailError == null && passwordError == null
}

/**
 * Проверка полей формы входа до отправки на сервер.
 * Без android.util.Patterns, чтобы проверять обычными unit-тестами.
 */
object LoginValidator {

    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    fun validate(email: String, password: String): LoginValidationResult {
        return LoginValidationResult(
            emailError = validateEmail(email),
            passwordError = validatePassword(password),
        )
    }

    fun validateEmail(email: String): EmailError? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> EmailError.Empty
            !EMAIL_REGEX.matches(trimmed) -> EmailError.InvalidFormat
            else -> null
        }
    }

    /** Пароль не обрезаем: пробелы могут быть его частью. */
    fun validatePassword(password: String): PasswordError? {
        return if (password.isEmpty()) PasswordError.Empty else null
    }
}
