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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.MockData
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.calendar.MovieCalendarItem
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarCardCustomizationSheet(
    useColoredCalendarCards: Boolean,
    onToggleUseColoredCalendarCards: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        CalendarCardCustomizationContent(
            useColoredCalendarCards = useColoredCalendarCards,
            onToggleUseColoredCalendarCards = onToggleUseColoredCalendarCards,
        )
    }
}

@Composable
fun CalendarCardCustomizationContent(
    useColoredCalendarCards: Boolean,
    onToggleUseColoredCalendarCards: () -> Unit,
) {
    val now = remember { Clock.System.now() }
    val today = remember(now) { now.toLocalDateTime(TimeZone.currentSystemDefault()).date }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = navigationBarBottomInset() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = mokoString(MR.strings.calendar_cards),
            style = MaterialTheme.typography.titleLarge,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            MovieCalendarItem(
                date = today,
                movie = MockData.mockMovie,
                instances = emptyList(),
                useFullColorCards = useColoredCalendarCards,
                posterModel = InstanceType.Radarr.mockCover?.let { painterResource(it) },
                onNavigate = {},
            )
        }

        LabelledSwitch(
            label = mokoString(MR.strings.use_colored_calendar_cards),
            sublabel = mokoString(MR.strings.use_colored_calendar_cards_desc),
            checked = useColoredCalendarCards,
            onCheckedChange = { onToggleUseColoredCalendarCards() },
        )
    }
}

@Preview
@Composable
private fun Preview_CalendarCardCustomizationSheet() {
    MaterialTheme {
        Surface {
            CalendarCardCustomizationContent(
                useColoredCalendarCards = false,
                onToggleUseColoredCalendarCards = {},
            )
        }
    }
}
