package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.math.expm1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownPicker(
    options: Collection<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    getOptionLabel: @Composable (T) -> String = { it.toString() },
    getOptionIcon: (@Composable (T) -> ImageVector)? = null,
    label: @Composable () -> Unit = {},
    includeAllOption: Boolean = false,
    allLabel: String = mokoString(MR.strings.all),
    onAllSelected: () -> Unit = {}
) {
    var isDropDownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isDropDownExpanded,
        onExpandedChange = { isDropDownExpanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            label()
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                readOnly = true,
                value = when {
                    selectedOption != null -> getOptionLabel(selectedOption)
                    includeAllOption -> allLabel
                    else -> mokoString(MR.strings.unknown)
                },
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isDropDownExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large,
                singleLine = true
            )
        }
        ExposedDropdownMenu(
            expanded = isDropDownExpanded,
            onDismissRequest = { isDropDownExpanded = false },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            if (includeAllOption) {
                DropdownMenuItem(
                    text = {
                        Text(
                            allLabel,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        isDropDownExpanded = false
                        onAllSelected()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            }
            options.forEach { t ->
                DropdownMenuItem(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = {
                        Text(
                            text = getOptionLabel(t),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = getOptionIcon?.let {
                        {
                            Icon(
                                imageVector = it(t),
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded = false
                        onOptionSelected(t)
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MultiSelectDropdownPicker(
    options: Collection<T>,
    selectedOptions: List<T>,
    valueLabel: String,
    onOptionSelected: (T, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
    getOptionLabel: @Composable (T) -> String = { it.toString() },
) {
    var isDropDownExpanded  by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isDropDownExpanded,
        onExpandedChange = { isDropDownExpanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            label()
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                readOnly = true,
                value = valueLabel,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isDropDownExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large,
                singleLine = true
            )
        }
        ExposedDropdownMenu(
            expanded = isDropDownExpanded,
            onDismissRequest = { isDropDownExpanded = false },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                DropdownMenuItem(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = {
                        Text(
                            text = getOptionLabel(option),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(Icons.Default.Check, null)
                        }
                    },
                    onClick = {
                        onOptionSelected(option, !isSelected)
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}