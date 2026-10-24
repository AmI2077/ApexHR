package com.example.apexhr.core.di

import com.example.apexhr.features.auth.data.AuthRepositoryImpl
import com.example.apexhr.features.auth.domain.AuthRepository
import com.example.apexhr.features.profile.data.ProfileRepositoryImpl
import com.example.apexhr.features.profile.domain.ProfileRepository
import com.example.apexhr.features.shop.data.ShopRepositoryImpl
import com.example.apexhr.features.shop.domain.ShopRepository
import org.koin.dsl.module

val repoModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
    single<ShopRepository> { ShopRepositoryImpl(get()) }
}