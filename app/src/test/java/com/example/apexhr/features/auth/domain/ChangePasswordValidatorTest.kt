package com.example.apexhr.features.auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangePasswordValidatorTest {

    @Test
    fun `matching passwords of minimal length are valid`() {
        val result = ChangePasswordValidator.validate("secret", "secret")

        assertTrue(result.isValid)
        assertNull(result.newPasswordError)
        assertNull(result.confirmPasswordError)
    }

    @Test
    fun `empty fields report both errors`() {
        val result = ChangePasswordValidator.validate("", "")

        assertFalse(result.isValid)
        assertEquals(NewPasswordError.Empty, result.newPasswordError)
        assertEquals(ConfirmPasswordError.Empty, result.confirmPasswordError)
    }

    @Test
    fun `password of spaces only is empty`() {
        val result = ChangePasswordValidator.validate("      ", "      ")

        assertEquals(NewPasswordError.Empty, result.newPasswordError)
    }

    @Test
    fun `password shorter than minimum is too short`() {
        val result = ChangePasswordValidator.validate("12345", "12345")

        assertEquals(NewPasswordError.TooShort, result.newPasswordError)
        assertNull(result.confirmPasswordError)
    }

    @Test
    fun `different confirmation is mismatch`() {
        val result = ChangePasswordValidator.validate("secret1", "secret2")

        assertNull(result.newPasswordError)
        assertEquals(ConfirmPasswordError.Mismatch, result.confirmPasswordError)
    }

    @Test
    fun `confirmation is compared without trimming`() {
        val result = ChangePasswordValidator.validate("secret ", "secret")

        assertEquals(ConfirmPasswordError.Mismatch, result.confirmPasswordError)
    }
}