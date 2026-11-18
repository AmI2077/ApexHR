package com.example.apexhr.features.home.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.composables.ApexAmount
import com.example.apexhr.core.ui.composables.EmptyState
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.darkBlueColor
import com.example.apexhr.core.ui.theme.pillBackgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.features.home.ui.models.AchievementUiModel
import com.example.apexhr.features.home.ui.models.ChallengeUiModel
import com.example.apexhr.features.home.ui.models.LeaderboardEntryUiModel

@Composable
fun HomeHeader(
    balance: Long?,
    onPointsClick: () -> Unit,
    modifier: Modifier = Modifier,
    unreadNotifications: Int = 0,
    onNotificationsClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = stringResource(R.string.apex_brand),
                tint = darkBlueColor,
                modifier = Modifier
                    .width(ApexDimens.logoWidth.dp)
                    .height(ApexDimens.logoHeight.dp),
            )
            Spacer(modifier = Modifier.width(ApexDimens.spacing8.dp))
            Text(
                text = stringResource(R.string.apex_brand),
                color = darkBlueColor,
                fontSize = ApexDimens.text22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            NotificationsBell(
                unreadCount = unreadNotifications,
                onClick = onNotificationsClick,
            )
            // Пока баланс не загружен, блок апексов не показываем.
            if (balance != null) {
                ApexAmount(
                    amount = balance,
                    modifier = Modifier.clickable(
                        onClick = onPointsClick,
                        onClickLabel = stringResource(R.string.home_balance_content_description),
                    ),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

/** Колокольчик уведомлений с бейджем непрочитанных (больше 99 — «99+»). */
@Composable
fun NotificationsBell(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = if (unreadCount > 0) {
        stringResource(R.string.notifications_bell_unread, unreadCount)
    } else {
        stringResource(R.string.notifications_title)
    }
    IconButton(onClick = onClick, modifier = modifier) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(containerColor = primaryBlueColor, contentColor = backgroundColor) {
                        Text(text = if (unreadCount > 99) "99+" else unreadCount.toString())
                    }
                }
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = description,
                tint = primaryTextColor,
            )
        }
    }
}

@Composable
fun HomeGreeting(
    userName: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                R.string.home_greeting,
                userName.ifBlank { stringResource(R.string.home_greeting_fallback_name) },
            ),
            color = primaryTextColor,
            fontSize = ApexDimens.text24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
        Text(
            text = stringResource(R.string.home_subtitle),
            color = secondaryTextColor,
            fontSize = ApexDimens.text14.sp,
        )
    }
}

@Composable
fun ChallengeCard(
    challenge: ChallengeUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .clickable(onClick = onClick)
            .padding(
                horizontal = ApexDimens.spacing20.dp,
                vertical = ApexDimens.spacing16.dp,
            ),
    ) {
        Text(
            text = challenge.title.ifBlank { stringResource(R.string.home_challenge_untitled) },
            color = primaryTextColor,
            fontSize = ApexDimens.text16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_challenge_prize),
                color = primaryBlueColor,
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.Medium,
            )
            ApexAmount(
                amount = challenge.prizeAmount,
                color = primaryBlueColor,
                fontSize = ApexDimens.text14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Сколько первых мест выделяется залитым синим кружком. */
private const val HIGHLIGHTED_RANKS = 3

/**
 * Максимальная высота списка в карточке лидерборда: строка ≈ 52 dp + 8 dp
 * промежутка, так видно 5 строк и половину шестой — понятно, что список листается.
 */
private const val LEADERBOARD_LIST_MAX_HEIGHT = 326

/**
 * Лидерборд в стиле карточек веба: светлая скруглённая карточка с заголовком
 * и подзаголовком, внутри — белые строки «место · имя · число благодарностей».
 *
 * В списке все сотрудники, поэтому он ограничен по высоте и листается внутри
 * карточки. Карточка — элемент LazyColumn главной, поэтому внутри не вложенный
 * LazyColumn (без фиксированной высоты он падает), а Column с verticalScroll
 * и heightIn(max): сотрудников десятки, ленивость здесь не нужна.
 */
@Composable
fun HomeLeaderboardCard(
    entries: List<LeaderboardEntryUiModel>,
    onEntryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    myEntry: LeaderboardEntryUiModel? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(pillBackgroundColor)
            .padding(ApexDimens.spacing16.dp),
    ) {
        Text(
            text = stringResource(R.string.home_leaderboard),
            color = primaryTextColor,
            fontSize = ApexDimens.text18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
        Text(
            text = stringResource(R.string.home_leaderboard_subtitle),
            color = secondaryTextColor,
            fontSize = ApexDimens.text14.sp,
        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing12.dp))
        if (entries.isEmpty()) {
            EmptyState(text = stringResource(R.string.home_leaderboard_empty))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = LEADERBOARD_LIST_MAX_HEIGHT.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
            ) {
                entries.forEach { entry ->
                    LeaderboardRow(
                        entry = entry,
                        onClick = { onEntryClick(entry.id) },
                    )
                }
            }
        }
        if (myEntry != null) {
            // Как в вебе: моё место под списком, если меня нет в топе.
            Spacer(modifier = Modifier.height(ApexDimens.spacing12.dp))
            Text(
                text = stringResource(R.string.home_leaderboard_my_place),
                color = secondaryTextColor,
                fontSize = ApexDimens.text12.sp,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
            LeaderboardRow(
                entry = myEntry,
                onClick = { onEntryClick(myEntry.id) },
            )
        }
    }
}

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(backgroundColor)
            .then(
                if (entry.isMine) {
                    Modifier.border(1.dp, primaryBlueColor, RoundedCornerShape(ApexDimens.radius16.dp))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = ApexDimens.spacing12.dp,
                vertical = ApexDimens.spacing10.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LeaderboardRankBadge(rank = entry.rank)
        Text(
            text = entry.name.ifBlank { stringResource(R.string.home_leaderboard_unknown_name) },
            color = primaryTextColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        // Число благодарностей, а не валюта, поэтому без значка апексов.
        Text(
            text = entry.thanksCount,
            color = primaryTextColor,
            fontSize = ApexDimens.text16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

/** Номер места в кружке: 1–3 — залитый фирменным синим с белой цифрой, остальные — светлый с синей. */
@Composable
private fun LeaderboardRankBadge(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    val highlighted = rank in 1..HIGHLIGHTED_RANKS
    Box(
        modifier = modifier
            .size(ApexDimens.icon32.dp)
            .clip(CircleShape)
            .background(if (highlighted) primaryBlueColor else secondaryBlueColor.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank.toString(),
            color = if (highlighted) backgroundColor else primaryBlueColor,
            fontSize = ApexDimens.text14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

/**
 * Строка достижения в стиле карточки профиля: звезда, жирное название и
 * подпись. Полученное — залитая синяя звезда и «Открыто», ещё не полученное —
 * светлая контурная звезда и описание условия из каталога.
 */
@Composable
fun HomeAchievementRow(
    achievement: AchievementUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = if (achievement.isEarned) {
        stringResource(R.string.home_achievement_earned)
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
                text = achievement.title.ifBlank { stringResource(R.string.home_achievement_untitled) },
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

@Composable
fun SectionTitle(
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
 * Все достижения списком: в строке помещаются и звезда, и название целиком,
 * и подпись, чего не дала бы сетка из узких карточек. Строки идут одним
 * элементом ленты главной с промежутком 8 dp, как в профиле.
 */
@Composable
fun HomeAchievementsList(
    achievements: List<AchievementUiModel>,
    onAchievementClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
    ) {
        achievements.forEach { achievement ->
            HomeAchievementRow(
                achievement = achievement,
                onClick = { onAchievementClick(achievement.id) },
            )
        }
    }
}