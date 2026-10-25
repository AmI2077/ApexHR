package com.example.apexhr.features.activities.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import kotlinx.coroutines.flow.Flow

/** Шапка экрана деталей: «Назад» и заголовок. */
@Composable
fun ActivitySubscreenHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = primaryTextColor,
            )
        }
        Text(
            text = title,
            color = primaryTextColor,
            fontSize = ApexDimens.text20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

/**
 * Каркас экрана деталей: шапка, содержимое и snackbar снизу.
 * Содержимое — Loading/Error/контент, его выбирает вызывающий.
 */
@Composable
fun ActivityDetailsLayout(
    title: String,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ActivitySubscreenHeader(
                title = title,
                onBackClick = onBackClick,
                modifier = Modifier.padding(ApexDimens.spacing8.dp),
            )
            content()
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Прокручиваемое содержимое деталей с отступами по краям. */
@Composable
fun ActivityDetailsContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = ApexDimens.spacing20.dp,
                end = ApexDimens.spacing20.dp,
                bottom = ApexDimens.spacing24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
        content = content,
    )
}

@Composable
fun ActivityDetailsTitle(
    title: String?,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title ?: stringResource(R.string.activities_untitled),
        color = primaryTextColor,
        fontSize = ApexDimens.text22.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

/** Строка «подпись: значение» в блоке сведений. */
@Composable
fun ActivityInfoText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = primaryTextColor,
        fontSize = ApexDimens.text14.sp,
        modifier = modifier,
    )
}

@Composable
fun ActivityStatusText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = primaryBlueColor,
        fontSize = ApexDimens.text14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

/** Пояснение (почему действие недоступно и т. п.). */
@Composable
fun ActivityNoteText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = secondaryTextColor,
        fontSize = ApexDimens.text13.sp,
        modifier = modifier,
    )
}

@Composable
fun ActivityDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = primaryTextColor,
        fontSize = ApexDimens.text14.sp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(ApexDimens.spacing16.dp),
    )
}

/** Кнопка действия; пока идёт запрос, неактивна и показывает индикатор. */
@Composable
fun ActivityActionButton(
    text: String,
    inProgress: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !inProgress,
        modifier = modifier
            .fillMaxWidth()
            .height(ApexDimens.buttonHeight.dp),
        shape = RoundedCornerShape(ApexDimens.radius20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = primaryBlueColor,
            contentColor = backgroundColor,
        ),
    ) {
        if (inProgress) {
            CircularProgressIndicator(
                color = backgroundColor,
                strokeWidth = ApexDimens.spacing2.dp,
                modifier = Modifier.size(ApexDimens.icon20.dp),
            )
        } else {
            Text(
                text = text,
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Показывает сообщения ViewModel в snackbar по одному. */
@Composable
fun ActivityMessagesEffect(
    messages: Flow<ActivityMessage>,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(messages, snackbarHostState) {
        messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = context.getString(message.textRes, *message.args.toTypedArray()),
                duration = SnackbarDuration.Short,
            )
        }
    }
}
