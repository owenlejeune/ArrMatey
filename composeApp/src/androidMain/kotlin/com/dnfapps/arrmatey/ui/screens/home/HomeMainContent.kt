package com.dnfapps.arrmatey.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.ui.helpers.LocalIsTabActive

@Composable
fun HomeMainContent(
    overlayTab: TabItem?,
    visibleTabs: List<TabItem>,
    pagerState: PagerState,
    windowSizeClass: WindowSizeClass,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = overlayTab,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn().togetherWith(fadeOut())
        },
        label = "OverlayTransition",
    ) { currentOverlay ->
        if (currentOverlay != null) {
            CompositionLocalProvider(LocalIsTabActive provides true) {
                TabItemContent(currentOverlay, windowSizeClass, false)
            }
        } else {
            key(visibleTabs.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    beyondViewportPageCount = visibleTabs.size,
                    key = { page -> visibleTabs[page].key },
                ) { page ->
                    val wideRailIsVisible = isExpanded && overlayTab == null && visibleTabs.size > 1
                    val isTabActive = overlayTab == null && pagerState.currentPage == page
                    CompositionLocalProvider(LocalIsTabActive provides isTabActive) {
                        TabItemContent(visibleTabs[page], windowSizeClass, wideRailIsVisible)
                    }
                }
            }
        }
    }
}
