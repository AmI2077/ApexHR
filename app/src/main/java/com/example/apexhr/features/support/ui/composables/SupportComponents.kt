package com.example.apexhr.features.support.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.core.ui.theme.transparent
import com.example.apexhr.features.support.domain.SupportTicketStatus
import com.example.apexhr.features.support.ui.models.labelRes

/** Поле ввода поддержки на фоне карточки. */
@Composable
fun SupportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = if (singleLine) 1 else maxLines,
        placeholder = {
            Text(text = placeholder, fontSize = ApexDimens.text14.sp, color = secondaryTextColor)
        },
        modifier = modifier,
        shape = RoundedCornerShape(ApexDimens.radius12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = pillBackgroundColor,
            unfocusedContainerColor = pillBackgroundColor,
            disabledContainerColor = pillBackgroundColor,
            focusedIndicatorColor = transparent,
            unfocusedIndicatorColor = transparent,
            disabledIndicatorColor = transparent,
            cursorColor = primaryBlueColor,
            focusedTextColor = primaryTextColor,
            unfocusedTextColor = primaryTextColor,
            disabledTextColor = secondaryTextColor,
        ),
    )
}

/** Статус обращения в виде пилюли. */
@Composable
fun SupportStatusPill(
    status: SupportTicketStatus,
    modifier: Modifier = Modifier,
) {
    val active = status.acceptsMessages
    Text(
        text = stringResource(status.labelRes()),
        color = if (active) primaryBlueColor else secondaryTextColor,
        fontSize = ApexDimens.text12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(pillBackgroundColor)
            .padding(horizontal = ApexDimens.spacing8.dp, vertical = ApexDimens.spacing2.dp),
    )
}