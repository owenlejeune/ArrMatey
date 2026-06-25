package com.dnfapps.arrmatey.ui.components.bazarr

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitleLanguage

/** Small pill describing a subtitle language, e.g. "EN", "EN · Forced", "PT · HI". */
@Composable
fun SubtitleLanguageChip(
    language: BazarrSubtitleLanguage,
    modifier: Modifier = Modifier
) {
    SubtitleLanguageChip(
        label = language.chipLabel(),
        modifier = modifier
    )
}

@Composable
fun SubtitleLanguageChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

fun BazarrSubtitleLanguage.chipLabel(): String = buildString {
    append(code2.uppercase().ifBlank { name })
    if (forced) append(" · Forced")
    if (hi) append(" · HI")
}
