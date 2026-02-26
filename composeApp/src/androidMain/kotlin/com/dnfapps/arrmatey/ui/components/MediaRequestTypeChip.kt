package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.seerr.api.model.RequestType

@Composable
fun MediaRequestTypeChip(
    text: String,
    requestType: RequestType,
    modifier: Modifier = Modifier
) {
    val (container, content) = when (requestType) {
        RequestType.Tv -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        RequestType.Movie -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 14.sp
            ),
            color = content
        )
    }
}