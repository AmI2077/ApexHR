package com.example.apexhr.core.ui.model

/**
 * Возвращает строку, только если это http(s)-ссылка. Сервер иногда кладёт
 * в поля картинок произвольный текст — такие значения грузить бессмысленно.
 */
fun String?.takeIfHttpUrl(): String? = this?.takeIf {
    it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true)
}
