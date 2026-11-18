package com.example.apexhr.core.di

import com.example.apexhr.BuildConfig
import com.example.apexhr.core.data.network.KtorAuthClient
import com.example.apexhr.core.data.network.KtorModule
import com.example.apexhr.core.domain.NetworkAuthClient
import com.example.apexhr.core.domain.SessionEventBus
import com.example.apexhr.features.activities.data.ActivitiesApi
import com.example.apexhr.features.activities.data.KtorActivitiesApi
import com.example.apexhr.features.activities.data.KtorProofStorageApi
import com.example.apexhr.features.activities.data.KtorQuestProofApi
import com.example.apexhr.features.activities.data.ProofStorageApi
import com.example.apexhr.features.activities.data.QuestProofApi
import com.example.apexhr.features.home.data.HomeApi
import com.example.apexhr.features.home.data.KtorHomeApi
import com.example.apexhr.features.profile.data.AvatarStorageApi
import com.example.apexhr.features.profile.data.KtorAvatarStorageApi
import com.example.apexhr.features.profile.data.KtorProfileApi
import com.example.apexhr.features.profile.data.ProfileApi
import com.example.apexhr.features.shop.data.CartApi
import com.example.apexhr.features.shop.data.KtorCartApi
import com.example.apexhr.features.shop.data.KtorShopApi
import com.example.apexhr.features.shop.data.ShopApi
import com.example.apexhr.features.thanks.data.KtorThanksApi
import com.example.apexhr.features.thanks.data.ThanksApi
import com.example.apexhr.features.notifications.data.KtorNotificationsApi
import com.example.apexhr.features.notifications.data.NotificationsApi
import com.example.apexhr.features.colleague.data.ColleagueApi
import com.example.apexhr.features.colleague.data.KtorColleagueApi
import com.example.apexhr.features.support.data.KtorSupportApi
import com.example.apexhr.features.support.data.SupportApi
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

    single<ProfileApi> { KtorProfileApi(get(AUTHORIZED_HTTP_CLIENT)) }
    // Supabase Storage: без автоматического bearer и сброса сессии на 401.
    single<AvatarStorageApi> { KtorAvatarStorageApi(get(AUTH_HTTP_CLIENT)) }
    single<ShopApi> { KtorShopApi(get(AUTHORIZED_HTTP_CLIENT)) }
    single<CartApi> { KtorCartApi(get(AUTHORIZED_HTTP_CLIENT)) }
    single<HomeApi> { KtorHomeApi(get(AUTHORIZED_HTTP_CLIENT)) }
    single<ActivitiesApi> { KtorActivitiesApi(get(AUTHORIZED_HTTP_CLIENT)) }
    single<QuestProofApi> { KtorQuestProofApi(get(AUTHORIZED_HTTP_CLIENT)) }
    single<ProofStorageApi> { KtorProofStorageApi(get(AUTH_HTTP_CLIENT)) }
    single<ThanksApi> { KtorThanksApi(get(AUTHORIZED_HTTP_CLIENT)) }
    single<NotificationsApi> { KtorNotificationsApi(get(AUTHORIZED_HTTP_CLIENT)) }
    single<SupportApi> { KtorSupportApi(get(AUTHORIZED_HTTP_CLIENT)) }
    single<ColleagueApi> { KtorColleagueApi(get(AUTHORIZED_HTTP_CLIENT)) }
}
