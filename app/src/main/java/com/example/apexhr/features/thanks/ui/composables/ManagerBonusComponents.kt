package com.example.apexhr.features.thanks.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexPillTab
import com.example.apexhr.core.ui.composables.apexAmountText
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor

/** «Поощрение с рабочего баланса» — галочка из веба, на мобильном переключатель. */
@Composable
fun ManagerBonusToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.thanks_manager_mode),
            color = primaryTextColor,
            fontSize = ApexDimens.text14.sp,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            // Нажатие обрабатывает вся строка.
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = backgroundColor,
                checkedTrackColor = primaryBlueColor,
                uncheckedTrackColor = pillBackgroundColor,
            ),
        )
    }
}

/** «Рабочий баланс: N апексов» или «нет доступа», если его не удалось получить. */
@Composable
fun WorkBalanceText(
    workBalance: Long?,
    modifier: Modifier = Modifier,
) {
    Text(
        text = if (workBalance == null) {
            stringResource(R.string.thanks_work_balance_unavailable)
        } else {
            stringResource(R.string.thanks_work_balance, apexAmountText(workBalance))
        },
        color = secondaryTextColor,
        fontSize = ApexDimens.text13.sp,
        modifier = modifier,
    )
}

/** Суммы поощрения таблетками, как кнопки сумм в вебе. */
@Composable
fun BonusAmountPills(
    amounts: List<Long>,
    selected: Long?,
    onAmountClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
    ) {
        amounts.forEach { amount ->
            ApexPillTab(
                text = amount.toString(),
                selected = amount == selected,
                onClick = { onAmountClick(amount) },
            )
        }
    }
}
