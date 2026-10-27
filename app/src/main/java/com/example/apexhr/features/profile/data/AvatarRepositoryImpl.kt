package com.example.apexhr.features.profile.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.toDataError
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.profile.data.dto.UpdateOwnProfileRequest
import com.example.apexhr.features.profile.domain.AvatarChangeError
import com.example.apexhr.features.profile.domain.AvatarChangeResult
import com.example.apexhr.features.profile.domain.AvatarImageCompressor
import com.example.apexhr.features.profile.domain.AvatarRepository
import kotlinx.coroutines.CancellationException
import java.util.UUID

/** Лимит бакета avatars. */
const val MAX_AVATAR_BYTES = 2 * 1024 * 1024

class AvatarRepositoryImpl(
    private val profileApi: ProfileApi,
    private val storageApi: AvatarStorageApi,
    private val tokenStorage: TokenStorage,
    private val compressor: AvatarImageCompressor,
    private val newFileId: () -> String = { UUID.randomUUID().toString() },
) : AvatarRepository {

    override suspend fun changeAvatar(imageUri: String): AvatarChangeResult {
        // Профиль — авторизованным клиентом: при 401 он сам завершит сессию.
        // Токен читаем после запроса, чтобы взять актуальный.
        val user = try {
            profileApi.getCurrentUser()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return failure(AvatarChangeError.Data(e.toDataError()))
        }
        val token = tokenStorage.getAccessToken()
            ?: return failure(AvatarChangeError.Data(DataError.Unauthorized))
        val userId = jwtSubject(token)
        if (userId == null || !userId.equals(user.id, ignoreCase = true)) {
            return failure(AvatarChangeError.SessionMismatch)
        }

        val jpeg = compressor.compress(imageUri)
            ?: return failure(AvatarChangeError.ImageUnreadable)
        if (jpeg.size > MAX_AVATAR_BYTES) return failure(AvatarChangeError.ImageTooLarge)

        // Каждый раз новый файл: старая ссылка не отдаст новое фото из кэша.
        val path = "$userId/${newFileId()}.jpg"
        try {
            storageApi.upload(path, jpeg, token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return failure(e.toUploadError())
        }

        val avatarUrl = NetworkConfig.avatarPublicUrl(path)
        try {
            profileApi.updateOwnProfile(
                UpdateOwnProfileRequest(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    middleName = user.middleName,
                    avatarUrl = avatarUrl,
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // Профиль на новый файл не ссылается — убираем его.
            deleteQuietly(path, token)
            return failure(AvatarChangeError.Data(e.toDataError()))
        }

        avatarPathOf(user.avatarUrl, userId)
            ?.takeIf { it != path }
            ?.let { deleteQuietly(it, token) }

        return AvatarChangeResult.Success(avatarUrl)
    }

    private suspend fun deleteQuietly(path: String, token: String) {
        try {
            storageApi.delete(path, token)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            // Лишний файл в хранилище не мешает пользователю.
        }
    }

    private fun failure(error: AvatarChangeError) = AvatarChangeResult.Failure(error)
}

/**
 * Путь файла внутри бакета avatars, если [url] ведёт в него и в папку
 * пользователя [userId]; иначе null (внешние ссылки не трогаем).
 */
fun avatarPathOf(url: String?, userId: String): String? {
    val path = url
        ?.removePrefix("${NetworkConfig.AVATARS_PUBLIC_URL}/")
        ?.takeIf { it != url }
        ?.substringBefore('?')
        ?: return null
    if (path.contains("..")) return null
    return path.takeIf { it.startsWith("$userId/", ignoreCase = true) && it.length > userId.length + 1 }
}

private fun Throwable.toUploadError(): AvatarChangeError {
    val status = (this as? ApiStatusException)?.statusCode
        ?: return AvatarChangeError.Data(toDataError())
    return when (status) {
        413 -> AvatarChangeError.ImageTooLarge
        in 400..499 -> AvatarChangeError.UploadRejected
        else -> AvatarChangeError.Data(toDataError())
    }
}
