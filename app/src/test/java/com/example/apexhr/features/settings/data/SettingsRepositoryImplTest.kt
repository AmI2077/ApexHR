package com.example.apexhr.features.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.example.apexhr.features.settings.domain.ThemeChoice
import com.example.apexhr.features.settings.domain.toSavedDarkTheme
import com.example.apexhr.features.settings.domain.toThemeChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsRepositoryImplTest {

    /** DataStore в памяти: хранит Preferences так же, как настоящий, но без файла. */
    private class InMemoryPreferencesDataStore : DataStore<Preferences> {
        private val state = MutableStateFlow(emptyPreferences())
        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }

    @Test
    fun `dark theme is null until user chooses`() = runBlocking {
        val repository = SettingsRepositoryImpl(InMemoryPreferencesDataStore())

        assertNull(repository.darkTheme.first())
    }

    @Test
    fun `saved dark theme is read back`() = runBlocking {
        val repository = SettingsRepositoryImpl(InMemoryPreferencesDataStore())

        repository.setDarkTheme(true)

        assertEquals(true, repository.darkTheme.first())
    }

    @Test
    fun `dark theme can be turned off again`() = runBlocking {
        val repository = SettingsRepositoryImpl(InMemoryPreferencesDataStore())

        repository.setDarkTheme(true)
        repository.setDarkTheme(false)

        assertEquals(false, repository.darkTheme.first())
    }

    @Test
    fun `system choice clears saved theme`() = runBlocking {
        val repository = SettingsRepositoryImpl(InMemoryPreferencesDataStore())

        repository.setDarkTheme(true)
        repository.setDarkTheme(null)

        assertNull(repository.darkTheme.first())
    }

    @Test
    fun `theme choice maps to saved value and back`() {
        ThemeChoice.entries.forEach { choice ->
            assertEquals(choice, choice.toSavedDarkTheme().toThemeChoice())
        }
        assertEquals(ThemeChoice.System, null.toThemeChoice())
        assertNull(ThemeChoice.System.toSavedDarkTheme())
        assertEquals(true, ThemeChoice.Dark.toSavedDarkTheme())
        assertEquals(false, ThemeChoice.Light.toSavedDarkTheme())
    }

    @Test
    fun `choice survives new repository over same store`() = runBlocking {
        // Как после перезапуска: новый репозиторий, тот же файл настроек.
        val store = InMemoryPreferencesDataStore()
        SettingsRepositoryImpl(store).setDarkTheme(true)

        assertEquals(true, SettingsRepositoryImpl(store).darkTheme.first())
    }
}
