package com.example.apexhr.core.data.network

object NetworkConfig {
    const val HOST = "apexapp-backend.onrender.com"
    const val BASE_URL = "https://$HOST"

    const val BASE_LOGIN_URL = "$BASE_URL/api/Auth/login"
    const val CURRENT_USER_URL = "$BASE_URL/api/Auth/me"

    // Профиль
    const val USER_PROFILE_URL = "$BASE_URL/api/users/me"
    const val BALANCE_URL = "$BASE_URL/api/balances/me"
    const val BALANCE_TRANSACTIONS_URL = "$BASE_URL/api/balances/me/transactions"
    fun userAchievementsUrl(userId: String) = "$BASE_URL/api/achievements/users/$userId"

    // Каталог
    const val CATEGORIES_URL = "$BASE_URL/api/categories"
    const val PRODUCTS_URL = "$BASE_URL/api/products"

    // Главная
    const val QUESTS_URL = "$BASE_URL/api/quests"
    const val LEADERBOARD_URL = "$BASE_URL/api/leaderboard"

    // Бэкенд крутится на Render: после простоя «холодный старт» может
    // занимать десятки секунд, поэтому таймауты заметно больше обычных.
    const val REQUEST_TIMEOUT_MILLIS = 60_000L
    const val CONNECT_TIMEOUT_MILLIS = 30_000L
    const val SOCKET_TIMEOUT_MILLIS = 60_000L
}
