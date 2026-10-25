package com.example.apexhr.features.activities.data

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.activities.domain.ActivityActionError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ActivitiesErrorMapperTest {

    private fun error(code: Int, message: String? = null) =
        ActivitiesApiException(code, message).toActivityActionError()

    @Test
    fun `server message is read from message detail or title`() {
        assertEquals("Quest was not found.", parseActivityServerMessage("""{"message":"Quest was not found."}"""))
        assertEquals("Подробно", parseActivityServerMessage("""{"title":"Кратко","detail":"Подробно"}"""))
        assertEquals("Кратко", parseActivityServerMessage("""{"title":"Кратко","detail":"  "}"""))
    }

    @Test
    fun `non json body has no message`() {
        assertNull(parseActivityServerMessage(""))
        assertNull(parseActivityServerMessage("<html>Bad gateway</html>"))
        assertNull(parseActivityServerMessage("""["a"]"""))
        assertNull(parseActivityServerMessage("""{"message":42}"""))
    }

    @Test
    fun `business refusals keep server message`() {
        assertEquals(ActivityActionError.Rejected("Event is full."), error(409, "Event is full."))
        assertEquals(ActivityActionError.Rejected("Answer is required."), error(400, "Answer is required."))
        assertEquals(ActivityActionError.Rejected(null), error(409))
    }

    @Test
    fun `not found and forbidden are mapped by code`() {
        assertEquals(ActivityActionError.NotFound, error(404, "Survey was not found."))
        assertEquals(ActivityActionError.Forbidden, error(403))
    }

    @Test
    fun `transport errors are data errors`() {
        assertEquals(ActivityActionError.Data(DataError.Unauthorized), error(401))
        assertEquals(ActivityActionError.Data(DataError.Server), error(503, "Service unavailable"))
        assertEquals(ActivityActionError.Data(DataError.Timeout), error(408))
        assertEquals(ActivityActionError.Data(DataError.Network), UnknownHostException().toActivityActionError())
        assertEquals(ActivityActionError.Data(DataError.Timeout), SocketTimeoutException().toActivityActionError())
    }
}
