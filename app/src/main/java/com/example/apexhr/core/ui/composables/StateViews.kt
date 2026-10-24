package com.example.apexhr.core.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor

/** Индикатор загрузки блока данных. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ApexDimens.spacing40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = primaryBlueColor)
    }
}

/** Сообщение об ошибке загрузки с кнопкой «Повторить». */
@Composable
fun ErrorState(
    error: DataError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ApexDimens.spacing32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(error.messageRes()),
            color = secondaryTextColor,
            fontSize = ApexDimens.text14.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryBlueColor,
                contentColor = backgroundColor,
            ),
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

/** Заглушка для пустого списка. */
@Composable
fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = secondaryTextColor,
        fontSize = ApexDimens.text14.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ApexDimens.spacing24.dp),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ErrorStatePreview() {
    ErrorState(error = DataError.Network, onRetry = {})
}
