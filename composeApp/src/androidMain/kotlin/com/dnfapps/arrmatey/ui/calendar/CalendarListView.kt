package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.CalendarState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch

@Composable
fun CalendarListView(
    state: CalendarState,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val endOfListReached by remember(state.dates) {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= state.dates.size - 1
        }
    }

    val todayIndex by remember(state.dates, state.today) {
        derivedStateOf {
            state.dates.indexOfFirst { it >= state.today }.coerceAtLeast(0)
        }
    }

    val isTodayVisible by remember(todayIndex) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.any { it.index == todayIndex }
        }
    }

    LaunchedEffect(state.dates.isEmpty()) {
        if (state.dates.isNotEmpty()) {
            val targetIndex = state.dates
                .indexOfFirst { it >= state.today }
                .coerceAtLeast(0)
            listState.scrollToItem(targetIndex)
        }
    }

    LaunchedEffect(endOfListReached, state.isLoadingFuture) {
        if (endOfListReached && !state.isLoadingFuture) {
            onLoadMore()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = state.dates,
                key = { it.toString() }
            ) { date ->
                CalendarDaySection(
                    date = date,
                    items = state.items[date] ?: emptyList()
                )
            }
        }

        AnimatedVisibility(
            visible = !isTodayVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(todayIndex)
                    }
                },
                modifier = Modifier.padding(12.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = mokoString(MR.strings.today)
                )
            }
        }
    }
}