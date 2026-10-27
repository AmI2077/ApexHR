package com.example.apexhr.features.profile.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.example.apexhr.features.profile.domain.AvatarImageCompressor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Уменьшает фото до [AVATAR_MAX_SIDE_PX] по длинной стороне и сжимает в JPEG.
 * ImageDecoder сам учитывает поворот из EXIF.
 */
class AndroidAvatarImageCompressor(
    private val context: Context,
) : AvatarImageCompressor {

    override suspend fun compress(imageUri: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(imageUri))
            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                // Программный bitmap: аппаратный нельзя сжать в JPEG.
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longSide = max(info.size.width, info.size.height)
                if (longSide > AVATAR_MAX_SIDE_PX) {
                    val scale = AVATAR_MAX_SIDE_PX.toFloat() / longSide
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt().coerceAtLeast(1),
                        (info.size.height * scale).roundToInt().coerceAtLeast(1),
                    )
                }
            }
            try {
                encode(bitmap)
            } finally {
                bitmap.recycle()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /** Снижает качество, пока файл не уложится в лимит бакета. */
    private fun encode(bitmap: Bitmap): ByteArray? {
        var quality = START_QUALITY
        while (true) {
            val bytes = ByteArrayOutputStream().use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) return null
                out.toByteArray()
            }
            if (bytes.size <= MAX_AVATAR_BYTES || quality <= MIN_QUALITY) return bytes
            quality -= QUALITY_STEP
        }
    }
}

const val AVATAR_MAX_SIDE_PX = 1024
private const val START_QUALITY = 85
private const val MIN_QUALITY = 40
private const val QUALITY_STEP = 15
