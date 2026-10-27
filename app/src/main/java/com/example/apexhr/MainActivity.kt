package com.example.apexhr

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apexhr.core.ui.navigation.AppRoot
import com.example.apexhr.features.settings.domain.SettingsRepository
import com.example.apexhr.features.settings.domain.resolveDarkTheme
import com.example.apexhr.ui.theme.ApexHRTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Пока настройка не прочитана или не выбрана — тема как в системе.
            val savedDarkTheme by settingsRepository.darkTheme
                .collectAsStateWithLifecycle(initialValue = null)
            val darkTheme = resolveDarkTheme(savedDarkTheme, isSystemInDarkTheme())

            // Значки системных панелей — под тему приложения, а не системы.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        LIGHT_NAVIGATION_SCRIM,
                        DARK_NAVIGATION_SCRIM,
                    ) { darkTheme },
                )
                onDispose { }
            }

            // dynamicColor = false: фирменные цвета «Апекса» вместо цветов обоев.
            ApexHRTheme(darkTheme = darkTheme, dynamicColor = false) {
                AppRoot()
            }
        }
    }

    private companion object {
        // Те же полупрозрачные подложки навигационной панели, что у enableEdgeToEdge() по умолчанию.
        val LIGHT_NAVIGATION_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_NAVIGATION_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
