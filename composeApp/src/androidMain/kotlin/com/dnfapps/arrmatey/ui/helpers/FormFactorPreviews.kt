package com.dnfapps.arrmatey.ui.helpers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-device Compose preview annotation covering key form factors:
 * Phone, Foldable (unfolded main screen), Tablet, and Desktop.
 */
@Preview(name = "1. Phone - Portrait", device = Devices.PHONE, showBackground = true)
@Preview(name = "2. Foldable - Unfolded Main Screen", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "3. Tablet - Landscape", device = Devices.TABLET, showBackground = true)
@Preview(name = "4. Desktop", device = Devices.DESKTOP, showBackground = true)
annotation class FormFactorPreviews

@FormFactorPreviews
@Composable
private fun FormFactorPreviewSample() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "ArrMatey Adaptive Multi-Device Layout Preview",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}
