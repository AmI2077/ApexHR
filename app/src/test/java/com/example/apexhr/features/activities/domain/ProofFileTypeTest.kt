package com.example.apexhr.features.activities.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProofFileTypeTest {

    @Test
    fun `types are detected by content`() {
        assertEquals(ProofFileType.Jpeg, proofFileTypeOf(jpeg()))
        assertEquals(ProofFileType.Png, proofFileTypeOf(png()))
        assertEquals(ProofFileType.Webp, proofFileTypeOf("RIFF\u0000\u0000\u0000\u0000WEBPVP8 ".latin1()))
        assertEquals(ProofFileType.Pdf, proofFileTypeOf("%PDF-1.7\n".latin1()))
    }

    @Test
    fun `other files are rejected`() {
        assertNull(proofFileTypeOf("GIF89a......".latin1()))
        assertNull(proofFileTypeOf("<html>".latin1()))
        assertNull(proofFileTypeOf(ByteArray(0)))
        // Подпись PNG без IHDR — не PNG.
        assertNull(proofFileTypeOf(png().also { it[12] = 'X'.code.toByte() }))
    }

    @Test
    fun `jpeg is uploaded with jpg extension like in web`() {
        assertEquals("jpg", ProofFileType.Jpeg.extension)
        assertEquals("application/pdf", ProofFileType.Pdf.mimeType)
    }
}

fun String.latin1(): ByteArray = toByteArray(Charsets.ISO_8859_1)

fun jpeg(size: Int = 16): ByteArray = ByteArray(size).also {
    it[0] = 0xFF.toByte()
    it[1] = 0xD8.toByte()
    it[2] = 0xFF.toByte()
}

fun png(size: Int = 32): ByteArray = ByteArray(size).also {
    byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A).copyInto(it)
    "IHDR".latin1().copyInto(it, destinationOffset = 12)
}