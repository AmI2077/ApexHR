package com.example.apexhr.core.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.model.apexPluralQuantity
import com.example.apexhr.core.ui.model.formatAmount
import com.example.apexhr.core.ui.model.formatSignedAmount
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor

/** С какой стороны от числа рисовать значок апексов. */
enum class ApexIconPosition { Start, End }

/** Размер значка относительно размера шрифта, если [ApexAmount] не передан явный iconSize. */
private const val ICON_TO_FONT_RATIO = 1.15f
private val DefaultFontSize = ApexDimens.text14.sp

/**
 * Сумма в апексах: число + значок валюты (Material Symbols «Diamond Shine»).
 *
 * Для чтения с экрана весь блок озвучивается как «1 250 апексов» (plurals apex_amount).
 *
 * @param amount сумма; форматируется через [formatAmount] (или [formatSignedAmount] при [showSign]).
 * @param style базовый стиль текста; [fontSize], [fontWeight] и [color] переопределяют его.
 * @param iconSize размер значка; по умолчанию — пропорционально размеру шрифта.
 * @param iconTint цвет значка; по умолчанию фирменный синий.
 * @param iconPosition значок перед числом ([ApexIconPosition.Start]) или после него.
 * @param showSign показывать «+» у положительных сумм и «−» у отрицательных (история операций).
 */
@Composable
fun ApexAmount(
    amount: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = primaryTextColor,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    iconSize: Dp = Dp.Unspecified,
    iconTint: Color = primaryBlueColor,
    iconPosition: ApexIconPosition = ApexIconPosition.Start,
    showSign: Boolean = false,
) {
    val formatted = if (showSign) formatSignedAmount(amount) else formatAmount(amount)
    ApexAmount(
        formattedAmount = formatted,
        modifier = modifier,
        style = style,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        iconSize = iconSize,
        iconTint = iconTint,
        iconPosition = iconPosition,
        contentDescription = pluralStringResource(
            R.plurals.apex_amount,
            apexPluralQuantity(amount),
            formatted,
        ),
    )
}

/**
 * То же, что [ApexAmount] с Long, но для уже отформатированной строки из UI-модели
 * (в моделях фич суммы часто лежат строкой после formatAmount).
 *
 * @param contentDescription что озвучивать вместо «число + значок»; удобно передать
 * результат [apexAmountText]. При null озвучивается только число.
 */
@Composable
fun ApexAmount(
    formattedAmount: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = primaryTextColor,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    iconSize: Dp = Dp.Unspecified,
    iconTint: Color = primaryBlueColor,
    iconPosition: ApexIconPosition = ApexIconPosition.Start,
    contentDescription: String? = null,
) {
    val resolvedFontSize = when {
        fontSize.isSpecified -> fontSize
        style.fontSize.isSpecified -> style.fontSize
        else -> DefaultFontSize
    }
    val resolvedIconSize = if (iconSize != Dp.Unspecified) {
        iconSize
    } else {
        // toDp() поддерживает только sp; для em берём размер по умолчанию.
        val base = if (resolvedFontSize.isSp) resolvedFontSize else DefaultFontSize
        with(LocalDensity.current) { base.toDp() } * ICON_TO_FONT_RATIO
    }
    val semanticsModifier = if (contentDescription != null) {
        Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
    } else {
        Modifier.semantics(mergeDescendants = true) {}
    }

    Row(
        modifier = modifier.then(semanticsModifier),
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconPosition == ApexIconPosition.Start) {
            ApexCurrencyIcon(size = resolvedIconSize, tint = iconTint)
        }
        Text(
            text = formattedAmount,
            style = style,
            color = color,
            fontSize = resolvedFontSize,
            fontWeight = fontWeight,
            maxLines = 1,
        )
        if (iconPosition == ApexIconPosition.End) {
            ApexCurrencyIcon(size = resolvedIconSize, tint = iconTint)
        }
    }
}

/** Отдельный значок апексов (например, для заглушек или кнопок). */
@Composable
fun ApexCurrencyIcon(
    modifier: Modifier = Modifier,
    size: Dp = ApexDimens.icon16.dp,
    tint: Color = primaryBlueColor,
    contentDescription: String? = null,
) {
    Icon(
        painter = painterResource(R.drawable.ic_diamond_shine_24),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size),
    )
}

/** Текст суммы со словом в нужной форме: 1 -> «1 апекс», 3 -> «3 апекса», 1250 -> «1 250 апексов». */
@Composable
@ReadOnlyComposable
fun apexAmountText(amount: Long): String =
    pluralStringResource(R.plurals.apex_amount, apexPluralQuantity(amount), formatAmount(amount))

/** Только слово в нужной форме, без числа: «апекс» / «апекса» / «апексов». */
@Composable
@ReadOnlyComposable
fun apexWord(amount: Long): String =
    pluralStringResource(R.plurals.apex_word, apexPluralQuantity(amount))

@Preview(showBackground = true)
@Composable
private fun ApexAmountPreview() {
    Column(modifier = Modifier.padding(ApexDimens.spacing16.dp)) {
        ApexAmount(amount = 1_250, fontSize = ApexDimens.text16.sp, fontWeight = FontWeight.Medium)
        ApexAmount(
            amount = 12_500,
            fontSize = ApexDimens.text28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryBlueColor,
            iconPosition = ApexIconPosition.End,
        )
        ApexAmount(amount = 150, showSign = true, color = primaryBlueColor)
        ApexAmount(amount = -50, showSign = true, color = negativeRedColor, iconTint = negativeRedColor)
        ApexAmount(formattedAmount = "300", iconSize = ApexDimens.icon24.dp)
        Text(text = apexAmountText(21) + ", " + apexAmountText(3) + ", " + apexAmountText(11))
    }
}