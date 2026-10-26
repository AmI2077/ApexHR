package com.example.apexhr.features.auth.data

import com.example.apexhr.core.data.network.ApiMessageException
import com.example.apexhr.features.auth.domain.AuthError
import com.example.apexhr.features.auth.domain.ChangePasswordError
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ChangePasswordErrorMapperTest {

    @Test
    fun `400 keeps server message`() {
        assertEquals(
            ChangePasswordError.Rejected("Слишком короткий пароль"),
            ApiMessageException(400, "Слишком короткий пароль").toChangePasswordError()
        )
    }

    @Test
    fun `other 4xx without message is rejected without text`() {
        assertEquals(
            ChangePasswordError.Rejected(null),
            ApiMessageException(403, null).toChangePasswordError()
        )
    }

    @Test
    fun `401 means session expired`() {
        assertEquals(
            ChangePasswordError.SessionExpired,
            ApiMessageException(401, "Unauthorized").toChangePasswordError()
        )
    }

    @Test
    fun `408 and 5xx are auth errors`() {
        assertEquals(
            ChangePasswordError.Auth(AuthError.Timeout),
            ApiMessageException(408, null).toChangePasswordError()
        )
        assertEquals(
            ChangePasswordError.Auth(AuthError.Server),
            ApiMessageException(503, "oops").toChangePasswordError()
        )
    }

    @Test
    fun `network and timeout exceptions are auth errors`() {
        assertEquals(
            ChangePasswordError.Auth(AuthError.Network),
            UnknownHostException("no network").toChangePasswordError()
        )
        assertEquals(
            ChangePasswordError.Auth(AuthError.Timeout),
            SocketTimeoutException("slow").toChangePasswordError()
        )
    }
}