package com.example.apexhr.features.auth.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.primaryTextColor

/**
 * Логотип «Апекс»: фирменная «А» из app_logo (с градиентом, поэтому Image,
 * а не Icon с tint) и надпись «ПЕКС», выровненная по нижнему краю буквы.
 */
@Composable
fun ApexLogo(
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.auth_brand)

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Image(
            modifier = Modifier
                .height(52.dp)
                .alignBy { it.measuredHeight },
            painter = painterResource(R.drawable.app_logo),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.alignByBaseline(),
            text = stringResource(R.string.auth_brand_wordmark),
            color = primaryTextColor,
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApexLogoPreview() {
    ApexLogo()
}
