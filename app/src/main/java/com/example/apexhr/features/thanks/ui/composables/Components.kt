package com.example.apexhr.features.thanks.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexCurrencyIcon
import com.example.apexhr.core.ui.composables.ApexPillTabs
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.core.ui.theme.transparent
import com.example.apexhr.features.profile.ui.models.joinedText
import com.example.apexhr.features.thanks.ui.models.ColleagueUiModel
import com.example.apexhr.features.thanks.ui.models.ThanksNotice

@Composable
fun ThanksHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = stringResource(R.string.back),
//                    tint = primaryTextColor,
//                )
            }
            Text(
                text = stringResource(R.string.thanks_title),
                color = primaryTextColor,
                fontSize = ApexDimens.text20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = stringResource(R.string.thanks_subtitle),
            color = secondaryTextColor,
            fontSize = ApexDimens.text13.sp,
            modifier = Modifier.padding(start = ApexDimens.spacing16.dp),
        )
    }
}

@Composable
fun ThanksSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(ApexDimens.searchFieldHeight.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.thanks_search_placeholder),
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
            )
        },
        leadingIcon = {
//            Icon(
//                imageVector = Icons.Filled.Search,
//                contentDescription = stringResource(R.string.search),
//                tint = secondaryTextColor,
//                modifier = Modifier.size(ApexDimens.icon20.dp),
//            )
        },
        singleLine = true,
        shape = RoundedCornerShape(ApexDimens.radius20.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            unfocusedContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            disabledContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            focusedIndicatorColor = transparent,
            unfocusedIndicatorColor = transparent,
            disabledIndicatorColor = transparent,
            cursorColor = primaryBlueColor,
            focusedTextColor = primaryTextColor,
            unfocusedTextColor = primaryTextColor,
        ),
    )
}

@Composable
fun ColleagueRow(
    colleague: ColleagueUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    onQuickThanksClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(
                secondaryBlueColor.copy(alpha = if (selected) 0.45f else 0.28f)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = ApexDimens.spacing16.dp,
                vertical = ApexDimens.spacing12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = colleague.name ?: stringResource(R.string.thanks_unknown_colleague),
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing2.dp))
            Text(
                text = colleague.position.joinedText(),
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier.clickable(onClick = onQuickThanksClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
//            Icon(
//                imageVector = Icons.Filled.Favorite,
//                contentDescription = null,
//                tint = primaryBlueColor,
//                modifier = Modifier.size(ApexDimens.icon16.dp),
//            )
            Spacer(modifier = Modifier.width(ApexDimens.spacing4.dp))
            Text(
                text = stringResource(R.string.thanks_action),
                color = primaryBlueColor,
                fontSize = ApexDimens.text13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun ThanksMessageField(
    message: String,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.thanks_message_label),
            color = secondaryTextColor,
            fontSize = ApexDimens.text13.sp,
        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
        TextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(ApexDimens.messageFieldHeight.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.thanks_message_placeholder),
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                )
            },
            shape = RoundedCornerShape(ApexDimens.radius16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
                unfocusedContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
                disabledContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
                focusedIndicatorColor = transparent,
                unfocusedIndicatorColor = transparent,
                disabledIndicatorColor = transparent,
                cursorColor = primaryBlueColor,
                focusedTextColor = primaryTextColor,
                unfocusedTextColor = primaryTextColor,
            ),
        )
    }
}

@Composable
fun SendThanksButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(ApexDimens.buttonHeight.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryBlueColor,
            contentColor = backgroundColor,
            disabledContainerColor = secondaryBlueColor,
            disabledContentColor = backgroundColor,
        ),
        shape = RoundedCornerShape(ApexDimens.radius24.dp),
    ) {
        Text(
            text = stringResource(R.string.thanks_send),
            fontSize = ApexDimens.text16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun ThanksSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = secondaryTextColor,
        fontSize = ApexDimens.text12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@Composable
fun ThanksAmountSelector(
    options: List<Long>,
    selected: Long,
    onAmountClick: (Long) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Таблетки умеют только текст, поэтому значок валюты стоит у подписи.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.thanks_amount_label),
                color = secondaryTextColor,
                fontSize = ApexDimens.text13.sp,
            )
            Spacer(modifier = Modifier.width(ApexDimens.spacing4.dp))
            ApexCurrencyIcon(
                size = ApexDimens.icon16.dp,
                contentDescription = stringResource(R.string.thanks_amount_currency),
            )
        }
        Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
        ApexPillTabs(
            labels = options.map { amount -> stringResource(R.string.thanks_amount_option, amount) },
            selectedIndex = options.indexOf(selected),
            onSelect = { index -> onAmountClick(options[index]) },
            enabled = enabled,
        )
    }
}

@Composable
fun ThanksMessageCounter(
    length: Int,
    maxLength: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.thanks_message_counter, length, maxLength),
        color = secondaryTextColor,
        fontSize = ApexDimens.text12.sp,
        modifier = modifier,
    )
}

@Composable
fun ThanksNoticeText(
    notice: ThanksNotice,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(notice.textRes, *notice.args.toTypedArray()),
        color = if (notice.isError) negativeRedColor else primaryBlueColor,
        fontSize = ApexDimens.text14.sp,
        modifier = modifier.fillMaxWidth(),
    )
}
