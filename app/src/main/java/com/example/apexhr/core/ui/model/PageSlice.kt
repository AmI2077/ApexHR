package com.example.apexhr.core.ui.model

/**
 * Одна страница уже загруженного списка — как usePage в вебе: списки, которые API
 * отдаёт целиком (заказы, каталог, благодарности), листаются кнопками «Назад» / «Дальше».
 * @param page текущая страница с 1, уже ограниченная диапазоном 1..[pages].
 * @param pages всего страниц, не меньше 1.
 * @param total элементов во всём списке.
 */
data class PageSlice<T>(
    val items: List<T>,
    val page: Int,
    val pages: Int,
    val total: Int,
) {
    val hasPrevious: Boolean get() = page > 1
    val hasNext: Boolean get() = page < pages

    /** Как Pager в вебе: при одной странице кнопок нет. */
    val isPagerVisible: Boolean get() = pages > 1
}

/**
 * Страница [page] по [size] элементов. Номер вне диапазона ограничивается, как в вебе:
 * если список стал короче, показывается последняя страница.
 */
fun <T> List<T>.pageSlice(page: Int, size: Int): PageSlice<T> {
    require(size > 0) { "size must be positive" }
    val pages = maxOf(1, (this.size + size - 1) / size)
    val current = page.coerceIn(1, pages)
    val start = (current - 1) * size
    return PageSlice(
        items = subList(start, minOf(start + size, this.size)),
        page = current,
        pages = pages,
        total = this.size,
    )
}
