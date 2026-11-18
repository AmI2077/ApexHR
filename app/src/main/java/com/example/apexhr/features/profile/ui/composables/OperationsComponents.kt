package com.example.apexhr.features.profile.ui.composables

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.composables.ApexPillTabs
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.profile.domain.TransactionFilter
import com.example.apexhr.features.profile.ui.history.models.HistoryTransactionUiModel
import com.example.apexhr.features.profile.ui.history.models.labelRes

/** Таблетки-фильтры по виду операции на вкладке «Операции». */
@Composable
fun ProfileOperationsFilters(
    selected: TransactionFilter,
    onFilterClick: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = TransactionFilter.entries
    ApexPillTabs(
        labels = filters.map { stringResource(it.labelRes()) },
        selectedIndex = filters.indexOf(selected),
        onSelect = { index -> onFilterClick(filters[index]) },
        modifier = modifier,
    )
}

/** Операция по балансу: описание, «вид · дата» и сумма со знаком. */
@Composable
fun ProfileOperationRow(
    transaction: HistoryTransactionUiModel,
    modifier: Modifier = Modifier,
) {
    val isPositive = transaction.amount >= 0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(
                horizontal = ApexDimens.spacing16.dp,
                vertical = ApexDimens.spacing12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title.ifBlank { stringResource(R.string.profile_transaction_untitled) },
                color = primaryTextColor,
                fontSize = ApexDimens.text14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(stringResource(transaction.typeRes), transaction.date)
                    .joinToString(" · "),
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
            )
        }
        val amountColor = if (isPositive) primaryBlueColor else negativeRedColor
        ApexAmount(
            amount = transaction.amount,
            color = amountColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.SemiBold,
            iconTint = amountColor,
            showSign = true,
            modifier = Modifier.padding(start = ApexDimens.spacing12.dp),
        )
    }
}


