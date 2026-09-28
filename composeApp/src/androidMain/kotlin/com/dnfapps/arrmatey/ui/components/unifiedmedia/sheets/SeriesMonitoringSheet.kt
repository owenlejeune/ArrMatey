package com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.SeriesMonitorType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesMonitoringSheet(
    onDismissRequest: () -> Unit,
    onOptionSelected: (SeriesMonitorType) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = mokoString(MR.strings.series_monitoring),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
            HorizontalDivider()

            val options =
                remember {
                    SeriesMonitorType.entries.filter {
                        it != SeriesMonitorType.Unknown &&
                            it != SeriesMonitorType.LatestSeason &&
                            it != SeriesMonitorType.Skip
                    }
                }

            options.forEach { option ->
                ListItem(
                    headlineContent = { Text(mokoString(option.resource)) },
                    modifier =
                    Modifier.clickable {
                        onOptionSelected(option)
                        onDismissRequest()
                    },
                )
            }
        }
    }
}
