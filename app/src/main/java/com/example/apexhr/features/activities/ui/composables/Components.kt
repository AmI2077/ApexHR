package com.example.apexhr.features.activities.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.backgroundColor
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.primaryTextColor
import com.example.apexhr.core.ui.theme.secondaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.core.ui.theme.transparent
import com.example.apexhr.features.activities.ui.models.ActivitySubtitle
import com.example.apexhr.features.activities.ui.models.ActivityUiModel

@Composable
fun ActivitiesHeader(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.activities_title),
        color = primaryTextColor,
        fontSize = ApexDimens.text28.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun ActivitiesSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(ApexDimens.searchFieldHeight.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.activities_search_placeholder),
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
            )
        },
        leadingIcon = {
//            Icon(
//                imageVector = Icons.Filled.Search,
//                contentDescription = stringResource(R.string.search),
//                tint = secondaryTextColor,
//                modifier = Modifier.size(ApexDimens.icon20.dp),
//            )
        },
        singleLine = true,
        shape = RoundedCornerShape(ApexDimens.radius20.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            unfocusedContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            disabledContainerColor = secondaryBlueColor.copy(alpha = 0.28f),
            focusedIndicatorColor = transparent,
            unfocusedIndicatorColor = transparent,
            disabledIndicatorColor = transparent,
            cursorColor = primaryBlueColor,
            focusedTextColor = primaryTextColor,
            unfocusedTextColor = primaryTextColor,
        ),
    )
}

@Composable
fun ActivitiesPopularTitle(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.popular),
        color = primaryBlueColor,
        fontSize = ApexDimens.text12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@Composable
fun ActivityCard(
    activity: ActivityUiModel,
    onClick: () -> Unit,
    onParticipateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ApexDimens.radius20.dp))
            .background(secondaryBlueColor.copy(alpha = 0.28f))
            .clickable(onClick = onClick)
            .padding(ApexDimens.spacing12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ApexDimens.activityImageSize.dp)
                .clip(RoundedCornerShape(ApexDimens.radius16.dp))
                .background(secondaryBlueColor.copy(alpha = 0.55f)),
        )
        Spacer(modifier = Modifier.width(ApexDimens.spacing12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title ?: stringResource(R.string.activities_untitled),
                color = primaryTextColor,
                fontSize = ApexDimens.text16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(ApexDimens.spacing4.dp))
            activity.subtitle?.let { subtitle ->
                Text(
                    text = subtitle.text(),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (activity.reward != null) {
                Text(
                    text = stringResource(R.string.activities_reward, activity.reward),
                    color = primaryTextColor,
                    fontSize = ApexDimens.text13.sp,
                )
            }
            if (activity.statusRes != null) {
                Text(
                    text = stringResource(activity.statusRes),
                    color = primaryBlueColor,
                    fontSize = ApexDimens.text12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.height(ApexDimens.spacing8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onParticipateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlueColor,
                        contentColor = backgroundColor,
                    ),
                    shape = RoundedCornerShape(ApexDimens.radius20.dp),
                    contentPadding = PaddingValues(
                        horizontal = ApexDimens.spacing16.dp,
                        vertical = ApexDimens.spacing6.dp,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.participate),
                        fontSize = ApexDimens.text12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivitySubtitle.text(): String = when (this) {
    is ActivitySubtitle.Questions -> pluralStringResource(R.plurals.activities_questions_count, count, count)
    is ActivitySubtitle.Steps -> pluralStringResource(R.plurals.activities_steps_count, count, count)
    is ActivitySubtitle.EventTime -> listOfNotNull(startAt, location)
        .joinToString(stringResource(R.string.activities_event_separator))
}
