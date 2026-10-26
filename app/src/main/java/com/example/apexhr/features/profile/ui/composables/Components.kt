package com.example.apexhr.features.profile.ui.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.composables.ApexPillTabs
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.profile.ui.models.ProfileAchievementUiModel
import com.example.apexhr.features.profile.ui.models.ProfileTab
import com.example.apexhr.features.profile.ui.models.ThankUiModel

@Composable
fun ProfileHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                contentDescription = stringResource(R.string.back),
//                tint = primaryTextColor,
//            )
        }
        Text(
            text = stringResource(R.string.profile_title),
            color = primaryTextColor,
            fontSize = ApexDimens.text18.sp,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onLogoutClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = stringResource(R.string.logout),
                tint = primaryTextColor,
            )
        }
    }
}

@Composable
fun ProfileUserInfo(
    name: String,
    department: String,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val avatarDescription = stringResource(R.string.profile_avatar)
        Box(
            modifier = Modifier
                .size(ApexDimens.avatarSize.dp)
                .clip(CircleShape)
                .background(secondaryBlueColor.copy(alpha = 0.28f))
                .semantics { contentDescription = avatarDescription },
            contentAlignment = Alignment.Center,
        ) {
            // Силуэт виден, пока фото нет, оно грузится или не загрузилось.
            var photoLoaded by remember(avatarUrl) { mutableStateOf(false) }
            if (!photoLoaded) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = primaryBlueColor.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxSize(0.6f),
                )
            }
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = { photoLoaded = true },
                    onError = { photoLoaded = false },
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        Spacer(modifier = Modifier.width(ApexDimens.spacing16.dp))
        Column {
            Text(
                text = name.ifBlank { stringResource(R.string.profile_name_unknown) },
                color = primaryTextColor,
                fontSize = ApexDimens.text20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
            Text(
                text = department,
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
            )
        }
    }
}

/** Текущий баланс одной сплошной карточкой. */
@Composable
fun ProfileBalanceBlock(
    balance: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(ApexDimens.spacing16.dp),
    ) {
        Text(
            text = stringResource(R.string.profile_current_balance),
            color = secondaryTextColor,
            fontSize = ApexDimens.text14.sp,
        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
        ApexAmount(
            amount = balance,
            color = primaryBlueColor,
            fontSize = ApexDimens.text28.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun ProfileTabs(
    selectedTab: ProfileTab,
    onTabClick: (ProfileTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = ProfileTab.entries
    ApexPillTabs(
        labels = tabs.map { stringResource(it.labelRes()) },
        selectedIndex = tabs.indexOf(selectedTab),
        onSelect = { index -> onTabClick(tabs[index]) },
        modifier = modifier,
    )
}

@StringRes
private fun ProfileTab.labelRes(): Int = when (this) {
    ProfileTab.Thanks -> R.string.profile_tab_thanks
    ProfileTab.Achievements -> R.string.profile_tab_achievements
    ProfileTab.Operations -> R.string.profile_tab_operations
}

/** Заголовок блока «Полученные»/«Отправленные» на вкладке благодарностей. */
@Composable
fun ProfileSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = primaryTextColor,
        fontSize = ApexDimens.text18.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

/**
 * Полученная или отправленная благодарность. Полученная — со знаком «+»
 * и акцентным цветом, отправленная — без знака и приглушённо.
 */
@Composable
fun ProfileThankRow(
    thank: ThankUiModel,
    received: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .padding(
                horizontal = ApexDimens.spacing16.dp,
                vertical = ApexDimens.spacing12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = thank.name ?: stringResource(R.string.thanks_unknown_colleague),
                color = primaryTextColor,
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            thank.message?.let { message ->
                Spacer(modifier = Modifier.height(ApexDimens.spacing2.dp))
                Text(
                    text = message,
                    color = primaryTextColor,
                    fontSize = ApexDimens.text13.sp,
                )
            }
            thank.date?.let { date ->
                Spacer(modifier = Modifier.height(ApexDimens.spacing2.dp))
                Text(
                    text = date,
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text12.sp,
                )
            }
        }
        Spacer(modifier = Modifier.width(ApexDimens.spacing8.dp))
        val amountColor = if (received) primaryBlueColor else secondaryTextColor
        ApexAmount(
            amount = thank.amount,
            color = amountColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.Bold,
            iconTint = amountColor,
            showSign = received,
        )
    }
}

/**
 * Карточка достижения: звезда слева, ниже названия — «Открыто» у полученного
 * или условие получения у неполученного (если сервер его прислал).
 */
@Composable
fun ProfileAchievementRow(
    achievement: ProfileAchievementUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = if (achievement.isEarned) {
        stringResource(R.string.profile_achievement_earned)
    } else {
        achievement.description?.takeIf { it.isNotBlank() }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .clickable(onClick = onClick)
            .padding(
                horizontal = ApexDimens.spacing16.dp,
                vertical = ApexDimens.spacing12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (achievement.isEarned) Icons.Filled.Star else Icons.Outlined.StarOutline,
            contentDescription = null,
            tint = if (achievement.isEarned) primaryBlueColor else secondaryBlueColor,
            modifier = Modifier.size(ApexDimens.icon28.dp),
        )
        Spacer(modifier = Modifier.width(ApexDimens.spacing12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title.ifBlank { stringResource(R.string.profile_achievement_untitled) },
                color = primaryTextColor,
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.Bold,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(ApexDimens.spacing2.dp))
                Text(
                    text = subtitle,
                    color = secondaryTextColor,
                    fontSize = ApexDimens.text12.sp,
                )
            }
        }
    }
}

/** Переход к отправке благодарности коллеге. */
@Composable
fun ProfileSendThanksButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ApexDimens.buttonHeight.dp),
    ) {
        Text(
            text = stringResource(R.string.profile_send_thanks),
            color = primaryBlueColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
