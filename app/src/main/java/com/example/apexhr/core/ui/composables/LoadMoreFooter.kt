package com.example.apexhr.core.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apexhr.R
import com.example.apexhr.core.domain.DataError
import com.example.apexhr.core.ui.model.LoadMoreState
import com.example.apexhr.core.ui.model.messageRes
import com.example.apexhr.core.ui.theme.ApexDimens
import com.example.apexhr.core.ui.theme.primaryBlueColor
import com.example.apexhr.core.ui.theme.secondaryTextColor
import com.example.apexhr.ui.theme.ApexHRTheme

/**
 * Последний элемент постраничного LazyColumn: бесконечная прокрутка.
 *
 * LazyColumn создаёт элемент, только когда он подходит к экрану (с упреждающей
 * подгрузкой), поэтому в состоянии [LoadMoreState.Available] подвал сам вызывает
 * [onLoadMore], как только пользователь долистал до конца списка. Пока страница
 * грузится — индикатор; если не загрузилась — текст ошибки и «Повторить»
 * (повторно сам не запрашивает, чтобы не долбить сервер при плохой сети).
 * ViewModel должна игнорировать вызов, если загрузка уже идёт или страниц больше нет.
 */
@Composable
fun LoadMoreFooter(
    state: LoadMoreState,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    when (state) {
        LoadMoreState.Hidden -> Unit

        LoadMoreState.Available -> {
            // Ключ — сам подвал: после каждой догруженной страницы он снова входит
            // в состояние Available и, если всё ещё виден, просит следующую.
            LaunchedEffect(Unit) { currentOnLoadMore() }
            LoadMoreProgress(modifier)
        }

        LoadMoreState.Loading -> LoadMoreProgress(modifier)

        is LoadMoreState.Error -> Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = ApexDimens.spacing8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ApexDimens.spacing4.dp),
        ) {
            Text(
                text = stringResource(state.error.messageRes()),
                color = secondaryTextColor,
                fontSize = ApexDimens.text14.sp,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = { currentOnLoadMore() }) {
                Text(
                    text = stringResource(R.string.retry),
                    color = primaryBlueColor,
                    fontSize = ApexDimens.text14.sp,
                )
            }
        }
    }
}

@Composable
private fun LoadMoreProgress(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ApexDimens.spacing12.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = primaryBlueColor,
            strokeWidth = 3.dp,
            modifier = Modifier.size(LOAD_MORE_PROGRESS_SIZE.dp),
        )
    }
}

private const val LOAD_MORE_PROGRESS_SIZE = 28

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun LoadMoreFooterPreview() {
    ApexHRTheme(dynamicColor = false, darkTheme = false) {
        Column {
            LoadMoreFooter(state = LoadMoreState.Loading, onLoadMore = {})
            LoadMoreFooter(state = LoadMoreState.Error(DataError.Network), onLoadMore = {})
        }
    }
}
