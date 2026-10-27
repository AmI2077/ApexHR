package com.example.apexhr.features.thanks.data

import com.example.apexhr.features.thanks.data.dto.CreateThankRequest
import com.example.apexhr.features.thanks.data.dto.ThankDto
import com.example.apexhr.features.thanks.data.dto.ThanksUserDto
import com.example.apexhr.features.thanks.domain.Colleague
import com.example.apexhr.features.thanks.domain.Thank
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ThanksMappersTest {

    // Те же настройки, что в networkModule.
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val anna = "5b850be7-abe7-4a45-91a6-53dceeed040f"
    private val me = "d5b02405-82d2-4607-80b6-b6e67d844349"
    private val names = mapOf(anna to "Анна Иванова", me to "Пётр Петров")

    @Test
    fun `users json is parsed and mapped`() {
        val users = json.decodeFromString<List<ThanksUserDto>>(
            """
            [{"id":"$anna","firstName":"Анна","lastName":"Иванова","middleName":null,"avatarUrl":null,
              "department":"Маркетинг","position":"Аналитик","isActive":true,
              "lastLoginAt":"2026-10-25T22:04:51.608615Z","roles":["employee"],"canAccessAdminPanel":false},
             {"id":"$me","firstName":" ","lastName":null,"department":"","position":"Employee","isActive":false}]
            """.trimIndent()
        )

        assertEquals(
            Colleague(id = anna, name = "Анна Иванова", position = "Аналитик", department = "Маркетинг"),
            users[0].toColleague(),
        )
        assertEquals(
            Colleague(id = me, name = null, position = "Employee", department = null),
            users[1].toColleague(),
        )
        assertFalse(users[1].isActive)
    }

    @Test
    fun `missing isActive means active`() {
        val user = json.decodeFromString<ThanksUserDto>("""{"id":"$anna"}""")

        assertEquals(true, user.isActive)
    }

    @Test
    fun `received thank gets sender name from users instead of id`() {
        val thank = json.decodeFromString<ThankDto>(
            """
            {"id":7,"fromUserId":"$anna","fromUserName":"$anna","toUserId":"$me","toUserName":"$me",
             "amount":50,"message":"Спасибо за помощь","isPublic":true,"createdAt":"2026-10-25T18:55:04.265575Z"}
            """.trimIndent()
        )

        assertEquals(
            Thank(
                id = 7,
                counterpartId = anna,
                counterpartName = "Анна Иванова",
                amount = 50,
                message = "Спасибо за помощь",
                createdAt = Instant.parse("2026-10-25T18:55:04.265575Z"),
            ),
            thank.toReceived(names),
        )
    }

    @Test
    fun `sent thank gets recipient name`() {
        val thank = ThankDto(id = 8, fromUserId = me, fromUserName = me, toUserId = anna, toUserName = anna, amount = 10)

        val sent = thank.toSent(names)

        assertEquals(anna, sent.counterpartId)
        assertEquals("Анна Иванова", sent.counterpartName)
    }

    @Test
    fun `unknown user with id instead of name has no name`() {
        val unknown = "11111111-2222-3333-4444-555555555555"

        assertNull(resolveName(unknown, unknown, names))
        assertNull(resolveName(unknown, "AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE", names))
        assertNull(resolveName(unknown, null, names))
        assertNull(resolveName(null, " ", names))
    }

    @Test
    fun `unknown user with real name in thank keeps it`() {
        assertEquals(
            "Мария Смирнова",
            resolveName("11111111-2222-3333-4444-555555555555", "Мария Смирнова", names),
        )
    }

    @Test
    fun `user id is matched ignoring case`() {
        assertEquals("Анна Иванова", resolveName(anna.uppercase(), anna.uppercase(), names))
    }

    @Test
    fun `blank message and broken date become null`() {
        val thank = ThankDto(id = 1, fromUserId = anna, message = "  ", createdAt = "вчера").toReceived(names)

        assertNull(thank.message)
        assertNull(thank.createdAt)
    }

    @Test
    fun `send request is serialized as in swagger`() {
        val body = json.encodeToJsonElement(
            CreateThankRequest.serializer(),
            CreateThankRequest(toUserId = anna, amount = 1, message = "Спасибо", isPublic = true),
        ).jsonObject

        assertEquals(anna, body["toUserId"]?.jsonPrimitive?.content)
        assertEquals("1", body["amount"]?.jsonPrimitive?.content)
        assertEquals("Спасибо", body["message"]?.jsonPrimitive?.content)
        assertEquals("true", body["isPublic"]?.jsonPrimitive?.content)
    }
}
