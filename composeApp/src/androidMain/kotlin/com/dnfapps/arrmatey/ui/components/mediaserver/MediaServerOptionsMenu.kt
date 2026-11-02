package com.dnfapps.arrmatey.ui.components.mediaserver

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaServerOptionsMenu(
    modifier: Modifier = Modifier,
    onViewWebGui: () -> Unit,
    onRefresh: (() -> Unit)? = null,
    trigger: @Composable (onClick: () -> Unit) -> Unit = { onClick ->
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
            )
        }
    },
) {
    var expanded by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        trigger { expanded = true }

        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = groupInteractionSource,
            ) {
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.view_web_gui)) },
                    shapes = MenuDefaults.itemShape(0, 1),
                    selected = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        expanded = false
                        onViewWebGui()
                    },
                )

                if (onRefresh != null) {
                    DropdownMenuItem(
                        text = { Text(mokoString(MR.strings.refresh)) },
                        shapes = MenuDefaults.itemShape(0, 1),
                        selected = false,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            expanded = false
                            onRefresh()
                        },
                    )
                }
            }
        }
    }
}
