package com.example.apexhr.features.engagement.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ActivityLevelCardPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp)) {
            ActivityLevelCard(level = ActivityLevelUiModel.Preview, onMyActivityClick = {})
            ActivityLevelCard(level = ActivityLevelUiModel.Preview, detailed = true)
            ActivityLevelCard(level = null, onMyActivityClick = {})
        }
    }
}

/**
 * Карточка «Уровень активности», как ActivityLevelCard в вебе: название уровня,
 * очки из максимума, полоса прогресса и сколько осталось до следующего уровня.
 * @param level null — уровень не загрузился: вместо него текст ошибки.
 * @param detailed на экране «Моя активность»: ещё и из чего сложились очки.
 * @param onMyActivityClick ссылка «Моя активность» (на главной); null — ссылки нет.
 */
@Composable
fun ActivityLevelCard(
    level: ActivityLevelUiModel?,
    modifier: Modifier = Modifier,
    detailed: Boolean = false,
    onMyActivityClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(pillBackgroundColor)
            .padding(ApexDimens.spacing16.dp),
    ) {
        Text(
            text = stringResource(R.string.activity_level_caption),
            color = secondaryTextColor,
            fontSize = ApexDimens.text12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (level == null) {
            Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
            Text(
                text = stringResource(R.string.activity_level_unavailable),
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
            )
        } else {
            ActivityLevelBody(level = level, detailed = detailed)
        }
        if (onMyActivityClick != null) {
            Spacer(modifier = Modifier.height(ApexDimens.spacing12.dp))
            Text(
                text = stringResource(R.string.my_activity_title),
                color = primaryBlueColor,
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(ApexDimens.radius8.dp))
                    .clickable(onClick = onMyActivityClick)
                    .padding(vertical = ApexDimens.spacing4.dp),
            )
        }
    }
}

@Composable
private fun ActivityLevelBody(level: ActivityLevelUiModel, detailed: Boolean) {
    Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = level.title ?: stringResource(R.string.activity_level_untitled),
            color = primaryTextColor,
            fontSize = ApexDimens.text20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.activity_level_score, level.score, level.maxScore),
            color = secondaryTextColor,
            fontSize = ApexDimens.text14.sp,
        )
    }
    Spacer(modifier = Modifier.height(ApexDimens.spacing12.dp))
    LinearProgressIndicator(
        progress = { level.progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(ApexDimens.spacing6.dp),
        color = primaryBlueColor,
        trackColor = secondaryBlueColor.copy(alpha = 0.4f),
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
    Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
    Text(
        text = level.nextTitle?.let { stringResource(R.string.activity_level_to_next, it, level.pointsToNext) }
            ?: stringResource(R.string.activity_level_top),
        color = secondaryTextColor,
        fontSize = ApexDimens.text14.sp,
    )
    if (detailed && level.breakdown.isNotEmpty()) {
        Spacer(modifier = Modifier.height(ApexDimens.spacing12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing6.dp)) {
            level.breakdown.forEach { source ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = source.title ?: stringResource(R.string.activity_level_source_untitled),
                        color = primaryTextColor,
                        fontSize = ApexDimens.text14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = ApexDimens.spacing8.dp),
                    )
                    Text(
                        text = source.points,
                        color = primaryTextColor,
                        fontSize = ApexDimens.text14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
