package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.icons.Hard_drive
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstancePicker(
    type: InstanceType,
    currentInstance: Instance?,
    typeInstances: List<Instance>,
    onInstanceSelected: (Instance) -> Unit,
    modifier: Modifier = Modifier,
    buttonColors: IconButtonColors = IconButtonDefaults.iconButtonColors()
) {
    val navManager = navigationManager
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        IconButton(
            onClick = { isExpanded = true },
            colors = buttonColors
        ) {
            Icon(Hard_drive, null)
        }

        DropdownMenuPopup(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = interactionSource,
                containerColor = MenuDefaults.groupVibrantContainerColor
            ) {
                typeInstances.forEachIndexed { index, inst ->
                    DropdownMenuItem(
                        text = { Text(inst.label) },
                        selected = inst.id == currentInstance?.id,
                        shapes = MenuDefaults.itemShape(index, typeInstances.size+1),
                        colors = MenuDefaults.selectableItemVibrantColors(),
                        onClick = {
                            isExpanded = false
                            onInstanceSelected(inst)
                        },
                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                    )
                }
                HorizontalDivider(Modifier.padding(MenuDefaults.HorizontalDividerPadding))
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.add_instance)) },
                    selected = false,
                    shapes = MenuDefaults.itemShape(typeInstances.size, typeInstances.size+1),
                    colors = MenuDefaults.selectableItemVibrantColors(),
                    leadingIcon = { Icon(Icons.Default.Add, null) },
                    onClick = {
                        isExpanded = false
                        navManager.openNewInstanceScreen(type)
                    }
                )
            }
        }
    }
}