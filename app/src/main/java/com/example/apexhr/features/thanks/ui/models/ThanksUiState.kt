package com.example.apexhr.features.thanks.ui.models

data class ThanksUiState(
    val searchQuery: String,
    val colleagues: List<ColleagueUiModel>,
    val selectedColleagueId: String?,
    val message: String,
) {
    val selectedColleagueName: String?
        get() = colleagues.firstOrNull { it.id == selectedColleagueId }?.name

    companion object {
        val Preview = ThanksUiState(
            searchQuery = "",
            colleagues = listOf(
                ColleagueUiModel(
                    id = "anna",
                    name = "Анна Петрова",
                    position = "Менеджер по маркетингу",
                ),
                ColleagueUiModel(
                    id = "ivan",
                    name = "Иван Соколов",
                    position = "Руководитель разработки",
                ),
                ColleagueUiModel(
                    id = "maria",
                    name = "Мария Лебедева",
                    position = "Специалист по работе с клиентами",
                ),
                ColleagueUiModel(
                    id = "dmitry",
                    name = "Дмитрий Кузнецов",
                    position = "Аналитик",
                ),
            ),
            selectedColleagueId = "anna",
            message = "",
        )
    }
}

data class ColleagueUiModel(
    val id: String,
    val name: String,
    val position: String,
)
