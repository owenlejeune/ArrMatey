package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.seerr.api.model.PersonCredits
import com.dnfapps.arrmatey.seerr.api.model.PersonDetails
import com.dnfapps.arrmatey.ui.helpers.LocalIsInTwoPane
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun PersonDetailsHeader(
    item: PersonDetails,
    credits: PersonCredits?,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    wideRailIsVisible: Boolean = false,
) {
    val backdrops = credits?.backdrops ?: emptyList()
    var currentIndex by remember { mutableIntStateOf(0) }
    val isInTwoPane = LocalIsInTwoPane.current

    if (backdrops.isNotEmpty()) {
        LaunchedEffect(backdrops) {
            while (true) {
                delay(5.seconds)
                currentIndex = (currentIndex + 1) % backdrops.size
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Crossfade(
            targetState = backdrops.getOrNull(currentIndex),
            animationSpec = tween(1500),
            label = "BackdropSlideshow",
            modifier = Modifier.matchParentSize(),
        ) { url ->
            Box(modifier = Modifier.fillMaxSize()) {
                DetailHeaderBanner(
                    bannerUrl = url,
                    gradientHeight = 150.dp,
                    startGradient = isExpanded && (wideRailIsVisible || isInTwoPane),
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 170.dp)
                    .padding(horizontal = 12.dp),
        ) {
            PosterItem(
                item = item,
                modifier = Modifier.width(150.dp),
                showOverlays = false,
            )
        }
    }
}
