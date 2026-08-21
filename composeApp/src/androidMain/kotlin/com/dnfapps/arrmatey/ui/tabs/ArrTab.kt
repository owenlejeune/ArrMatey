package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.MediaScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.navigation.toSearch
import com.dnfapps.arrmatey.ui.components.navigation.forwardSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.mediaNavEntries
import com.dnfapps.arrmatey.ui.components.navigation.popSlideTransform
import com.dnfapps.arrmatey.ui.components.navigation.predictivePopSlideTransform
import com.dnfapps.arrmatey.ui.screens.ArrLibraryScreen
import org.koin.compose.koinInject

@Composable
fun ArrTab(
    type: InstanceType,
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<NavKey> = navigationManager.arr(type)
) {
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    val baseIndex = navigation.backStack.indexOfLast { it is ArrScreen.Library || it is MediaScreen.Search }.coerceAtLeast(0)
    val baseScreen = navigation.backStack[baseIndex]

    val detailBackStack = navigation.backStack.filterIndexed { index, _ -> index > baseIndex }
    val showDetails = isExpanded && detailBackStack.isNotEmpty()

    val detailsWeight by animateFloatAsState(
        targetValue = if (showDetails) 1f else 0.001f,
        label = "DetailsWeight"
    )

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavDisplay(
                backStack = if (showDetails) listOf(baseScreen) else navigation.backStack,
                onBack = { navigation.popBackStack() },
                transitionSpec = { forwardSlideTransform() },
                popTransitionSpec = { popSlideTransform() },
                predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
                entryProvider = arrEntryProvider(type, isExpanded, wideRailIsVisible, navigation)
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
            modifier = Modifier.weight(detailsWeight)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (lastValidDetailBackStack.value.isNotEmpty()) {
                    NavDisplay(
                        backStack = lastValidDetailBackStack.value,
                        onBack = { navigation.popBackStack() },
                        transitionSpec = { forwardSlideTransform() },
                        popTransitionSpec = { popSlideTransform() },
                        predictivePopTransitionSpec = { _ -> predictivePopSlideTransform() },
                        entryProvider = arrEntryProvider(type, isExpanded, wideRailIsVisible, navigation)
                    )
                }
            }
        }
    }
}

private fun arrEntryProvider(type: InstanceType, isExpanded: Boolean, wideRailIsVisible: Boolean, navigation: Navigator<*>) = entryProvider {
    entry<ArrScreen.Library> {
        ArrLibraryScreen(
            type = type,
            isExpanded = isExpanded,
            wideRailIsVisible = wideRailIsVisible,
            onNavigateToSearch = { navigation.toSearch(it) },
            onNavigateToDetails = { media ->
                val tmdbId = when (media) {
                    is ArrMovie -> media.tmdbId.takeIf { it > 0 }
                    is ArrSeries -> media.tmdbId?.takeIf { it > 0 }
                    else -> null
                }
                val tvdbId = (media as? ArrSeries)?.tvdbId?.takeIf { it > 0 }
                navigation.toDetails(
                    id = media.id,
                    tmdbId = tmdbId,
                    tvdbId = tvdbId,
                    type = type
                )
            }
        )
    }
    mediaNavEntries(navigation = navigation, isExpanded = isExpanded, defaultInstanceType = type)
}
