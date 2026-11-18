package com.example.apexhr.features.home.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.home.ui.models.FeedItemUiModel
import com.example.apexhr.features.home.ui.models.HomeUiState
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HomeFeedListPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        HomeFeedList(items = HomeUiState.Preview.feed)
    }
}

/**
 * Лента благодарностей на главной, как Feed в вебе: строки одним элементом
 * ленты главной (записей не больше пяти, ленивость не нужна).
 */
@Composable
fun HomeFeedList(
    items: List<FeedItemUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(pillBackgroundColor)
            .padding(ApexDimens.spacing16.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
    ) {
        items.forEach { item -> HomeFeedRow(item = item) }
    }
}

/** «Кто → Кому — сообщение», ниже сумма в апексах и пометка «поощрение». */
@Composable
private fun HomeFeedRow(item: FeedItemUiModel) {
    val unknown = stringResource(R.string.home_leaderboard_unknown_name)
    val noMessage = stringResource(R.string.home_feed_no_message)
    Column(verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp)) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = primaryTextColor)) {
                    append(item.fromName ?: unknown)
                }
                withStyle(SpanStyle(color = secondaryTextColor)) {
                    append(" → ")
                    append(item.toName ?: unknown)
                }
                withStyle(SpanStyle(color = primaryTextColor)) {
                    append(" — ")
                    append(item.message ?: noMessage)
                }
            },
            fontSize = ApexDimens.text14.sp,
        )
        if (item.amount > 0 || item.isManagerBonus) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
            ) {
                if (item.amount > 0) {
                    ApexAmount(
                        amount = item.amount,
                        showSign = true,
                        color = primaryBlueColor,
                        iconTint = primaryBlueColor,
                        fontSize = ApexDimens.text13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (item.isManagerBonus) {
                    Text(
                        text = stringResource(R.string.home_feed_manager_bonus),
                        color = primaryBlueColor,
                        fontSize = ApexDimens.text12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
