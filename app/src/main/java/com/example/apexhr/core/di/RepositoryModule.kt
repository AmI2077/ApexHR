package com.example.apexhr.core.di

import com.example.apexhr.features.activities.data.ActivitiesRepositoryImpl
import com.example.apexhr.features.activities.domain.ActivitiesRepository
import com.example.apexhr.features.auth.data.AuthRepositoryImpl
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.home.data.HomeRepositoryImpl
import com.example.apexhr.features.home.domain.HomeRepository
import com.example.apexhr.features.profile.data.ProfileRepositoryImpl
import com.example.apexhr.features.profile.domain.ProfileRepository
import com.example.apexhr.features.shop.data.CartRepositoryImpl
import com.example.apexhr.features.shop.data.ShopRepositoryImpl
import com.example.apexhr.features.shop.domain.CartRepository
import com.example.apexhr.features.shop.domain.ShopRepository
import org.koin.dsl.module

val repoModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<ShopRepository> { ShopRepositoryImpl(get()) }
    single<CartRepository> { CartRepositoryImpl(get(), get()) }
    single<HomeRepository> { HomeRepositoryImpl(get(), get()) }
    single<ActivitiesRepository> { ActivitiesRepositoryImpl(get()) }
}