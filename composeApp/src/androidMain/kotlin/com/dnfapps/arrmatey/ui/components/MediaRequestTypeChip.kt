package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.seerr.api.model.RequestType

@Composable
fun MediaRequestTypeChip(
    text: String,
    requestType: RequestType,
    modifier: Modifier = Modifier,
) {
    val (container, content) =
        when (requestType) {
            RequestType.Tv -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
            RequestType.Movie -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
            RequestType.Person -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        }

    AMStatusBadge(
        text = text,
        containerColor = container,
        contentColor = content,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = modifier,
    )
}
