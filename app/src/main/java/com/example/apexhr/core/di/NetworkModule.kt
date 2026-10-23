package com.example.apexhr.core.di

import com.example.apexhr.BuildConfig
import com.example.apexhr.core.data.network.KtorAuthClient
import com.example.apexhr.core.data.network.KtorModule
import com.example.apexhr.core.domain.NetworkAuthClient
import com.example.apexhr.core.domain.SessionEventBus
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Клиент без токена (логин). */
val AUTH_HTTP_CLIENT = named("authHttpClient")

/** Клиент с bearer-токеном (запросы от имени пользователя). */
val AUTHORIZED_HTTP_CLIENT = named("authorizedHttpClient")

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            coerceInputValues = true
        }
    }

    single { SessionEventBus() }

    single(AUTH_HTTP_CLIENT) {
        KtorModule.provideAuthClient(get(), BuildConfig.DEBUG)
    }

    single(AUTHORIZED_HTTP_CLIENT) {
        KtorModule.provideAuthorizedClient(
            json = get(),
            isDebug = BuildConfig.DEBUG,
            tokenStorage = get(),
            sessionEventBus = get()
        )
    }

    single<NetworkAuthClient> {
        KtorAuthClient(
            client = get(AUTH_HTTP_CLIENT),
            authorizedClient = get(AUTHORIZED_HTTP_CLIENT)
        )
    }
}
