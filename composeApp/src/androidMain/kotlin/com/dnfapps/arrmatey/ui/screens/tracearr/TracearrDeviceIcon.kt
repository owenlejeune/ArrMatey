package com.dnfapps.arrmatey.ui.screens.tracearr

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.tracearr.api.model.TracearrDevicePlatform
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession

@Composable
fun TracearrDeviceIcon(streamSession: TracearrStreamSession) {
    val devicePlatform =
        remember(streamSession) {
            TracearrDevicePlatform.fromSession(
                streamSession.platform,
                streamSession.product,
                streamSession.device,
            )
        }
    val icon =
        when (devicePlatform) {
            TracearrDevicePlatform.PHONE -> Icons.Default.PhoneIphone
            TracearrDevicePlatform.TABLET -> Icons.Default.Tablet
            TracearrDevicePlatform.TV -> Icons.Default.Tv
            TracearrDevicePlatform.DESKTOP -> Icons.Default.DesktopWindows
            TracearrDevicePlatform.CONSOLE -> Icons.Default.SportsEsports
            TracearrDevicePlatform.UNKNOWN -> Icons.Default.SmartDisplay
        }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.size(24.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = devicePlatform.name,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
