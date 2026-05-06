package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MediaItem
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.TranslucentBlackDarker
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
                val model = painterResource(mockCover)
                when (preferences.viewType) {
                    ViewType.List -> {
                        MediaItem(
                            aspectRatio = type.aspectRatio,
                            item = type.mockMedia,
                            onItemClick = {},
                            showBannerBackground = preferences.showBannerBackground,
                            includeOverview = preferences.includeOverview,
                            posterModel = model,
                            bannerModel = model
                        )
                    }

                    ViewType.Grid -> {
                        PosterItem(
                            posterHeight = 200.dp,
                            item = type.mockMedia,
                            posterModel = model,
                            aspectRatio = type.aspectRatio,
                            showFooter = preferences.showFullDetails,
                            additionalContent = {
                                if (preferences.showOverlay) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(.5f)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    listOf(TranslucentBlackDarker, Color.Transparent)
                                                )
                                            )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                                        tint = Color.White
                                    )
                                    LinearProgressIndicator(
                                        progress = { 0.6f },
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .height(6.dp),
                                        color = ArrBlue,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
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