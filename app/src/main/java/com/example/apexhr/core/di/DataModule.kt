package com.example.apexhr.core.di

import com.example.apexhr.core.data.local.TokenDataStore
import com.example.apexhr.core.data.local.TokenEncryptor
import com.example.apexhr.core.data.local.getTokenStore
import com.example.apexhr.core.domain.TokenStorage
import com.example.apexhr.features.settings.data.SettingsRepositoryImpl
import com.example.apexhr.features.settings.data.getSettingsStore
import com.example.apexhr.features.settings.domain.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Отдельный DataStore настроек: без квалификатора по-прежнему отдаётся DataStore токенов. */
val SETTINGS_DATA_STORE = named("settingsDataStore")

val dataModule = module {
    includes(networkModule, repoModule)

    single { getTokenStore(androidContext().applicationContext) }
    single { TokenEncryptor() }

    single<TokenStorage> { TokenDataStore(get(), get()) }

    single(SETTINGS_DATA_STORE) { getSettingsStore(androidContext().applicationContext) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(SETTINGS_DATA_STORE)) }
}