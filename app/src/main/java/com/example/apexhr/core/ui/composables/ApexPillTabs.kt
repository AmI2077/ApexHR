package com.example.apexhr.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.ui.theme.ApexHRTheme

/**
 * Горизонтально прокручиваемый ряд вкладок-«таблеток» вместо текстовых вкладок.
 *
 * Выбранная — залита фирменным синим с белым жирным текстом, остальные — светлый фон
 * и тёмный текст, без обводки; расстояние между таблетками 8dp.
 *
 * @param labels подписи вкладок по порядку.
 * @param selectedIndex индекс выбранной вкладки; если он вне диапазона, не выбрана ни одна.
 * @param onSelect вызывается с индексом нажатой вкладки (в том числе уже выбранной —
 * повторное нажатие при необходимости отсекает вызывающий код).
 * @param contentPadding отступы ряда; например PaddingValues(horizontal = 20.dp), чтобы
 * первая таблетка стояла по сетке экрана, а прокрутка шла до края.
 * @param enabled false — вкладки не нажимаются (например, во время загрузки).
 */
@Composable
fun ApexPillTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    enabled: Boolean = true,
) {
    // Обычный remember: по задумке экраны не переживают смену конфигурации.
    val listState = remember { LazyListState() }
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        state = listState,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(labels) { index, label ->
            ApexPillTab(
                text = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                enabled = enabled,
            )
        }
    }
}

/** Одна вкладка-«таблетка»; для своих рядов, если не подходит [ApexPillTabs]. */
@Composable
fun ApexPillTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) primaryBlueColor else pillBackgroundColor)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Tab,
                onClick = onClick,
            )
            .heightIn(min = ApexDimens.pillTabHeight.dp)
            .padding(horizontal = ApexDimens.spacing16.dp, vertical = ApexDimens.spacing8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) backgroundColor else primaryTextColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ApexPillTabsPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        var selected by remember { mutableIntStateOf(0) }
        Column(verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp)) {
            ApexPillTabs(
                labels = listOf("Опросы", "Мини-игры", "Мероприятия"),
                selectedIndex = selected,
                onSelect = { selected = it },
                contentPadding = PaddingValues(horizontal = ApexDimens.spacing20.dp),
            )
            ApexPillTabs(
                labels = listOf("Все", "Мерч", "Сертификаты", "Опыт", "Благотворительность", "Обучение"),
                selectedIndex = 2,
                onSelect = {},
                contentPadding = PaddingValues(horizontal = ApexDimens.spacing20.dp),
            )
        }
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF121417, widthDp = 360)
@Composable
private fun ApexPillTabsDarkPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = true) {
        var selected by remember { mutableIntStateOf(0) }
        Column(verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp)) {
            ApexPillTabs(
                labels = listOf("Опросы", "Мини-игры", "Мероприятия"),
                selectedIndex = selected,
                onSelect = { selected = it },
                contentPadding = PaddingValues(horizontal = ApexDimens.spacing20.dp),
            )
            ApexPillTabs(
                labels = listOf("Все", "Мерч", "Сертификаты", "Опыт", "Благотворительность", "Обучение"),
                selectedIndex = 2,
                onSelect = {},
                contentPadding = PaddingValues(horizontal = ApexDimens.spacing20.dp),
            )
        }
    }
}