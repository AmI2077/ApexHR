package com.example.apexhr.features.activities.ui.common

import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.features.activities.domain.ActivityActionError
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityMessageTest {

    @Test
    fun `rejected with server text shows it`() {
        assertEquals(
            ActivityMessage(R.string.activities_error_server_message, listOf("Event is full")),
            ActivityActionError.Rejected("Event is full").toActivityMessage(),
        )
    }

    @Test
    fun `rejected without text shows generic action error`() {
        listOf(null, "", "  ").forEach { text ->
            assertEquals(
                ActivityMessage(R.string.activities_error_action),
                ActivityActionError.Rejected(text).toActivityMessage(),
            )
        }
    }

    @Test
    fun `not found and forbidden have own texts`() {
        assertEquals(
            ActivityMessage(R.string.activities_error_not_found),
            ActivityActionError.NotFound.toActivityMessage(),
        )
        assertEquals(
            ActivityMessage(R.string.activities_error_forbidden),
            ActivityActionError.Forbidden.toActivityMessage(),
        )
    }

    @Test
    fun `data errors use common texts except unknown`() {
        assertEquals(
            ActivityMessage(R.string.data_error_network),
            ActivityActionError.Data(DataError.Network).toActivityMessage(),
        )
        assertEquals(
            ActivityMessage(R.string.auth_session_expired),
            ActivityActionError.Data(DataError.Unauthorized).toActivityMessage(),
        )
        assertEquals(
            ActivityMessage(R.string.activities_error_action),
            ActivityActionError.Data(DataError.Unknown).toActivityMessage(),
        )
    }
}
