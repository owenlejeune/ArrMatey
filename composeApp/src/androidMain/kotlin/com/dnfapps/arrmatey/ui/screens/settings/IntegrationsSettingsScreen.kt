package com.dnfapps.arrmatey.ui.screens.settings

import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.MoreScreenViewModel
import com.dnfapps.arrmatey.model.IconSource
import com.dnfapps.arrmatey.model.SettingItem
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.SettingsGroup
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationsSettingsScreen(
    onBack: () -> Unit,
    viewModel: MoreScreenViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val smartAddAction by viewModel.smartAddSeerrAction.collectAsStateWithLifecycle()
    val combineSeerrArrMedia by viewModel.combineSeerrArrMedia.collectAsStateWithLifecycle()
    val bazarrDetailsIntegration by viewModel.bazarrDetailsIntegration.collectAsStateWithLifecycle()

    var showSmartAddActionDropdown by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = mokoString(MR.strings.integrations)) },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = navigationBarBottomInset() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                SettingsGroup(
                    title = mokoString(MR.strings.seerr),
                    items =
                        listOf(
                            SettingItem(
                                icon = IconSource.Vector(Icons.Default.Approval),
                                title = mokoString(MR.strings.smart_add_seerr_action_title),
                                subtitle = mokoString(smartAddAction.resource),
                                onClick = { showSmartAddActionDropdown = true },
                                trailingContent = {
                                    Box {
                                        DropdownMenu(
                                            expanded = showSmartAddActionDropdown,
                                            onDismissRequest = {
                                                showSmartAddActionDropdown = false
                                            },
                                        ) {
                                            SmartAddSeerrAction.entries.forEach { action ->
                                                DropdownMenuItem(
                                                    text = { Text(mokoString(action.resource)) },
                                                    onClick = {
                                                        viewModel.setSmartAddSeerrAction(action)
                                                        showSmartAddActionDropdown = false
                                                    },
                                                )
                                            }
                                        }
                                    }
                                },
                            ),
                        ),
                    footer = mokoString(MR.strings.smart_add_seerr_action_description),
                )
                SettingsGroup(
                    items =
                        listOf(
                            SettingItem(
                                icon = IconSource.Vector(Icons.Default.Layers),
                                title = mokoString(MR.strings.combine_seerr_arr_media_title),
                                onClick = { viewModel.toggleCombineSeerrArrMedia() },
                                trailingContent = {
                                    Switch(
                                        checked = combineSeerrArrMedia,
                                        onCheckedChange = { viewModel.toggleCombineSeerrArrMedia() },
                                    )
                                },
                            ),
                        ),
                    footer = mokoString(MR.strings.combine_seerr_arr_media_description),
                )
            }

            SettingsGroup(
                title = mokoString(MR.strings.bazarr),
                items =
                    listOf(
                        SettingItem(
                            icon = IconSource.Vector(Icons.Default.Subtitles),
                            title = mokoString(MR.strings.bazarr_details_integration_title),
                            onClick = { viewModel.toggleBazarrDetailsIntegration() },
                            trailingContent = {
                                Switch(
                                    checked = bazarrDetailsIntegration,
                                    onCheckedChange = { viewModel.toggleBazarrDetailsIntegration() },
                                )
                            },
                        ),
                    ),
                footer = mokoString(MR.strings.bazarr_details_integration_description),
            )

            SettingsGroup(
                title = mokoString(MR.strings.deep_links_title),
                items =
                    listOf(
                        SettingItem(
                            icon = IconSource.Vector(Icons.Default.Link),
                            title = mokoString(MR.strings.tmdb_links),
                            onClick = {
                                val intent =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        Intent(
                                            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                            "package:${context.packageName}".toUri(),
                                        )
                                    } else {
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                    }
                                context.startActivity(intent)
                            },
                            trailingContent = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val isVerified =
                                        remember(context) {
                                            val manager = context.getSystemService(DomainVerificationManager::class.java)
                                            val userState = manager.getDomainVerificationUserState(context.packageName)
                                            userState?.hostToStateMap?.entries?.any { (host, state) ->
                                                host.contains("themoviedb.org") &&
                                                    (
                                                        state == DomainVerificationUserState.DOMAIN_STATE_VERIFIED ||
                                                            state == DomainVerificationUserState.DOMAIN_STATE_SELECTED
                                                    )
                                            } == true
                                        }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text =
                                                if (isVerified) {
                                                    mokoString(
                                                        MR.strings.links_verified,
                                                    )
                                                } else {
                                                    mokoString(MR.strings.links_not_verified)
                                                },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        )
                                        if (isVerified) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                        ),
                    ),
                footer = mokoString(MR.strings.tmdb_links_description),
            )
        }
    }
}
