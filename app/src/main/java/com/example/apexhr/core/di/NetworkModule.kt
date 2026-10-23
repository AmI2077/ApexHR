package com.example.apexhr.core.di

import com.example.apexhr.core.data.network.KtorAuthClient
import com.example.apexhr.core.data.network.KtorModule
import com.example.apexhr.core.domain.NetworkAuthClient
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    single<NetworkAuthClient> {
        KtorAuthClient(get())
    }

    single { KtorModule.provideAuthClient(get()) }
}