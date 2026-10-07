package com.dnfapps.arrmatey.ui.sheets.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.MultiSelectDropdownPicker
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun TagsSelectionCard(
    tags: List<Tag>,
    selectedTags: SnapshotStateList<Int>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (tags.isNotEmpty()) {
        ContainerCard(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
            shape = MaterialTheme.shapes.large,
            modifier = modifier.fillMaxWidth(),
        ) {
            MultiSelectDropdownPicker(
                options = tags.map { it.id },
                selectedOptions = selectedTags,
                valueLabel = mokoPlural(MR.plurals.tag_count, selectedTags.size),
                onOptionSelected = { tag, isSelected ->
                    if (isSelected) {
                        selectedTags.add(tag)
                    } else {
                        selectedTags.remove(tag)
                    }
                },
                getOptionLabel = { tag ->
                    tags.firstOrNull { tag == it.id }?.label
                        ?: mokoString(MR.strings.unknown)
                },
                label = { Text(mokoString(MR.strings.tags)) },
                enabled = enabled,
            )
        }
    }
}
