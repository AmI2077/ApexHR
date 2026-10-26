package com.example.apexhr.core.ui.model

import kotlin.math.abs

private const val GROUP_SEPARATOR = '\u00A0'
private const val MINUS_SIGN = '\u2212'

/** Форматирует сумму с разделением разрядов неразрывным пробелом: 12500 -> «12 500». */
fun formatAmount(amount: Long): String {
    val grouped = groupDigits(amount)
    return if (amount < 0) "-$grouped" else grouped
}

/**
 * Сумма операции со знаком: 150 -> «+150», -1250 -> «−1 250» (типографский минус U+2212), 0 -> «0».
 * Для истории начислений и списаний.
 */
fun formatSignedAmount(amount: Long): String {
    val grouped = groupDigits(amount)
    return when {
        amount > 0 -> "+$grouped"
        amount < 0 -> "$MINUS_SIGN$grouped"
        else -> grouped
    }
}

/**
 * Количество для выбора формы слова «апекс» через Android plurals.
 *
 * Plurals принимают Int, а суммы у нас Long. Для русского языка форма зависит только от двух
 * последних цифр модуля числа (1 — «апекс», 2–4 — «апекса», 0, 5–20 — «апексов»),
 * поэтому достаточно передать их — так не будет переполнения при больших суммах.
 */
fun apexPluralQuantity(amount: Long): Int = abs(amount % 100).toInt()

private fun groupDigits(amount: Long): String {
    // abs(Long.MIN_VALUE) отрицателен, поэтому берём цифры из строки без знака.
    val digits = amount.toString().removePrefix("-")
    return digits
        .reversed()
        .chunked(3)
        .joinToString(GROUP_SEPARATOR.toString())
        .reversed()
}