package com.example.apexhr.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.negativeRedColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.features.settings.ui.models.SettingsScreenCallbacks
import com.example.apexhr.ui.theme.ApexHRTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SettingsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SettingsScreen(darkTheme = false, callbacks = SettingsScreenCallbacks.Empty)
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
fun SettingsScreenDarkPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = true) {
        SettingsScreen(darkTheme = true, callbacks = SettingsScreenCallbacks.Empty)
    }
}

/**
 * Настройки: переключатель тёмной темы и выход из аккаунта.
 *
 * @param darkTheme включена ли сейчас тёмная тема (с учётом системной, если выбора нет).
 */
@Composable
fun SettingsScreen(
    darkTheme: Boolean,
    callbacks: SettingsScreenCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(ApexDimens.spacing8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = callbacks::onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = primaryTextColor,
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                color = primaryTextColor,
                fontSize = ApexDimens.text20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = ApexDimens.spacing12.dp),
        ) {
            Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = darkTheme,
                        role = Role.Switch,
                        onValueChange = callbacks::onDarkThemeChange,
                    )
                    .padding(vertical = ApexDimens.spacing12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_dark_theme),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text16.sp,
                    modifier = Modifier.weight(1f),
                )
                // Нажатие обрабатывает вся строка, поэтому у самого переключателя обработчика нет.
                Switch(checked = darkTheme, onCheckedChange = null)
            }
            Spacer(modifier = Modifier.height(ApexDimens.spacing24.dp))
            OutlinedButton(
                onClick = callbacks::onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ApexDimens.buttonHeight.dp),
                shape = RoundedCornerShape(ApexDimens.radius12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = negativeRedColor),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(ApexDimens.icon20.dp),
                )
                Spacer(modifier = Modifier.width(ApexDimens.spacing8.dp))
                Text(
                    text = stringResource(R.string.logout),
                    fontSize = ApexDimens.text16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
