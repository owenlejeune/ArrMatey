package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.compose.utils.breakable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExtraFileCard(extraFile: ExtraFile) {
    ContainerCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = extraFile.relativePath.breakable(),
            style = MaterialTheme.typography.bodyMediumEmphasized,
        )
        Text(
            text = extraFile.type.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
