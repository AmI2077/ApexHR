package com.example.apexhr.core.di

import com.example.apexhr.core.data.local.TokenDataStore
import com.example.apexhr.core.data.local.TokenEncryptor
import com.example.apexhr.core.data.local.getTokenDataStore
import com.example.apexhr.core.domain.TokenStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    includes(networkModule, repoModule)

    single { getTokenDataStore(androidContext().applicationContext) }
    single { TokenEncryptor() }

    single<TokenStorage> { TokenDataStore(get(), get()) }
}