package com.example.apexhr.features.profile.domain

import java.time.Instant

/**
 * Операция в полной истории баланса. amount > 0 — начисление, < 0 — списание.
 * @param createdAt null, если сервер не прислал дату или прислал её в неизвестном формате.
 */
data class HistoryTransaction(
    val id: Long,
    val amount: Long,
    val description: String?,
    val type: TransactionType,
    val createdAt: Instant?,
)

/** Вид операции; в API это relatedEntityType (строка), см. маппер. */
enum class TransactionType {
    Activity,
    Thanks,
    Bonus,
    Purchase,
    Refund,
    Adjustment,
    Other,
}

/**
 * Фильтр истории, как на вебе: все / активности / благодарности / поощрения /
 * покупки / корректировки. Фильтра по типу в API нет, поэтому фильтруем уже
 * загруженные операции.
 */
enum class TransactionFilter {
    All,
    Activities,
    Thanks,
    Bonuses,
    Purchases,
    Adjustments,
    ;

    /** Операции вида [TransactionType.Other] видны только во «Все». */
    fun matches(type: TransactionType): Boolean = when (this) {
        All -> true
        Activities -> type == TransactionType.Activity
        Thanks -> type == TransactionType.Thanks
        Bonuses -> type == TransactionType.Bonus
        Purchases -> type == TransactionType.Purchase || type == TransactionType.Refund
        Adjustments -> type == TransactionType.Adjustment
    }
}

/**
 * Страница истории.
 * @param hasMore сервер вернул полную страницу — за ней могут быть ещё операции.
 */
data class TransactionPage(
    val transactions: List<HistoryTransaction>,
    val hasMore: Boolean,
)
