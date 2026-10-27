package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

/** Файлы фото профиля в бакете avatars Supabase Storage. */
interface AvatarStorageApi {
    /** Кладёт JPEG по пути «<id пользователя>/<файл>.jpg» внутри бакета. */
    suspend fun upload(path: String, jpeg: ByteArray, accessToken: String)

    suspend fun delete(path: String, accessToken: String)
}

/**
 * @param client клиент без автоматического токена (AUTH_HTTP_CLIENT): авторизованный
 * подставляет bearer только для бэкенда и на любой 401 завершает сессию, а отказ
 * хранилища не должен разлогинивать. Заголовки Supabase ставим вручную.
 */
class KtorAvatarStorageApi(
    private val client: HttpClient,
) : AvatarStorageApi {
    override suspend fun upload(path: String, jpeg: ByteArray, accessToken: String) {
        client.post(NetworkConfig.avatarObjectUrl(path)) {
            supabaseHeaders(accessToken)
            setBody(ByteArrayContent(jpeg, ContentType.Image.JPEG))
        }.requireSuccess()
    }

    override suspend fun delete(path: String, accessToken: String) {
        client.delete(NetworkConfig.avatarObjectUrl(path)) {
            supabaseHeaders(accessToken)
        }.requireSuccess()
    }

    private fun HttpRequestBuilder.supabaseHeaders(accessToken: String) {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
        header(SUPABASE_API_KEY_HEADER, NetworkConfig.SUPABASE_ANON_KEY)
    }
}

private const val SUPABASE_API_KEY_HEADER = "apikey"
