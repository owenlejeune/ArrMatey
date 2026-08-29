package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData

@Composable
fun ClearLogo(item: ArrMedia) {
    item.getClearLogo()?.remoteUrl?.let { logo ->
        Box(
            modifier = Modifier.wrapContentSize(),
        ) {
            AsyncImage(
                model = rememberRemoteImageData(logo),
                contentDescription = item.title,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .height(120.dp)
                        .padding(horizontal = 6.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
