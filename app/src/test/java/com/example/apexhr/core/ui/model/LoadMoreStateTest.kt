package com.example.apexhr.core.ui.model

import com.example.apexhr.core.domain.DataError
import org.junit.Assert.assertEquals
import org.junit.Test

class LoadMoreStateTest {

    @Test
    fun `footer state follows paging flags`() {
        assertEquals(LoadMoreState.Available, loadMoreStateOf(hasMore = true, isLoading = false, error = null))
        assertEquals(LoadMoreState.Hidden, loadMoreStateOf(hasMore = false, isLoading = false, error = null))
        assertEquals(LoadMoreState.Loading, loadMoreStateOf(hasMore = true, isLoading = true, error = DataError.Server))
        assertEquals(
            LoadMoreState.Error(DataError.Timeout),
            loadMoreStateOf(hasMore = true, isLoading = false, error = DataError.Timeout),
        )
        // Всё загружено — старая ошибка не показывается.
        assertEquals(LoadMoreState.Hidden, loadMoreStateOf(hasMore = false, isLoading = false, error = DataError.Server))
    }
}
