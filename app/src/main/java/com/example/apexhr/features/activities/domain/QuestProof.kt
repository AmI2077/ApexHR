package com.example.apexhr.features.activities.domain

/** Лимит файла подтверждения, как в вебе и в бакете proofs. */
const val MAX_PROOF_BYTES = 2 * 1024 * 1024

/** Допустимые файлы подтверждения: тип определяется по содержимому, как в вебе. */
enum class ProofFileType(
    val mimeType: String,
    val extension: String,
    val isImage: Boolean,
) {
    Jpeg("image/jpeg", "jpg", isImage = true),
    Png("image/png", "png", isImage = true),
    Webp("image/webp", "webp", isImage = true),
    Pdf("application/pdf", "pdf", isImage = false),
}

/** Тип файла по первым байтам; null — не JPEG, PNG, WebP или PDF. */
fun proofFileTypeOf(bytes: ByteArray): ProofFileType? {
    fun ascii(from: Int, to: Int) = String(bytes.copyOfRange(from, to), Charsets.ISO_8859_1)
    val pngSignature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    return when {
        bytes.size >= 24 && bytes.copyOfRange(0, 8).contentEquals(pngSignature) && ascii(12, 16) == "IHDR" ->
            ProofFileType.Png

        bytes.size >= 4 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte() ->
            ProofFileType.Jpeg

        bytes.size >= 12 && ascii(0, 4) == "RIFF" && ascii(8, 12) == "WEBP" -> ProofFileType.Webp
        bytes.size >= 4 && ascii(0, 4) == "%PDF" -> ProofFileType.Pdf
        else -> null
    }
}

/** Читает файл по адресу (content://); не больше [limit] байт. null — прочитать не удалось. */
fun interface ProofFileSource {
    suspend fun readBytes(uri: String, limit: Int): ByteArray?
}

sealed interface QuestProofResult {
    data object Success : QuestProofResult
    data class Failure(val error: QuestProofError) : QuestProofResult
}

sealed interface QuestProofError {
    /** Файл не выбран, а прежнего подтверждения нет. */
    data object NoFile : QuestProofError
    data object FileUnreadable : QuestProofError

    /** Не JPEG, PNG, WebP или PDF. */
    data object UnsupportedType : QuestProofError

    /** Больше 2 МБ (фото — даже после сжатия). */
    data object FileTooLarge : QuestProofError

    /** В токене нет id пользователя: путь файла не построить. */
    data object SessionMismatch : QuestProofError

    /** Хранилище отказало (4xx): например, нет прав на запись в бакет proofs. */
    data object UploadRejected : QuestProofError

    /** Ошибка запроса к бэкенду (загрузка в хранилище или отправка подтверждения). */
    data class Action(val error: ActivityActionError) : QuestProofError
}

interface QuestProofRepository {
    /**
     * Загружает файл в бакет proofs и отправляет подтверждение шага.
     * @param fileUri новый файл; null — оставить прежний [existingStoragePath] (только при обновлении).
     * @param update true — подтверждение на проверке, его заменяет PUT …/proof;
     * false — первое или повторное после отказа, POST …/complete.
     */
    suspend fun submitProof(
        questId: Long,
        stepId: Long,
        fileUri: String?,
        existingStoragePath: String?,
        comment: String?,
        update: Boolean,
    ): QuestProofResult
}