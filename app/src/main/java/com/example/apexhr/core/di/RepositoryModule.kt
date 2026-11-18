package com.example.apexhr.core.di

import com.example.apexhr.features.notifications.data.NotificationsRepositoryImpl
import com.example.apexhr.features.notifications.domain.NotificationsRepository
import com.example.apexhr.features.support.data.SupportRepositoryImpl
import com.example.apexhr.features.support.domain.SupportRepository
import com.example.apexhr.features.activities.data.ActivitiesRepositoryImpl
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.auth.data.AuthRepositoryImpl
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.home.data.HomeRepositoryImpl
import com.example.apexhr.features.home.domain.HomeRepository
import com.example.apexhr.features.profile.data.AndroidAvatarImageCompressor
import com.example.apexhr.features.profile.data.AvatarRepositoryImpl
import com.example.apexhr.features.profile.data.ProfileRepositoryImpl
import com.example.apexhr.features.profile.data.TransactionHistoryRepositoryImpl
import com.example.apexhr.features.profile.domain.AvatarImageCompressor
import com.example.apexhr.features.profile.domain.AvatarRepository
import com.example.apexhr.features.profile.domain.ProfileRepository
import com.example.apexhr.features.profile.domain.TransactionHistoryRepository
import com.example.apexhr.features.shop.data.CartRepositoryImpl
import com.example.apexhr.features.shop.data.ShopRepositoryImpl
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.domain.ShopRepository
import com.example.apexhr.features.thanks.data.ThanksRepositoryImpl
import com.example.apexhr.features.thanks.domain.ThanksRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repoModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<AvatarImageCompressor> { AndroidAvatarImageCompressor(androidContext().applicationContext) }
    single<AvatarRepository> { AvatarRepositoryImpl(get(), get(), get(), get()) }
    single<TransactionHistoryRepository> { TransactionHistoryRepositoryImpl(get()) }
    single<ShopRepository> { ShopRepositoryImpl(get()) }
    single<CartRepository> { CartRepositoryImpl(get(), get()) }
    single<HomeRepository> { HomeRepositoryImpl(get(), get()) }
    single<ActivitiesRepository> { ActivitiesRepositoryImpl(get()) }
    single<ThanksRepository> { ThanksRepositoryImpl(get()) }
    single<NotificationsRepository> { NotificationsRepositoryImpl(get()) }
    single<SupportRepository> { SupportRepositoryImpl(get()) }
}