package com.example.apexhr.features.profile.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class JwtClaimsTest {

    @Test
    fun `subject is read from payload`() {
        assertEquals("user-1", jwtSubject(testJwt("""{"sub":"user-1","role":"authenticated"}""")))
    }

    @Test
    fun `token without subject or not a jwt gives null`() {
        assertNull(jwtSubject(testJwt("""{"role":"anon"}""")))
        assertNull(jwtSubject("not-a-jwt"))
        assertNull(jwtSubject("a.%%%.c"))
    }
}

/** JWT без настоящей подписи: приложение подпись не проверяет. */
fun testJwt(payloadJson: String): String {
    val encoder = Base64.getUrlEncoder().withoutPadding()
    val header = encoder.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
    val payload = encoder.encodeToString(payloadJson.toByteArray())
    return "$header.$payload.signature"
}
