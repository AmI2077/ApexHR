package com.example.apexhr.core.data.network

object NetworkConfig {
    const val HOST = "apexapp-backend.onrender.com"
    const val BASE_URL = "https://$HOST"

    const val BASE_LOGIN_URL = "$BASE_URL/api/Auth/login"
    const val CURRENT_USER_URL = "$BASE_URL/api/Auth/me"
    const val CHANGE_PASSWORD_URL = "$BASE_URL/api/Auth/change-password"

    // Профиль
    const val USER_PROFILE_URL = "$BASE_URL/api/users/me"
    const val BALANCE_URL = "$BASE_URL/api/balances/me"
    const val BALANCE_TRANSACTIONS_URL = "$BASE_URL/api/balances/me/transactions"
    fun userAchievementsUrl(userId: String) = "$BASE_URL/api/achievements/users/$userId"

    // Supabase Storage: фото профиля (бакет avatars, лимит 2 МБ).
    // Токен бэкенда — это JWT Supabase, им и авторизуемся в хранилище.
    const val SUPABASE_URL = "https://hqvphndmvluncjfsqspf.supabase.co"

    /** Публичный anon-ключ проекта: он и так открыт в вебе, секретом не является. */
    const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhxdnBobmRtdmx1bmNqZnNxc3BmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTIxNTY0MjEsImV4cCI6MjEwNzczMjQyMX0.-VxhzXAXnnp03RxVdlAvlx0xyKzb7G963QLJvSZkf0o"
    const val AVATARS_OBJECT_URL = "$SUPABASE_URL/storage/v1/object/avatars"
    const val AVATARS_PUBLIC_URL = "$SUPABASE_URL/storage/v1/object/public/avatars"
    fun avatarObjectUrl(path: String) = "$AVATARS_OBJECT_URL/$path"
    fun avatarPublicUrl(path: String) = "$AVATARS_PUBLIC_URL/$path"

    // Благодарности (текущий пользователь — тот же USER_PROFILE_URL, что у профиля)
    const val USERS_URL = "$BASE_URL/api/users"
    const val THANKS_URL = "$BASE_URL/api/thanks"
    const val THANKS_FOR_ME_URL = "$THANKS_URL/for-me"
    const val THANKS_FROM_ME_URL = "$THANKS_URL/from-me"

    // Каталог
    const val CATEGORIES_URL = "$BASE_URL/api/categories"
    const val PRODUCTS_URL = "$BASE_URL/api/products"

    // Корзина и заказы
    const val CART_URL = "$BASE_URL/api/cart"
    const val CART_ITEMS_URL = "$BASE_URL/api/cart/items"
    fun cartItemUrl(itemId: Long) = "$CART_ITEMS_URL/$itemId"
    const val ORDERS_URL = "$BASE_URL/api/orders/history"
    const val ORDERS_CHECKOUT_URL = "$BASE_URL/api/orders/checkout"

    // Главная
    const val QUESTS_URL = "$BASE_URL/api/quests"
    const val LEADERBOARD_URL = "$BASE_URL/api/leaderboard"

    // Активности (список квестов — тот же QUESTS_URL, что у главной)
    const val SURVEYS_URL = "$BASE_URL/api/surveys"
    const val EVENTS_URL = "$BASE_URL/api/events"
    fun surveyUrl(surveyId: Long) = "$SURVEYS_URL/$surveyId"
    fun surveyResponsesUrl(surveyId: Long) = "${surveyUrl(surveyId)}/responses"
    fun questUrl(questId: Long) = "$QUESTS_URL/$questId"
    fun questJoinUrl(questId: Long) = "${questUrl(questId)}/join"
    fun questCompleteUrl(questId: Long) = "${questUrl(questId)}/complete"
    fun questStepCompleteUrl(questId: Long, stepId: Long) = "${questUrl(questId)}/steps/$stepId/complete"
    fun eventUrl(eventId: Long) = "$EVENTS_URL/$eventId"
    fun eventRegisterUrl(eventId: Long) = "${eventUrl(eventId)}/register"
    fun eventRegistrationUrl(eventId: Long) = "${eventUrl(eventId)}/registration"
    fun eventRewardUrl(eventId: Long) = "${eventUrl(eventId)}/reward"

    // Бэкенд крутится на Render: после простоя «холодный старт» может
    // занимать десятки секунд, поэтому таймауты заметно больше обычных.
    const val REQUEST_TIMEOUT_MILLIS = 60_000L
    const val CONNECT_TIMEOUT_MILLIS = 30_000L
    const val SOCKET_TIMEOUT_MILLIS = 60_000L
}
