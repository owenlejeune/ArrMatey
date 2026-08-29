package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData

@Composable
fun EpisodePosterItem(episode: Episode) {
    val url = episode.getPoster()?.remoteUrl
    Box(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.surface)
                .height(100.dp),
    ) {
        AsyncImage(
            model = rememberRemoteImageData(url),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
        )
    }
}
