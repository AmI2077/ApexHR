package com.example.apexhr.core.di

import com.example.apexhr.features.notifications.ui.NotificationsBadgeViewModel
import com.example.apexhr.features.notifications.ui.NotificationsViewModel
import com.example.apexhr.features.colleague.ui.ColleagueProfileViewModel
import com.example.apexhr.features.support.ui.SupportListViewModel
import com.example.apexhr.features.support.ui.SupportNewTicketViewModel
import com.example.apexhr.features.support.ui.SupportTicketViewModel
import com.example.apexhr.features.activities.ui.ActivitiesViewModel
import com.example.apexhr.features.activities.ui.event.EventDetailsViewModel
import com.example.apexhr.features.activities.ui.quest.QuestDetailsViewModel
import com.example.apexhr.features.activities.ui.quest.proof.QuestStepProofViewModel
import com.example.apexhr.features.activities.ui.survey.SurveyDetailsViewModel
import com.example.apexhr.features.auth.ui.ChangePasswordViewModel
import com.example.apexhr.features.auth.ui.LoginViewModel
import com.example.apexhr.features.auth.ui.SessionViewModel
import com.example.apexhr.features.home.ui.HomeViewModel
import com.example.apexhr.features.profile.ui.ProfileViewModel
import com.example.apexhr.features.settings.ui.SettingsViewModel
import com.example.apexhr.features.shop.ui.ShopViewModel
import com.example.apexhr.features.shop.ui.product.ProductDetailsViewModel
import com.example.apexhr.features.shop.ui.cart.CartViewModel
import com.example.apexhr.features.shop.ui.orders.OrdersViewModel
import com.example.apexhr.features.thanks.ui.ThanksViewModel
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
        ChangePasswordViewModel(get())
    }

    viewModel {
        ProfileViewModel(get(), get(), get(), get())
    }

    viewModel {
        ThanksViewModel(get())
    }

    viewModel {
        SettingsViewModel(get())
    }

    viewModel {
        ShopViewModel(get(), get())
    }

    viewModel { (productId: Long) ->
        ProductDetailsViewModel(productId, get(), get())
    }

    viewModel {
        CartViewModel(get())
    }

    viewModel {
        OrdersViewModel(get())
    }

    viewModel {
        NotificationsViewModel(get())
    }

    viewModel {
        NotificationsBadgeViewModel(get())
    }

    viewModel {
        SupportListViewModel(get())
    }

    viewModel {
        SupportNewTicketViewModel(get())
    }

    viewModel { (ticketId: Long) ->
        SupportTicketViewModel(ticketId, get())
    }

    viewModel { (userId: String) ->
        ColleagueProfileViewModel(userId, get())
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

    viewModel { (questId: Long, stepId: Long) ->
        QuestStepProofViewModel(questId, stepId, get(), get())
    }

    viewModel { (surveyId: Long) ->
        SurveyDetailsViewModel(surveyId, get())
    }
}
