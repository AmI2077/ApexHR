package com.example.apexhr.features.settings.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.settings.ui.models.SettingsScreenCallbacks
import com.example.apexhr.ui.theme.ApexHRTheme
import com.example.apexhr.core.ui.composables.ApexPillTabs
import com.example.apexhr.features.settings.domain.ThemeChoice

@StringRes
private fun ThemeChoice.labelRes(): Int = when (this) {
    ThemeChoice.System -> R.string.settings_theme_system
    ThemeChoice.Light -> R.string.settings_theme_light
    ThemeChoice.Dark -> R.string.settings_theme_dark
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SettingsScreenPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        SettingsScreen(themeChoice = ThemeChoice.System, callbacks = SettingsScreenCallbacks.Empty)
    }
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF09090B, widthDp = 360)
@Composable
fun SettingsScreenDarkPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = true) {
        SettingsScreen(themeChoice = ThemeChoice.Dark, callbacks = SettingsScreenCallbacks.Empty)
    }
}

/**
 * Настройки: выбор темы, смена пароля, поддержка и выход из аккаунта.
 *
 * @param themeChoice сохранённый выбор темы; без выбора — «Как в системе».
 */
@Composable
fun SettingsScreen(
    themeChoice: ThemeChoice,
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
            Text(
                text = stringResource(R.string.settings_theme),
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                modifier = Modifier.padding(vertical = ApexDimens.spacing8.dp),
            )
            val choices = ThemeChoice.entries
            ApexPillTabs(
                labels = choices.map { stringResource(it.labelRes()) },
                selectedIndex = choices.indexOf(themeChoice),
                onSelect = { index -> callbacks.onThemeChoiceClick(choices[index]) },
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClick = callbacks::onChangePasswordClick,
                    )
                    .padding(vertical = ApexDimens.spacing12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = secondaryTextColor,
                    modifier = Modifier.size(ApexDimens.icon20.dp),
                )
                Spacer(modifier = Modifier.width(ApexDimens.spacing12.dp))
                Text(
                    text = stringResource(R.string.settings_change_password),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text16.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = secondaryTextColor,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClick = callbacks::onSupportClick,
                    )
                    .padding(vertical = ApexDimens.spacing12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SupportAgent,
                    contentDescription = null,
                    tint = secondaryTextColor,
                    modifier = Modifier.size(ApexDimens.icon20.dp),
                )
                Spacer(modifier = Modifier.width(ApexDimens.spacing12.dp))
                Text(
                    text = stringResource(R.string.support_title),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text16.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = secondaryTextColor,
                )
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
