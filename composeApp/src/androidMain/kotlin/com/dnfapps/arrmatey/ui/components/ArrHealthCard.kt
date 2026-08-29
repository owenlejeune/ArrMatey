package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrHealthType
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ArrHealthCard(health: ArrHealth) {
    val context = LocalContext.current

    val colors =
        when (health.type) {
            ArrHealthType.Error ->
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            ArrHealthType.Warning ->
                CardDefaults.cardColors(
                    containerColor = Color(0xffffc653),
                    contentColor = Color.Black,
                )
            else -> CardDefaults.cardColors()
        }
    val icon =
        when (health.type) {
            ArrHealthType.Error -> Icons.Default.ErrorOutline
            ArrHealthType.Warning -> Icons.Default.WarningAmber
            else -> Icons.Default.Info
        }

    ContainerCard(
        colors = colors,
        modifier =
            Modifier
                .clickable(
                    enabled = health.wikiUrl != null,
                    onClick = {
                        health.wikiUrl?.let { context.openLink(it) }
                    },
                ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null)
            Text(
                text = health.message ?: mokoString(MR.strings.unknown),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            health.wikiUrl?.let {
                Icon(Icons.AutoMirrored.Default.MenuBook, null)
            }
        }
    }
}
