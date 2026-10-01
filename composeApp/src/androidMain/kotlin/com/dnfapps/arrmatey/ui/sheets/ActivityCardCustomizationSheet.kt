package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.MockData
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ActivityItem
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCardCustomizationSheet(
    useColoredActivityCards: Boolean,
    onToggleUseColoredActivityCards: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        ActivityCardCustomizationContent(
            useColoredActivityCards = useColoredActivityCards,
            onToggleUseColoredActivityCards = onToggleUseColoredActivityCards,
        )
    }
}

@Composable
fun ActivityCardCustomizationContent(
    useColoredActivityCards: Boolean,
    onToggleUseColoredActivityCards: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = navigationBarBottomInset() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = mokoString(MR.strings.activity_cards),
            style = MaterialTheme.typography.titleLarge,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            ActivityItem(
                item = MockData.mockQueueItem,
                useFullColorCards = useColoredActivityCards,
                onClick = {},
            )
        }

        LabelledSwitch(
            label = mokoString(MR.strings.use_colored_activity_cards),
            sublabel = mokoString(MR.strings.use_colored_activity_cards_desc),
            checked = useColoredActivityCards,
            onCheckedChange = { onToggleUseColoredActivityCards() },
        )
    }
}

@Preview
@Composable
private fun Preview_ActivityCardCustomizationSheet() {
    MaterialTheme {
        Surface {
            ActivityCardCustomizationContent(
                useColoredActivityCards = false,
                onToggleUseColoredActivityCards = {},
            )
        }
    }
}
