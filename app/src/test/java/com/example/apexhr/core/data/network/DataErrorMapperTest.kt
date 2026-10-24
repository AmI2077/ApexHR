package com.example.apexhr.core.data.network

import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.domain.DataResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class DataErrorMapperTest {

    @Test
    fun `401 maps to unauthorized`() {
        assertEquals(DataError.Unauthorized, ApiStatusException(401).toDataError())
    }

    @Test
    fun `5xx maps to server error`() {
        assertEquals(DataError.Server, ApiStatusException(500).toDataError())
        assertEquals(DataError.Server, ApiStatusException(503).toDataError())
    }

    @Test
    fun `408 maps to timeout`() {
        assertEquals(DataError.Timeout, ApiStatusException(408).toDataError())
    }

    @Test
    fun `other status codes map to unknown`() {
        assertEquals(DataError.Unknown, ApiStatusException(404).toDataError())
    }

    @Test
    fun `socket timeout maps to timeout, not network`() {
        assertEquals(DataError.Timeout, SocketTimeoutException().toDataError())
    }

    @Test
    fun `io errors map to network`() {
        assertEquals(DataError.Network, UnknownHostException("host").toDataError())
        assertEquals(DataError.Network, IOException("io").toDataError())
    }

    @Test
    fun `wrapped cause is unwrapped`() {
        val wrapped = IllegalStateException("wrapper", SocketTimeoutException())
        assertEquals(DataError.Timeout, wrapped.toDataError())
    }

    @Test
    fun `unexpected exception maps to unknown`() {
        assertEquals(DataError.Unknown, IllegalArgumentException("bad json").toDataError())
    }

    @Test
    fun `safeApiCall wraps value and error`() = runBlocking {
        assertEquals(DataResult.Success(42), safeApiCall { 42 })
        assertEquals(
            DataResult.Failure(DataError.Server),
            safeApiCall<Int> { throw ApiStatusException(502) },
        )
    }

    @Test(expected = CancellationException::class)
    fun `safeApiCall rethrows cancellation`() {
        runBlocking {
            safeApiCall<Int> { throw CancellationException("cancelled") }
        }
    }
}
