package com.example.apexhr.features.news.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.news.ui.models.NewsUiModel
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NewsCardPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NewsCard(news = NewsUiModel.Preview.first(), onClick = {}, showMeta = true)
    }
}

/**
 * Карточка новости, как в вебе: светло-синий фон, заголовок и краткое описание.
 * @param showMeta в ленте новостей ещё и «автор · дата».
 */
@Composable
fun NewsCard(
    news: NewsUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showMeta: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .clickable(onClick = onClick)
            .padding(horizontal = ApexDimens.spacing16.dp, vertical = ApexDimens.spacing12.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
    ) {
        Text(
            text = news.title.ifBlank { stringResource(R.string.news_untitled) },
            color = primaryTextColor,
            fontSize = ApexDimens.text16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (news.summary != null) {
            Text(
                text = news.summary,
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val meta = newsMeta(news)
        if (showMeta && meta != null) {
            Text(text = meta, color = secondaryTextColor, fontSize = ApexDimens.text12.sp)
        }
    }
}

/** «Валерия Лапшина · 15.11.2026»; null, если нет ни автора, ни даты. */
fun newsMeta(news: NewsUiModel): String? =
    listOfNotNull(news.author, news.date).joinToString(" · ").takeIf { it.isNotEmpty() }
