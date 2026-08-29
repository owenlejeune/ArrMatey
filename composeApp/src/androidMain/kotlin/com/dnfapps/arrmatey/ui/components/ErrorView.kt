package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.HttpErrorType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ErrorView(
    errorType: HttpErrorType,
    message: String,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon =
        when (errorType) {
            HttpErrorType.Timeout -> Icons.Default.Timer
            HttpErrorType.Network -> Icons.Default.WifiOff
            else -> Icons.Default.Warning
        }

    val iconColor =
        when (errorType) {
            HttpErrorType.Timeout, HttpErrorType.Network -> Color(0xFFFFA500)
            else -> MaterialTheme.colorScheme.error
        }

    val title =
        when (errorType) {
            HttpErrorType.Timeout -> MR.strings.error_timeout_title
            HttpErrorType.Network -> MR.strings.error_network_title
            else -> MR.strings.error_generic_title
        }

    val detailMessage =
        if (errorType == HttpErrorType.Timeout) {
            mokoString(MR.strings.error_timeout_description)
        } else {
            message
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(48.dp),
        )

        Text(
            text = mokoString(title),
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = detailMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier =
                Modifier
                    .padding(horizontal = 32.dp)
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (errorType == HttpErrorType.Timeout) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(mokoString(MR.strings.error_timeout_configure_instance))
                }
            }

            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(mokoString(MR.strings.retry))
            }
        }

        if (errorType == HttpErrorType.Timeout) {
            Text(
                text = mokoString(MR.strings.error_timeout_tip, mokoString(MR.strings.slow_instance)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
            )
        }
    }
}
