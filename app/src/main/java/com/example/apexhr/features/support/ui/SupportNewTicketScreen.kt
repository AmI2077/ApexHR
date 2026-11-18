package com.example.apexhr.features.support.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.features.activities.ui.common.ActivityActionButton
import com.example.apexhr.features.shop.ui.cart.composables.ShopSubscreenHeader
import com.example.apexhr.features.support.ui.composables.SupportTextField
import com.example.apexhr.features.support.ui.models.SupportNewTicketScreenCallbacks
import com.example.apexhr.features.support.ui.models.SupportNewTicketUiState
import com.example.apexhr.ui.theme.ApexHRTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SupportNewTicketScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SupportNewTicketScreen(
            state = SupportNewTicketUiState(subject = "Не начислились апексы", message = ""),
            callbacks = SupportNewTicketScreenCallbacks.Empty,
        )
    }
}

/**
 * Новое обращение: тема и описание.
 * @param messages ошибки отправки (id строки), показываются тостом.
 */
@Composable
fun SupportNewTicketScreen(
    state: SupportNewTicketUiState,
    callbacks: SupportNewTicketScreenCallbacks,
    modifier: Modifier = Modifier,
    messages: Flow<Int> = emptyFlow(),
) {
    val context = LocalContext.current
    LaunchedEffect(messages) {
        messages.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding(),
    ) {
        ShopSubscreenHeader(
            title = stringResource(R.string.support_new_ticket),
            onBackClick = callbacks::onBackClick,
            modifier = Modifier.padding(ApexDimens.spacing8.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ApexDimens.spacing20.dp),
            verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
        ) {
            FieldLabel(stringResource(R.string.support_subject_label))
            SupportTextField(
                value = state.subject,
                onValueChange = callbacks::onSubjectChange,
                placeholder = stringResource(R.string.support_subject_placeholder),
                enabled = !state.isSending,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            FieldLabel(stringResource(R.string.support_message_label))
            SupportTextField(
                value = state.message,
                onValueChange = callbacks::onMessageChange,
                placeholder = stringResource(R.string.support_message_placeholder),
                enabled = !state.isSending,
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ActivityActionButton(
            text = stringResource(R.string.support_send_ticket),
            inProgress = state.isSending,
            enabled = state.canSend,
            onClick = callbacks::onSendClick,
            modifier = Modifier.padding(ApexDimens.spacing20.dp),
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = primaryTextColor,
        fontSize = ApexDimens.text14.sp,
        fontWeight = FontWeight.Medium,
    )
}