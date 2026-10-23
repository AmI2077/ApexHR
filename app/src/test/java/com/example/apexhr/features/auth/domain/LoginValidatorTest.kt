package com.example.apexhr.features.auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidatorTest {

    @Test
    fun `valid email and password pass`() {
        val result = LoginValidator.validate("user@example.com", "secret")

        assertTrue(result.isValid)
    }

    @Test
    fun `email with surrounding spaces is valid`() {
        assertNull(LoginValidator.validateEmail("  user.name+tag@mail.example.ru "))
    }

    @Test
    fun `empty or blank email is reported as empty`() {
        assertEquals(EmailError.Empty, LoginValidator.validateEmail(""))
        assertEquals(EmailError.Empty, LoginValidator.validateEmail("   "))
    }

    @Test
    fun `malformed email is reported as invalid format`() {
        listOf(
            "user",
            "user@",
            "@example.com",
            "user@example",
            "user example@mail.com",
            "user@@example.com",
            "user@example.c",
        ).forEach { email ->
            assertEquals(email, EmailError.InvalidFormat, LoginValidator.validateEmail(email))
        }
    }

    @Test
    fun `empty password is reported`() {
        assertEquals(PasswordError.Empty, LoginValidator.validatePassword(""))
    }

    @Test
    fun `password with spaces is not trimmed`() {
        assertNull(LoginValidator.validatePassword("   "))
    }

    @Test
    fun `both errors are reported together`() {
        val result = LoginValidator.validate("", "")

        assertFalse(result.isValid)
        assertEquals(EmailError.Empty, result.emailError)
        assertEquals(PasswordError.Empty, result.passwordError)
    }
}
