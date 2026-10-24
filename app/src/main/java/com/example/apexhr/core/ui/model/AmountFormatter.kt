package com.example.apexhr.core.ui.model

import kotlin.math.abs

private const val GROUP_SEPARATOR = '\u00A0'

/** Форматирует сумму с разделением разрядов неразрывным пробелом: 12500 -> «12 500». */
fun formatAmount(amount: Long): String {
    val digits = abs(amount).toString()
    val grouped = digits
        .reversed()
        .chunked(3)
        .joinToString(GROUP_SEPARATOR.toString())
        .reversed()
    return if (amount < 0) "-$grouped" else grouped
}
