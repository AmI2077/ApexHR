package com.example.apexhr.core.ui.model

import androidx.annotation.StringRes
import com.example.apexhr.R
import kotlin.math.abs

/** Падеж слова «апекс» рядом с числом. */
enum class ApexCase {
    /** Именительный (и совпадающий с ним винительный): «Приз: 21 апекс», «Оформить за 5 апексов». */
    Nominative,

    /** Родительный: «Не хватает 1 апекса», «из 21 апекса», «до 5 апексов». */
    Genitive,
}

/** Форма слова после числа по правилам русского языка. */
enum class RussianPluralForm {
    /** 1, 21, 101… — «апекс». */
    One,

    /** 2–4, 22–24… — «апекса». */
    Few,

    /** 0, 5–20, 25–30, 111… — «апексов». */
    Many,
}

/**
 * Форма слова для целого числа; знак не учитывается (−21 -> One).
 *
 * Выбор сделан на Kotlin, а не через Android plurals: plurals берут правила из языка
 * устройства, и на английской системе «5» попало бы в other, а «21» — не в one.
 */
fun russianPluralForm(amount: Long): RussianPluralForm {
    // amount % 100 не переполняется даже для Long.MIN_VALUE, в отличие от abs(amount).
    val lastTwo = abs(amount % 100).toInt()
    val last = lastTwo % 10
    return when {
        lastTwo in 11..14 -> RussianPluralForm.Many
        last == 1 -> RussianPluralForm.One
        last in 2..4 -> RussianPluralForm.Few
        else -> RussianPluralForm.Many
    }
}

/** Строка «%1$s апекс…» в нужной форме и падеже; %1$s — число после [formatAmount]. */
@StringRes
fun apexAmountRes(amount: Long, case: ApexCase = ApexCase.Nominative): Int {
    val form = russianPluralForm(amount)
    return when (case) {
        ApexCase.Nominative -> when (form) {
            RussianPluralForm.One -> R.string.apex_amount_one
            RussianPluralForm.Few -> R.string.apex_amount_few
            RussianPluralForm.Many -> R.string.apex_amount_many
        }
        // В родительном 2–4 и 5+ совпадают: «2 апексов», «5 апексов».
        ApexCase.Genitive -> when (form) {
            RussianPluralForm.One -> R.string.apex_amount_genitive_one
            RussianPluralForm.Few, RussianPluralForm.Many -> R.string.apex_amount_genitive_many
        }
    }
}

/** Только слово «апекс» в нужной форме и падеже, без числа. */
@StringRes
fun apexWordRes(amount: Long, case: ApexCase = ApexCase.Nominative): Int {
    val form = russianPluralForm(amount)
    return when (case) {
        ApexCase.Nominative -> when (form) {
            RussianPluralForm.One -> R.string.apex_word_one
            RussianPluralForm.Few -> R.string.apex_word_few
            RussianPluralForm.Many -> R.string.apex_word_many
        }
        ApexCase.Genitive -> when (form) {
            RussianPluralForm.One -> R.string.apex_word_genitive_one
            RussianPluralForm.Few, RussianPluralForm.Many -> R.string.apex_word_genitive_many
        }
    }
}