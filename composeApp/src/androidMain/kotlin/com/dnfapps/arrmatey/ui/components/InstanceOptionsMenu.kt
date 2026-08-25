package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
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
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstanceOptionsMenu(
    modifier: Modifier = Modifier,
    onViewWebGui: () -> Unit,
    onRunRssSync: () -> Unit,
    onSearchAllMissing: () -> Unit,
    onUpdateLibrary: () -> Unit,
    onBackupDatabase: () -> Unit,
    trigger: @Composable (onClick: () -> Unit) -> Unit = { onClick ->
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }
    }
) {
    var expanded by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        trigger { expanded = true }

        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = groupInteractionSource
            ) {
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.view_web_gui)) },
                    shapes = MenuDefaults.itemShape(0, 1),
                    selected = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        expanded = false
                        onViewWebGui()
                    }
                )

                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.run_rss_sync)) },
                    shapes = MenuDefaults.itemShape(0, 1),
                    selected = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.RssFeed,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        expanded = false
                        onRunRssSync()
                    }
                )

                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.search_all_missing)) },
                    shapes = MenuDefaults.itemShape(0, 1),
                    selected = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        expanded = false
                        onSearchAllMissing()
                    }
                )

                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.update_library)) },
                    shapes = MenuDefaults.itemShape(0, 1),
                    selected = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        expanded = false
                        onUpdateLibrary()
                    }
                )

                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.backup_database)) },
                    shapes = MenuDefaults.itemShape(0, 1),
                    selected = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        expanded = false
                        onBackupDatabase()
                    }
                )
            }
        }
    }
}
