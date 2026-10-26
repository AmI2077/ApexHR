package com.example.apexhr.core.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerErrorMessageTest {

    @Test
    fun `message field is returned trimmed`() {
        assertEquals(
            "Пароль слишком простой",
            parseServerErrorMessage("""{"message":"  Пароль слишком простой "}""")
        )
    }

    @Test
    fun `problem details detail is used when there is no message`() {
        assertEquals(
            "Bad password",
            parseServerErrorMessage("""{"title":"Bad Request","detail":"Bad password","status":400}""")
        )
    }

    @Test
    fun `title is used as last resort`() {
        assertEquals("Bad Request", parseServerErrorMessage("""{"title":"Bad Request"}"""))
    }

    @Test
    fun `blank or non string message is ignored`() {
        assertNull(parseServerErrorMessage("""{"message":"  "}"""))
        assertNull(parseServerErrorMessage("""{"message":42}"""))
    }

    @Test
    fun `non json body is ignored`() {
        assertNull(parseServerErrorMessage("<html>502 Bad Gateway</html>"))
        assertNull(parseServerErrorMessage(""))
        assertNull(parseServerErrorMessage("""["message"]"""))
    }
}