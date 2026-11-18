package com.example.apexhr.features.activities.data

import android.content.Context
import android.net.Uri
import com.example.apexhr.features.activities.domain.ProofFileSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Читает выбранный файл через ContentResolver, не больше limit байт. */
class AndroidProofFileSource(
    private val context: Context,
) : ProofFileSource {

    override suspend fun readBytes(uri: String, limit: Int): ByteArray? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(BUFFER_SIZE)
                while (out.size() < limit) {
                    val read = input.read(buffer, 0, minOf(buffer.size, limit - out.size()))
                    if (read < 0) break
                    out.write(buffer, 0, read)
                }
                out.toByteArray()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val BUFFER_SIZE = 64 * 1024
    }
}