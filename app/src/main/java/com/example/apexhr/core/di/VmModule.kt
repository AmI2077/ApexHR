package com.example.apexhr.core.di

import com.example.apexhr.features.auth.ui.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vmModule = module {
    viewModel {
        LoginViewModel(get())
    }
}