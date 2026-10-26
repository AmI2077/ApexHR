package com.example.apexhr.features.profile.ui.history.models

import com.example.apexhr.features.profile.domain.TransactionFilter

interface TransactionHistoryScreenCallbacks {
    fun onBackClick()
    fun onFilterClick(filter: TransactionFilter)
    fun onLoadMoreClick()
    fun onRetryClick()

    companion object {
        val Empty = object : TransactionHistoryScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onFilterClick(filter: TransactionFilter) = Unit
            override fun onLoadMoreClick() = Unit
            override fun onRetryClick() = Unit
        }
    }
}
