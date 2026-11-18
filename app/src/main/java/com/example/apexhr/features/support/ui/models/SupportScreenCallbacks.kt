package com.example.apexhr.features.support.ui.models

interface SupportListScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()
    fun onLoadMoreClick()

    /** Переход к обращению; задаёт навигация. */
    fun onTicketClick(id: Long) = Unit

    /** Переход к созданию обращения; задаёт навигация. */
    fun onNewTicketClick() = Unit

    companion object {
        val Empty = object : SupportListScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onLoadMoreClick() = Unit
        }
    }
}

interface SupportTicketScreenCallbacks {
    fun onBackClick()
    fun onRetryClick()
    fun onReplyChange(text: String)
    fun onSendClick()

    companion object {
        val Empty = object : SupportTicketScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onRetryClick() = Unit
            override fun onReplyChange(text: String) = Unit
            override fun onSendClick() = Unit
        }
    }
}

interface SupportNewTicketScreenCallbacks {
    fun onBackClick()
    fun onSubjectChange(text: String)
    fun onMessageChange(text: String)
    fun onSendClick()

    companion object {
        val Empty = object : SupportNewTicketScreenCallbacks {
            override fun onBackClick() = Unit
            override fun onSubjectChange(text: String) = Unit
            override fun onMessageChange(text: String) = Unit
            override fun onSendClick() = Unit
        }
    }
}