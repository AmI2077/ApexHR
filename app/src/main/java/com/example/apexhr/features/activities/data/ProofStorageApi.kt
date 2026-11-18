package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.requireSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.ByteArrayContent

/** Файлы подтверждения шагов квестов в бакете proofs Supabase Storage. */
interface ProofStorageApi {
    /** Кладёт файл по пути «<id пользователя>/<файл>» внутри бакета, не перезаписывая существующий. */
    suspend fun upload(path: String, bytes: ByteArray, mimeType: String, accessToken: String)
}

/**
 * Как в вебе: токен пользователя и публичный anon-ключ, без служебного ключа.
 * @param client клиент без автоматического токена (AUTH_HTTP_CLIENT): отказ
 * хранилища не должен разлогинивать. Заголовки Supabase ставим вручную.
 */
class KtorProofStorageApi(
    private val client: HttpClient,
) : ProofStorageApi {
    override suspend fun upload(path: String, bytes: ByteArray, mimeType: String, accessToken: String) {
        client.post(NetworkConfig.proofObjectUrl(path)) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header(SUPABASE_API_KEY_HEADER, NetworkConfig.SUPABASE_ANON_KEY)
            header(HttpHeaders.CacheControl, CACHE_SECONDS)
            header(UPSERT_HEADER, "false")
            setBody(ByteArrayContent(bytes, ContentType.parse(mimeType)))
        }.requireSuccess()
    }

    private companion object {
        const val SUPABASE_API_KEY_HEADER = "apikey"
        const val UPSERT_HEADER = "x-upsert"
        const val CACHE_SECONDS = "3600"
    }
}