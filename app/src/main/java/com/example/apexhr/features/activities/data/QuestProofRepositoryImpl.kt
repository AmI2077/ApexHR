package com.example.apexhr.features.activities.data

import com.example.apexhr.core.data.network.ApiStatusException
import com.example.apexhr.core.data.network.NetworkConfig
import com.example.apexhr.core.data.network.toDataError
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.activities.data.dto.CompleteQuestStepRequest
import com.example.apexhr.features.activities.domain.ActivityActionError
import com.example.apexhr.features.activities.domain.MAX_PROOF_BYTES
import com.example.apexhr.features.activities.domain.ProofFileSource
import com.example.apexhr.features.activities.domain.ProofFileType
import com.example.apexhr.features.activities.domain.QuestProofError
import com.example.apexhr.features.activities.domain.QuestProofRepository
import com.example.apexhr.features.activities.domain.QuestProofResult
import com.example.apexhr.features.activities.domain.proofFileTypeOf
import com.example.apexhr.features.profile.data.jwtSubject
import com.example.apexhr.features.profile.domain.AvatarImageCompressor
import java.util.UUID
import kotlinx.coroutines.CancellationException

/**
 * Подтверждение шага, как в вебе: файл кладётся в бакет proofs по пути
 * «<id пользователя из токена>/<uuid>.<расширение>» с токеном пользователя и
 * anon-ключом, в бэкенд уходит публичная ссылка на него.
 * Фото больше 2 МБ сжимается так же, как аватар; PDF больше 2 МБ не принимается.
 */
class QuestProofRepositoryImpl(
    private val api: QuestProofApi,
    private val storageApi: ProofStorageApi,
    private val tokenStorage: TokenStorage,
    private val fileSource: ProofFileSource,
    private val compressor: AvatarImageCompressor,
    private val newFileId: () -> String = { UUID.randomUUID().toString() },
) : QuestProofRepository {

    override suspend fun submitProof(
        questId: Long,
        stepId: Long,
        fileUri: String?,
        existingStoragePath: String?,
        comment: String?,
        update: Boolean,
    ): QuestProofResult {
        val storagePath = if (fileUri != null) {
            when (val uploaded = upload(fileUri)) {
                is Upload.Done -> uploaded.publicUrl
                is Upload.Failed -> return failure(uploaded.error)
            }
        } else {
            existingStoragePath?.takeIf { update && it.isNotBlank() }
                ?: return failure(QuestProofError.NoFile)
        }

        val request = CompleteQuestStepRequest(
            storagePath = storagePath,
            comment = comment?.trim()?.takeIf { it.isNotEmpty() },
        )
        return try {
            if (update) {
                api.updateProof(questId, stepId, request)
            } else {
                api.completeStepWithProof(questId, stepId, request)
            }
            QuestProofResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            failure(QuestProofError.Action(e.toActivityActionError()))
        }
    }

    private suspend fun upload(fileUri: String): Upload {
        val token = tokenStorage.getAccessToken()
            ?: return Upload.Failed(QuestProofError.Action(ActivityActionError.Data(DataError.Unauthorized)))
        val userId = jwtSubject(token) ?: return Upload.Failed(QuestProofError.SessionMismatch)

        val file = when (val prepared = prepareFile(fileUri)) {
            is Prepared.File -> prepared
            is Prepared.Failed -> return Upload.Failed(prepared.error)
        }

        val path = "$userId/${newFileId()}.${file.type.extension}"
        return try {
            storageApi.upload(path, file.bytes, file.type.mimeType, token)
            Upload.Done(NetworkConfig.proofPublicUrl(path))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Upload.Failed(e.toUploadError())
        }
    }

    private suspend fun prepareFile(fileUri: String): Prepared {
        val bytes = fileSource.readBytes(fileUri, MAX_PROOF_BYTES + 1)
            ?: return Prepared.Failed(QuestProofError.FileUnreadable)
        val type = proofFileTypeOf(bytes) ?: return Prepared.Failed(QuestProofError.UnsupportedType)
        if (bytes.size <= MAX_PROOF_BYTES) return Prepared.File(bytes, type)
        if (!type.isImage) return Prepared.Failed(QuestProofError.FileTooLarge)

        val jpeg = compressor.compress(fileUri) ?: return Prepared.Failed(QuestProofError.FileUnreadable)
        if (jpeg.size > MAX_PROOF_BYTES) return Prepared.Failed(QuestProofError.FileTooLarge)
        return Prepared.File(jpeg, ProofFileType.Jpeg)
    }

    private fun failure(error: QuestProofError) = QuestProofResult.Failure(error)

    private sealed interface Upload {
        data class Done(val publicUrl: String) : Upload
        data class Failed(val error: QuestProofError) : Upload
    }

    private sealed interface Prepared {
        class File(val bytes: ByteArray, val type: ProofFileType) : Prepared
        data class Failed(val error: QuestProofError) : Prepared
    }
}

private fun Throwable.toUploadError(): QuestProofError {
    val status = (this as? ApiStatusException)?.statusCode
        ?: return QuestProofError.Action(ActivityActionError.Data(toDataError()))
    return when (status) {
        413 -> QuestProofError.FileTooLarge
        in 400..499 -> QuestProofError.UploadRejected
        else -> QuestProofError.Action(ActivityActionError.Data(toDataError()))
    }
}