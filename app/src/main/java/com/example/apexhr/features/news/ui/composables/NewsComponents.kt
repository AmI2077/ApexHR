package com.example.apexhr.features.news.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.news.ui.models.NewsUiModel
import com.example.apexhr.ui.theme.ApexHRTheme

/** Обложка шире 16:9, как на карточках веба. */
const val NEWS_COVER_RATIO = 16f / 9f

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NewsCardPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NewsCard(news = NewsUiModel.Preview.first(), onClick = {}, showMeta = true)
    }
}

@Preview(name = "Carousel", showBackground = true, widthDp = 360)
@Composable
private fun NewsCarouselPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        NewsCarousel(news = NewsUiModel.Preview, onNewsClick = {})
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
private fun NewsCardDarkPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = true) {
        NewsCard(news = NewsUiModel.Preview.first(), onClick = {})
    }
}

/**
 * Карточка новости, как в вебе: скруглённая карточка с тонкой рамкой, сверху
 * обложка во всю ширину, под ней жирный заголовок и приглушённое краткое описание.
 * @param showMeta в ленте «Все новости» ещё и «автор · дата».
 */
@Composable
fun NewsCard(
    news: NewsUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showMeta: Boolean = false,
) {
    val shape = RoundedCornerShape(ApexDimens.radius16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(pillBackgroundColor)
            .border(width = 1.dp, color = secondaryTextColor.copy(alpha = 0.2f), shape = shape)
            .clickable(onClick = onClick),
    ) {
        NewsCover(
            imageUrl = news.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(NEWS_COVER_RATIO),
        )
        Column(
            modifier = Modifier.padding(
                horizontal = ApexDimens.spacing16.dp,
                vertical = ApexDimens.spacing12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
        ) {
            Text(
                text = news.title.ifBlank { stringResource(R.string.news_untitled) },
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (news.summary != null) {
                Text(
                    text = news.summary,
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val meta = newsMeta(news)
            if (showMeta && meta != null) {
                Text(text = meta, color = secondaryTextColor, fontSize = ApexDimens.text12.sp)
            }
        }
    }
}

/**
 * Обложка новости через Coil. Пока картинки нет или она не загрузилась —
 * заглушка: мягкий фирменный градиент со значком статьи.
 */
@Composable
fun NewsCover(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(secondaryBlueColor.copy(alpha = 0.55f), primaryBlueColor.copy(alpha = 0.35f)),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Article,
            contentDescription = null,
            tint = primaryBlueColor,
            modifier = Modifier.size(ApexDimens.icon32.dp),
        )
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.news_cover),
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

/**
 * Новости на главной: горизонтальная лента карточек на 85% ширины экрана.
 * Лента выходит за боковые отступы родителя ([edgePadding]), чтобы карточки
 * уезжали до края экрана, а первая стояла по сетке. Одна новость — во всю ширину.
 */
@Composable
fun NewsCarousel(
    news: List<NewsUiModel>,
    onNewsClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    edgePadding: Dp = 0.dp,
) {
    val single = news.singleOrNull()
    if (single != null) {
        NewsCard(news = single, onClick = { onNewsClick(single.id) }, modifier = modifier.fillMaxWidth())
        return
    }
    LazyRow(
        modifier = modifier
            .layout { measurable, constraints ->
                val extra = (edgePadding * 2).roundToPx()
                val width = constraints.maxWidth + extra
                val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                layout(constraints.maxWidth, placeable.height) {
                    placeable.place(-extra / 2, 0)
                }
            },
        contentPadding = PaddingValues(horizontal = edgePadding),
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
    ) {
        items(news, key = { it.id }) { item ->
            NewsCard(
                news = item,
                onClick = { onNewsClick(item.id) },
                modifier = Modifier.fillParentMaxWidth(NEWS_CAROUSEL_CARD_FRACTION),
            )
        }
    }
}

/** Доля ширины экрана под карточку в ленте новостей на главной. */
const val NEWS_CAROUSEL_CARD_FRACTION = 0.85f

/** «Валерия Лапшина · 18 ноя 2026, 01:59»; null, если нет ни автора, ни даты. */
fun newsMeta(news: NewsUiModel): String? =
    listOfNotNull(news.author, news.date).joinToString(" · ").takeIf { it.isNotEmpty() }
