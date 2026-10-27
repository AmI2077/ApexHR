package com.example.apexhr.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/*
 * Фирменные цвета берутся из MaterialTheme.colorScheme, поэтому сами
 * переключаются вместе со светлой/тёмной темой. Значения светлой схемы
 * совпадают с прежними константами, так что светлая тема выглядит как раньше.
 * Экраны должны быть обёрнуты в ApexHRTheme(dynamicColor = false).
 */

val darkBlueColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer
val primaryBlueColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val secondaryBlueColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
/** Светлый фон невыбранных вкладок-«таблеток» (ApexPillTabs). */
val pillBackgroundColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val backgroundColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val transparent = Color.Transparent

val primaryTextColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onBackground
val secondaryTextColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

val negativeRedColor: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.error

/** Светлая схема «Апекса»: прежние фирменные цвета. */
val ApexLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF306CC0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7D1F2),
    onPrimaryContainer = Color(0xFF1E4A8A),
    background = Color.White,
    onBackground = Color(0xFF14171A),
    surface = Color.White,
    onSurface = Color(0xFF14171A),
    surfaceVariant = Color(0xFFF3F7FC),
    onSurfaceVariant = Color(0xFF666B6E),
    error = Color(0xFFCC1A29),
)

/**
 * Тёмная схема «Апекса» на цветах веба: почти чёрный фон (#09090B),
 * карточки #171717, светлый текст и посветлевший фирменный синий.
 */
val ApexDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF5A8DD6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E4A8A),
    onPrimaryContainer = Color(0xFFD6E6F8),
    secondary = Color(0xFF2A63B6),
    background = Color(0xFF09090B),
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF09090B),
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF171717),
    onSurfaceVariant = Color(0xFFA1A1AA),
    surfaceContainerLowest = Color(0xFF171717),
    surfaceContainerLow = Color(0xFF171717),
    surfaceContainer = Color(0xFF171717),
    surfaceContainerHigh = Color(0xFF171717),
    surfaceContainerHighest = Color(0xFF171717),
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF404040),
    error = Color(0xFFF2777F),
    onError = Color(0xFF3B0A0F),
)