package com.example.apexhr.core.di

import com.example.apexhr.features.auth.data.AuthRepositoryImpl
import com.example.apexhr.features.auth.domain.AuthRepository
import org.koin.dsl.module

val repoModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}