package com.example.apexhr.features.activities.ui.models

interface ActivitiesScreenCallbacks {
    /** Экран показан (вход на вкладку или возврат с деталей): список перечитывается. */
    fun onScreenShown()
    fun onSearchQueryChange(query: String)
    fun onCategoryClick(categoryId: String)
    fun onActivityClick(activity: ActivityUiModel)
    fun onParticipateClick(activity: ActivityUiModel)
    fun onRetryClick()

    /** Список докручен до конца или нажато «Повторить» в футере: следующая страница. */
    fun onLoadMore()

    companion object {
        val Empty = object : ActivitiesScreenCallbacks {
            override fun onScreenShown() = Unit
            override fun onSearchQueryChange(query: String) = Unit
            override fun onCategoryClick(categoryId: String) = Unit
            override fun onActivityClick(activity: ActivityUiModel) = Unit
            override fun onParticipateClick(activity: ActivityUiModel) = Unit
            override fun onRetryClick() = Unit
            override fun onLoadMore() = Unit
        }
    }
}
