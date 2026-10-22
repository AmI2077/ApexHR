package com.example.apexhr.features.thanks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.features.thanks.ui.composables.ColleagueRow
import com.example.apexhr.features.thanks.ui.composables.SendThanksButton
import com.example.apexhr.features.thanks.ui.composables.ThanksHeader
import com.example.apexhr.features.thanks.ui.composables.ThanksMessageField
import com.example.apexhr.features.thanks.ui.composables.ThanksSearchField
import com.example.apexhr.features.thanks.ui.composables.ThanksSectionLabel
import com.example.apexhr.features.thanks.ui.models.ThanksScreenCallbacks
import com.example.apexhr.features.thanks.ui.models.ThanksUiState
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ThanksScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ThanksScreen(
            uiState = ThanksUiState.Preview,
            callbacks = ThanksScreenCallbacks.Empty,
        )
    }
}

@Composable
fun ThanksScreen(
    uiState: ThanksUiState,
    callbacks: ThanksScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    ThanksScreenContent(
        uiState = uiState,
        callbacks = callbacks,
        modifier = modifier,
    )
}

@Composable
fun ThanksScreenContent(
    uiState: ThanksUiState,
    callbacks: ThanksScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = ApexDimens.spacing20.dp,
                vertical = ApexDimens.spacing8.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing16.dp),
    ) {
        ThanksHeader(onBackClick = callbacks::onBackClick)
        ThanksSectionLabel(text = stringResource(R.string.thanks_colleagues))
        ThanksSearchField(
            query = uiState.searchQuery,
            onQueryChange = callbacks::onSearchQueryChange,
        )
        uiState.colleagues.forEach { colleague ->
            ColleagueRow(
                colleague = colleague,
                selected = colleague.id == uiState.selectedColleagueId,
                onClick = { callbacks.onColleagueClick(colleague.id) },
                onQuickThanksClick = { callbacks.onQuickThanksClick(colleague.id) },
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.thanks_send_section),
                color = primaryTextColor,
                fontSize = ApexDimens.text18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
            uiState.selectedColleagueName?.let { name ->
                Text(
                    text = stringResource(R.string.thanks_recipient, name),
                    color = primaryBlueColor,
                    fontSize = ApexDimens.text14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
        ThanksMessageField(
            message = uiState.message,
            onMessageChange = callbacks::onMessageChange,
        )
        SendThanksButton(
            onClick = callbacks::onSendClick,
            enabled = uiState.selectedColleagueId != null,
        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
    }
}
