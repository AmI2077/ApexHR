package com.example.apexhr.core.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.model.PageSlice
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ApexPagerPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ApexPager(page = 1, pages = 3, onPageChange = {})
    }
}

/** Листалка страниц, как Pager в вебе: «Назад», «2 / 5», «Дальше». */
@Composable
fun ApexPager(
    page: Int,
    pages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PagerButton(
            text = stringResource(R.string.pager_previous),
            enabled = page > 1,
            onClick = { onPageChange(page - 1) },
        )
        Text(
            text = stringResource(R.string.pager_position, page, pages),
            color = secondaryTextColor,
            fontSize = ApexDimens.text14.sp,
        )
        PagerButton(
            text = stringResource(R.string.pager_next),
            enabled = page < pages,
            onClick = { onPageChange(page + 1) },
        )
    }
}

/** Листалка для [PageSlice]; при одной странице ничего не показывает. */
@Composable
fun ApexPager(
    slice: PageSlice<*>,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (slice.isPagerVisible) {
        ApexPager(page = slice.page, pages = slice.pages, onPageChange = onPageChange, modifier = modifier)
    }
}

@Composable
private fun PagerButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(pillBackgroundColor)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else 0.4f)
            .heightIn(min = ApexDimens.pillTabHeight.dp)
            .padding(horizontal = ApexDimens.spacing16.dp, vertical = ApexDimens.spacing8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = primaryTextColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
