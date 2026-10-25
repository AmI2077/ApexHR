package com.example.apexhr.core.di

import com.example.apexhr.features.activities.ui.ActivitiesViewModel
import com.example.apexhr.features.activities.ui.event.EventDetailsViewModel
import com.example.apexhr.features.activities.ui.quest.QuestDetailsViewModel
import com.example.apexhr.features.auth.ui.LoginViewModel
import com.example.apexhr.features.auth.ui.SessionViewModel
import com.example.apexhr.features.home.ui.HomeViewModel
import com.example.apexhr.features.profile.ui.ProfileViewModel
import com.example.apexhr.features.shop.ui.ShopViewModel
import com.example.apexhr.features.shop.ui.cart.CartViewModel
import com.example.apexhr.features.shop.ui.orders.OrdersViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vmModule = module {
    viewModel {
        SessionViewModel(get(), get())
    }

    viewModel {
        LoginViewModel(get())
    }

    viewModel {
        ProfileViewModel(get())
    }

    viewModel {
        ShopViewModel(get(), get())
    }

    viewModel {
        CartViewModel(get())
    }

    viewModel {
        OrdersViewModel(get())
    }

    viewModel {
        HomeViewModel(get())
    }

    viewModel {
        ActivitiesViewModel(get())
    }

    viewModel { (eventId: Long) ->
        EventDetailsViewModel(eventId, get())
    }

    viewModel { (questId: Long) ->
        QuestDetailsViewModel(questId, get())
    }
}
