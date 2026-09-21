package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.screens.TabCustomizationContent
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabCustomizationSheet(
    onDismissRequest: () -> Unit,
    preferenceStore: PreferencesStore = koinInject(),
    tabManager: TabManager = koinInject(),
) {
    val tabConfig by tabManager.tabConfiguration.collectAsStateWithLifecycle()
    val useServiceNavLogos by preferenceStore.useServiceNavLogos.collectAsStateWithLifecycle(false)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = mokoString(MR.strings.customize_navigation),
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = { preferenceStore.resetTabPreferences() }) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset",
                    )
                }
            }

            TabCustomizationContent(
                useServiceNavLogos = useServiceNavLogos,
                visibleTabs = tabConfig.visibleTabs,
                drawerTabs = tabConfig.drawerTabs,
                hiddenTabs = tabConfig.hiddenTabs,
                updatePreferences = { preferenceStore.updateTabPreferences(it) },
                contentPadding = PaddingValues(bottom = navigationBarBottomInset() + 16.dp),
            )
        }
    }
}
