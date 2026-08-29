package com.dnfapps.arrmatey.ui.helpers

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.bitmapConfig
import coil3.request.crossfade
import coil3.request.transformations

@Composable
fun rememberRemoteImageData(
    url: String?,
    cacheKey: String? = url,
    crossfade: Boolean = true,
    trim: Boolean = true,
    onError: (ImageRequest, ErrorResult) -> Unit = { _, _ -> },
    onSuccess: (ImageRequest, SuccessResult) -> Unit = { _, _ -> },
    onStart: (ImageRequest) -> Unit = { _ -> },
    onCancel: (ImageRequest) -> Unit = { _ -> },
): ImageRequest {
    val context = LocalPlatformContext.current

    return remember(url) {
        ImageRequest
            .Builder(context)
            .data(url)
            .diskCacheKey(cacheKey)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(crossfade)
            .listener(
                onError = onError,
                onSuccess = onSuccess,
                onStart = onStart,
                onCancel = onCancel,
            ).apply {
                if (trim) {
                    bitmapConfig(Bitmap.Config.ARGB_8888)
                    transformations(TrimTransparencyTransformation())
                }
            }.build()
    }
}
