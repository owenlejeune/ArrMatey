package com.dnfapps.arrmatey.ui.components.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.MediaScreen
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.ui.helpers.LocalIsInTwoPane
import org.koin.compose.koinInject

@Composable
fun TwoPaneMasterDetailNavDisplay(
    navigation: Navigator<NavKey>,
    isExpanded: Boolean,
    wideRailIsVisible: Boolean,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    modifier: Modifier = Modifier,
    isMasterScreen: (NavKey) -> Boolean = { it is ArrScreen.Library || it is MediaScreen.Search },
    preferencesStore: PreferencesStore = koinInject(),
) {
    val dualPanelSupport by preferencesStore.dualPanelSupport.collectAsStateWithLifecycle(true)

    val baseIndex = navigation.backStack.indexOfLast(isMasterScreen).coerceAtLeast(0)
    val baseScreen = navigation.backStack[baseIndex]

    val detailBackStack = navigation.backStack.filterIndexed { index, _ -> index > baseIndex }
    val showDetails = isExpanded && dualPanelSupport && detailBackStack.isNotEmpty()

    val detailsWeight by animateFloatAsState(
        targetValue =
            if (showDetails) {
                if (wideRailIsVisible) 1.25f else 1f
            } else {
                0.001f
            },
        label = "DetailsWeight",
    )

    Row(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavDisplay(
                backStack = if (showDetails) listOf(baseScreen) else navigation.backStack,
                onBack = { navigation.popBackStack() },
                transitionSpec = { forwardSlideTransform() },
                popTransitionSpec = { popSlideTransform() },
                predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
                entryProvider = entryProvider,
            )
        }

        val lastValidDetailBackStack = remember { mutableStateOf<List<NavKey>>(emptyList()) }
        if (detailBackStack.isNotEmpty()) {
            lastValidDetailBackStack.value = detailBackStack
        }

        AnimatedVisibility(
            visible = showDetails,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
            modifier = Modifier.weight(detailsWeight),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (lastValidDetailBackStack.value.isNotEmpty()) {
                    CompositionLocalProvider(LocalIsInTwoPane provides true) {
                        NavDisplay(
                            backStack = lastValidDetailBackStack.value,
                            onBack = { navigation.popBackStack() },
                            transitionSpec = { forwardSlideTransform() },
                            popTransitionSpec = { popSlideTransform() },
                            predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
                            entryProvider = entryProvider,
                        )
                    }
                }
            }
        }
    }
}
