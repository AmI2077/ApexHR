package com.example.apexhr.core.di

import com.example.apexhr.features.auth.ui.LoginViewModel
import com.example.apexhr.features.auth.ui.SessionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vmModule = module {
    viewModel {
        SessionViewModel(get(), get())
    }

    viewModel {
        LoginViewModel(get())
    }
}
