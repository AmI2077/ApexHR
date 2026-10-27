package com.example.apexhr.features.auth.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.ui.theme.ApexHRTheme

/**
 * Логотип «Апекс» экранов авторизации (вход и пробуждение сервера):
 * значок app_logo фирменного синего цвета и надпись «Апекс» по нижнему краю значка.
 * Для чтения с экрана весь логотип озвучивается одним словом «Апекс».
 */
@Composable
fun ApexLogo(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.auth_brand)

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description }
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            tint = primaryBlueColor,
            painter = painterResource(R.drawable.app_logo),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.align(Alignment.Bottom),
            text = description,
            style = MaterialTheme.typography.headlineMedium,
            color = primaryTextColor,
            fontWeight = FontWeight.Bold
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

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF121417)
@Composable
private fun ApexLogoDarkPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = true) {
        ApexLogo()
    }
}