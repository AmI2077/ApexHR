package com.example.apexhr.features.thanks.domain

import com.example.apexhr.core.domain.DataResult
import kotlinx.coroutines.flow.Flow

interface ThanksRepository {
    /** Активные сотрудники, кроме текущего пользователя: кого можно поблагодарить. */
    suspend fun loadColleagues(): DataResult<List<Colleague>>

    /** Полученные и отправленные благодарности; имена — из списка сотрудников. */
    suspend fun loadHistory(): DataResult<ThanksHistory>

    /**
     * Отправляет благодарность без трат, как в вебе: сумма всегда
     * [PEER_THANKS_AMOUNT], благодарность публичная.
     */
    suspend fun sendThanks(toUserId: String, message: String): ThanksSendResult

    /**
     * Может ли текущий пользователь поощрять с рабочего баланса и сколько на нём.
     * Рабочий баланс запрашивается только у руководителя.
     */
    suspend fun loadManagerBonusAccess(): DataResult<ManagerBonusAccess>

    /**
     * Поощрение руководителя: POST /api/thanks/manager-bonus, сумма списывается
     * с рабочего баланса. Пустое сообщение не отправляется.
     */
    suspend fun sendManagerBonus(toUserId: String, amount: Long, message: String): ThanksSendResult

    /** Срабатывает после каждой успешной отправки, чтобы история обновилась. */
    val thanksSent: Flow<Unit>
}
