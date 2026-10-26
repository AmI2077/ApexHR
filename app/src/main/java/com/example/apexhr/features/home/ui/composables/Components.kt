package com.example.apexhr.features.home.ui.composables

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun AchievementCard(
    achievement: AchievementUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(ApexDimens.radius16.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .clickable(onClick = onClick)
            .padding(
                horizontal = ApexDimens.spacing8.dp,
                vertical = ApexDimens.spacing16.dp,
            )
            .height(ApexDimens.achievementMinHeight.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
//        Icon(
//            imageVector = Icons.Filled.Star,
//            contentDescription = null,
//            tint = primaryBlueColor,
//            modifier = Modifier.size(ApexDimens.icon28.dp),
//        )
        Spacer(modifier = Modifier.height(ApexDimens.spacing12.dp))
        Text(
            text = achievement.title.ifBlank { stringResource(R.string.home_achievement_untitled) },
            color = primaryTextColor,
            fontSize = ApexDimens.text12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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

@Composable
fun HomeAchievementsRow(
    achievements: List<AchievementUiModel>,
    onAchievementClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ApexDimens.spacing8.dp),
    ) {
        achievements.forEach { achievement ->
            AchievementCard(
                achievement = achievement,
                onClick = { onAchievementClick(achievement.id) },
                modifier = Modifier.weight(1f),
            )
        }
        if (achievements.isEmpty()) {
            Box(modifier = Modifier.weight(1f))
        }
    }
}
