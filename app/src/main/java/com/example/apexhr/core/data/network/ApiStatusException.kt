package com.example.apexhr.core.data.network

/**
 * Сервер ответил кодом, отличным от 2xx.
 */
class ApiStatusException(
    val statusCode: Int,
) : Exception("HTTP $statusCode")
