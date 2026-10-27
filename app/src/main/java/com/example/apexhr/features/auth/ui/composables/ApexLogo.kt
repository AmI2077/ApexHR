package com.example.apexhr.features.auth.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.ui.theme.ApexHRTheme

/** Высота знака-«А»; ширина — по пропорциям знака 540 × 607. */
private const val SIGN_HEIGHT = 56
private const val SIGN_WIDTH = 50

/**
 * Логотип «Апекс» экранов авторизации (вход и пробуждение сервера), как в вебе
 * (apexteam/public/brand/logo.png): синий знак-башня app_logo служит буквой «А»,
 * за ним надпись «ПЕКС» жирным шрифтом. Низ букв стоит на нижнем крае знака.
 * Надпись берёт цвет текста из темы, поэтому видна и в тёмной теме.
 * Для чтения с экрана весь логотип озвучивается одним словом «Апекс».
 */
@Composable
fun ApexLogo(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.auth_brand)

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.Bottom,
    ) {
        Icon(
            modifier = Modifier
                .width(SIGN_WIDTH.dp)
                .height(SIGN_HEIGHT.dp),
            tint = primaryBlueColor,
            painter = painterResource(R.drawable.app_logo),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            // Высота текста обрезается по базовой линии, чтобы при выравнивании
            // по низу буквы вставали ровно на нижний край знака.
            modifier = Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val baseline = placeable[LastBaseline]
                layout(placeable.width, baseline) { placeable.place(0, 0) }
            },
            text = stringResource(R.string.auth_brand_wordmark),
            color = primaryTextColor,
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApexLogoPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        ApexLogo()
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun ApexLogoDarkPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = true) {
        ApexLogo()
    }
}