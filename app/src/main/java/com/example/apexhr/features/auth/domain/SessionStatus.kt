package com.example.apexhr.features.auth.domain

sealed interface SessionStatus {
    /** Токен есть и сервер его принимает. */
    data object Active : SessionStatus

    /** Токена нет (или его не удалось расшифровать). */
    data object NotAuthorized : SessionStatus

    /** Токен истёк или сервер ответил 401. Токены уже удалены. */
    data object Expired : SessionStatus

    /** Не удалось проверить сессию (сеть, сервер). Токены сохранены. */
    data class CheckFailed(val error: AuthError) : SessionStatus
}
