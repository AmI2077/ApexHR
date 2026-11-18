package com.example.apexhr.features.shop.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.model.formatAmount
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.shop.ui.models.ShopBadgeUiModel

/** Цвет пометки скидки, как в вебе (rose-600). */
private val DiscountColor = Color(0xFFE11D48)

/** Бейджи товара цветными пилюлями, как на карточке в вебе. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShopBadges(
    badges: List<ShopBadgeUiModel>,
    modifier: Modifier = Modifier,
) {
    if (badges.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
    ) {
        badges.forEach { badge ->
            ShopPill(text = badge.name, color = Color(badge.colorArgb))
        }
    }
}

/** Цена к оплате, рядом зачёркнутая цена без скидки и «−N%». */
@Composable
fun ShopPrice(
    price: Long,
    originalPrice: Long?,
    discountPercent: Int?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = ApexDimens.text16.sp,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing6.dp),
    ) {
        ApexAmount(
            amount = price,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
        )
        if (originalPrice != null) {
            Text(
                text = formatAmount(originalPrice),
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
                textDecoration = TextDecoration.LineThrough,
            )
        }
        if (discountPercent != null) {
            ShopPill(text = stringResource(R.string.shop_discount_percent, discountPercent), color = DiscountColor)
        }
    }
}

@Composable
private fun ShopPill(text: String, color: Color) {
    Text(
        text = text,
        color = Color.White,
        fontSize = ApexDimens.text12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(color)
            .padding(horizontal = ApexDimens.spacing8.dp, vertical = 2.dp),
    )
}