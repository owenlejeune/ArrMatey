package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.BasePosterItem
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrViewCustomizationSheet(
    onDismissRequest: () -> Unit,
    type: InstanceType,
    preferences: InstancePreferences,
    onViewTypeChanged: (ViewType) -> Unit,
    onShowFullDetailsChanged: (Boolean) -> Unit,
    onShowOverlayChanged: (Boolean) -> Unit,
    onShowBannerBackgroundChanged: (Boolean) -> Unit,
    onIncludeOverviewChanged: (Boolean) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            type.mockCover?.let { mockCover ->
                when (preferences.viewType) {
                    ViewType.List -> {

                    }

                    ViewType.Grid -> {
                        BasePosterItem(
                            model = painterResource(mockCover),
                            aspectRatio = type.aspectRatio,
                            modifier = Modifier.height(200.dp)
                        )
                    }
                }
            }
            SingleChoiceSegmentedButtonRow {
                ViewType.entries.forEachIndexed { index, viewType ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ViewType.entries.size
                        ),
                        onClick = { onViewTypeChanged(viewType) },
                        selected = viewType == preferences.viewType,
                        label = { Text(mokoString(viewType.resource)) }
                    )
                }
            }

            AnimatedContent(
                targetState = preferences.viewType,
                transitionSpec =  { fadeIn().togetherWith(fadeOut()) },
                modifier = Modifier.padding(12.dp)
            ) { type ->
                when (type) {
                    ViewType.List -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LabelledSwitch(
                                label = mokoString(MR.strings.show_banner_background),
                                checked = preferences.showBannerBackground,
                                onCheckedChange = { onShowBannerBackgroundChanged(it) }
                            )
                            LabelledSwitch(
                                label = mokoString(MR.strings.include_overview),
                                checked = preferences.includeOverview,
                                onCheckedChange = { onIncludeOverviewChanged(it) }
                            )
                        }
                    }

                    ViewType.Grid -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LabelledSwitch(
                                label = mokoString(MR.strings.show_full_details),
                                checked = preferences.showFullDetails,
                                onCheckedChange = { onShowFullDetailsChanged(it) }
                            )
                            LabelledSwitch(
                                label = mokoString(MR.strings.show_overlay_items),
                                checked = preferences.showOverlay,
                                onCheckedChange = { onShowOverlayChanged(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}