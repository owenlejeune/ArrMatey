package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData

@Composable
fun AlbumCover(
    item: ArrAlbum,
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    radius: Dp = 10.dp,
    additionalContent: @Composable BoxScope.() -> Unit = {},
) {
    var imageLoadError by remember { mutableStateOf(false) }
    var imageLoaded by remember { mutableStateOf(false) }

    val url = item.getCover()?.remoteUrl

    val shadowModifier =
        if (elevation > 0.dp) {
            Modifier.shadow(
                elevation = elevation,
                shape = RoundedCornerShape(radius),
                clip = false,
            )
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(radius))
                .background(MaterialTheme.colorScheme.surface)
                .then(shadowModifier)
                .aspectRatio(1f, true),
    ) {
        AsyncImage(
            model =
                rememberRemoteImageData(
                    url = url,
                    onError = { _, err ->
                        println(err.throwable.message)
                        imageLoadError = true
                    },
                    onSuccess = { _, _ -> imageLoaded = true },
                ),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
        )
        if (imageLoadError) {
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp).align(Alignment.Center),
            )
        }
        if (imageLoaded) {
            additionalContent()
        }
    }
}
