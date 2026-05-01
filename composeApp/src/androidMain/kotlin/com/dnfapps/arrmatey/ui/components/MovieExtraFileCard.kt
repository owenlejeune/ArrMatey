package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.compose.utils.breakable

@Composable
fun ExtraFileCard(extraFile: ExtraFile) {
    ContainerCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = extraFile.relativePath.breakable(),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = extraFile.type.name,
            fontSize = 12.sp
        )
    }
}