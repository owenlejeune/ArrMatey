package com.dnfapps.arrmatey.ui.components

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun AppIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawable =
        remember {
            context.packageManager.getApplicationIcon(context.packageName)
        }

    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                setImageDrawable(drawable)
            }
        },
        modifier = modifier,
    )
}
