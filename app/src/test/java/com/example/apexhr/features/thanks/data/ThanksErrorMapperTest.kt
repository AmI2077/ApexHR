package com.example.apexhr.features.thanks.data

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.thanks.domain.ThanksSendError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ThanksErrorMapperTest {

    private fun error(code: Int, message: String? = null) =
        ThanksApiException(code, message).toThanksSendError()

    @Test
    fun `server message is read from message detail or title`() {
        assertEquals("Недельный лимит исчерпан", parseThanksServerMessage("""{"message":"Недельный лимит исчерпан"}"""))
        assertEquals("Подробно", parseThanksServerMessage("""{"title":"Кратко","detail":"Подробно"}"""))
        assertEquals("Кратко", parseThanksServerMessage("""{"title":"Кратко","detail":"  "}"""))
    }

    @Test
    fun `non json body has no message`() {
        assertNull(parseThanksServerMessage(""))
        assertNull(parseThanksServerMessage("<html>Bad gateway</html>"))
        assertNull(parseThanksServerMessage("""["a"]"""))
        assertNull(parseThanksServerMessage("""{"message":42}"""))
    }

    @Test
    fun `weekly limit keeps server message`() {
        assertEquals(
            ThanksSendError.Rejected("Weekly limit exceeded"),
            error(400, "Weekly limit exceeded"),
        )
        assertEquals(ThanksSendError.Rejected(null), error(400))
        assertEquals(ThanksSendError.Rejected("Conflict"), error(409, "Conflict"))
    }

    @Test
    fun `not found and forbidden are mapped by code`() {
        assertEquals(ThanksSendError.NotFound, error(404, "User was not found."))
        assertEquals(ThanksSendError.Forbidden, error(403))
    }

    @Test
    fun `transport errors are data errors`() {
        assertEquals(ThanksSendError.Data(DataError.Unauthorized), error(401))
        assertEquals(ThanksSendError.Data(DataError.Server), error(500, "Internal error"))
        assertEquals(ThanksSendError.Data(DataError.Timeout), error(408))
        assertEquals(ThanksSendError.Data(DataError.Network), UnknownHostException().toThanksSendError())
        assertEquals(ThanksSendError.Data(DataError.Timeout), SocketTimeoutException().toThanksSendError())
    }
}
