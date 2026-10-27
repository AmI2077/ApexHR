package com.example.apexhr.features.profile.domain

import com.example.apexhr.core.domain.DataError

/** Смена фото профиля. */
interface AvatarRepository {
    /**
     * Сжимает выбранное изображение, загружает его новым файлом в хранилище,
     * сохраняет ссылку в профиле и удаляет прежний файл (ошибку удаления игнорирует).
     * @param imageUri content:// адрес из системного выбора фото.
     */
    suspend fun changeAvatar(imageUri: String): AvatarChangeResult
}

sealed interface AvatarChangeResult {
    data class Success(val avatarUrl: String) : AvatarChangeResult
    data class Failure(val error: AvatarChangeError) : AvatarChangeResult
}

sealed interface AvatarChangeError {
    /** Ошибка сети или бэкенда. */
    data class Data(val error: DataError) : AvatarChangeError

    /** id в токене не совпадает с id профиля (или токен не разобрать) — файл не загружаем. */
    data object SessionMismatch : AvatarChangeError

    /** Изображение не удалось прочитать или сжать. */
    data object ImageUnreadable : AvatarChangeError

    /** Даже после сжатия файл больше лимита бакета. */
    data object ImageTooLarge : AvatarChangeError

    /** Хранилище отказало (нет прав, токен не принят и т.п.). */
    data object UploadRejected : AvatarChangeError
}

/** Читает изображение по адресу и сжимает в JPEG для аватара; null — не получилось. */
fun interface AvatarImageCompressor {
    suspend fun compress(imageUri: String): ByteArray?
}
