package com.example.apexhr.core.data.network

object NetworkConfig {
    const val HOST = "apexapp-backend.onrender.com"
    const val BASE_URL = "https://$HOST"

    const val BASE_LOGIN_URL = "$BASE_URL/api/Auth/login"

    // Бэкенд крутится на Render: после простоя «холодный старт» может
    // занимать десятки секунд, поэтому таймауты заметно больше обычных.
    const val REQUEST_TIMEOUT_MILLIS = 60_000L
    const val CONNECT_TIMEOUT_MILLIS = 30_000L
    const val SOCKET_TIMEOUT_MILLIS = 60_000L
}
