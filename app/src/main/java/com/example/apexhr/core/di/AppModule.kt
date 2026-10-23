package com.example.apexhr.core.di

import org.koin.dsl.module

val appModule = module {
    includes(dataModule, vmModule)
}