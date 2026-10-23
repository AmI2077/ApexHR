package com.example.apexhr.features.auth.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor

/**
 * Экран проверки сессии при запуске. Бэкенд после простоя «просыпается»
 * до минуты, поэтому вместо голого лоадера показываем логотип и пояснение.
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.auth_loading)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .safeDrawingPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        ApexLogo()

        Spacer(Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.auth_server_waking_up),
            style = MaterialTheme.typography.bodyLarge,
            color = secondaryTextColor
        )

        Spacer(Modifier.height(24.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .semantics { contentDescription = description },
            color = primaryBlueColor,
            trackColor = secondaryBlueColor,
            gapSize = 0.dp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun LoadingScreenPreview() {
    LoadingScreen()
}
