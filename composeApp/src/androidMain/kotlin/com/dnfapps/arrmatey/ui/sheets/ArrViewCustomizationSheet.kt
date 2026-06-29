package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MediaItem
import com.dnfapps.arrmatey.ui.components.PosterGridItemOverlay
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
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
    onIncludeOverviewChanged: (Boolean) -> Unit,
    onBannerBlurChanged: (Blur) -> Unit,
    onGridDensityChanged: (GridDensity) -> Unit,
    onGridSpacingChanged: (GridSpacing) -> Unit,
    onPosterElevationChanged: (PosterElevation) -> Unit,
    onPosterRadiusChanged: (PosterRadius) -> Unit,
    onApplyGloballyChanged: (Boolean) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
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
                            bannerModel = model,
                            blur = preferences.bannerBlur,
                            posterRadius = preferences.posterRadius,
                            posterElevation = preferences.posterElevation
                        )
                    }

                    ViewType.Grid -> {
                        PosterItem(
                            posterHeight = 200.dp,
                            item = type.mockMedia,
                            posterModel = model,
                            aspectRatio = type.aspectRatio,
                            showFooter = preferences.showFullDetails,
                            radius = preferences.posterRadius,
                            elevation = preferences.posterElevation,
                            additionalContent = {
                                if (preferences.showOverlay) {
                                    PosterGridItemOverlay()
                                }
                            }
                        )
                    }
                }
            }
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                ViewType.entries.forEachIndexed { index, viewType ->
                    SegmentedButton(
                        modifier = Modifier.weight(1f),
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ViewType.entries.size
                        ),
                        onClick = { onViewTypeChanged(viewType) },
                        selected = viewType == preferences.viewType,
                        label = {
                            Text(
                                text = mokoString(viewType.resource),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            Text(
                text = mokoString(MR.strings.customization_options),
                style = MaterialTheme.typography.headlineSmall
            )

            LabelledSwitch(
                label = mokoString(MR.strings.apply_globally),
                sublabel = mokoString(MR.strings.apply_globally_message),
                checked = preferences.applyGlobally,
                onCheckedChange = { onApplyGloballyChanged(it) }
            )

            AnimatedContent(
                targetState = preferences.viewType,
                transitionSpec =  { fadeIn().togetherWith(fadeOut()) }
            ) { type ->
                when (type) {
                    ViewType.List -> ListTypeOptions(
                        preferences, onShowBannerBackgroundChanged, onIncludeOverviewChanged,
                        onBannerBlurChanged, onPosterElevationChanged, onPosterRadiusChanged
                    )

                    ViewType.Grid -> GridTypeOptions(
                        preferences, onShowFullDetailsChanged, onShowOverlayChanged,
                        onGridDensityChanged, onGridSpacingChanged, onPosterElevationChanged,
                        onPosterRadiusChanged
                    )
                }
            }
        }
    }
}

@Composable
fun ListTypeOptions(
    preferences: InstancePreferences,
    onShowBannerBackgroundChanged: (Boolean) -> Unit,
    onIncludeOverviewChanged: (Boolean) -> Unit,
    onBannerBlurChanged: (Blur) -> Unit,
    onPosterElevationChanged: (PosterElevation) -> Unit,
    onPosterRadiusChanged: (PosterRadius) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
        Column {
            Text(
                text = mokoString(MR.strings.banner_blur),
                style = MaterialTheme.typography.titleSmall
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                Blur.entries.forEachIndexed { index, blur ->
                    SegmentedButton(
                        modifier = Modifier.weight(1f),
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = Blur.entries.size
                        ),
                        onClick = { onBannerBlurChanged(blur) },
                        selected = blur == preferences.bannerBlur,
                        label = {
                            Text(
                                text = mokoString(blur.label),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        enabled = preferences.showBannerBackground
                    )
                }
            }
        }
        PosterOptions(
            preferences = preferences,
            onPosterRadiusChanged = onPosterRadiusChanged,
            onPosterElevationChanged = onPosterElevationChanged
        )
    }
}

@Composable
fun GridTypeOptions(
    preferences: InstancePreferences,
    onShowFullDetailsChanged: (Boolean) -> Unit,
    onShowOverlayChanged: (Boolean) -> Unit,
    onGridDensityChanged: (GridDensity) -> Unit,
    onGridSpacingChanged: (GridSpacing) -> Unit,
    onPosterElevationChanged: (PosterElevation) -> Unit,
    onPosterRadiusChanged: (PosterRadius) -> Unit
) {
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
        Column {
            Text(
                text = mokoString(MR.strings.grid_density),
                style = MaterialTheme.typography.titleSmall
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                GridDensity.entries.forEachIndexed { index, density ->
                    SegmentedButton(
                        modifier = Modifier.weight(1f),
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = GridDensity.entries.size
                        ),
                        onClick = { onGridDensityChanged(density) },
                        selected = density == preferences.gridDensity,
                        label = {
                            Text(
                                text = mokoString(density.label),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
        Column {
            Text(
                text = mokoString(MR.strings.grid_spacing),
                style = MaterialTheme.typography.titleSmall
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                GridSpacing.entries.forEachIndexed { index, spacing ->
                    SegmentedButton(
                        modifier = Modifier.weight(1f),
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = GridSpacing.entries.size
                        ),
                        onClick = { onGridSpacingChanged(spacing) },
                        selected = spacing == preferences.gridSpacing,
                        label = {
                            Text(
                                text = mokoString(spacing.label),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
        PosterOptions(
            preferences = preferences,
            onPosterRadiusChanged = onPosterRadiusChanged,
            onPosterElevationChanged = onPosterElevationChanged
        )
    }
}

@Composable
fun PosterOptions(
    preferences: InstancePreferences,
    onPosterElevationChanged: (PosterElevation) -> Unit,
    onPosterRadiusChanged: (PosterRadius) -> Unit
) {
    Column {
        Text(
            text = mokoString(MR.strings.poster_elevation),
            style = MaterialTheme.typography.titleSmall
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            PosterElevation.entries.forEachIndexed { index, elevation ->
                SegmentedButton(
                    modifier = Modifier.weight(1f),
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PosterElevation.entries.size
                    ),
                    onClick = { onPosterElevationChanged(elevation) },
                    selected = elevation == preferences.posterElevation,
                    label = {
                        Text(
                            text = mokoString(elevation.label),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
    Column {
        Text(
            text = mokoString(MR.strings.poster_radius),
            style = MaterialTheme.typography.titleSmall
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            PosterRadius.entries.forEachIndexed { index, radius ->
                SegmentedButton(
                    modifier = Modifier.weight(1f),
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = PosterRadius.entries.size
                    ),
                    onClick = { onPosterRadiusChanged(radius) },
                    selected = radius == preferences.posterRadius,
                    label = {
                        Text(
                            text = mokoString(radius.label),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview_ArrViewCustomizationSheet() {
    ArrViewCustomizationSheet(
        onDismissRequest = {},
        type = InstanceType.Sonarr,
        preferences = InstancePreferences(),
        onViewTypeChanged = { },
        onShowFullDetailsChanged = { },
        onShowOverlayChanged = {},
        onShowBannerBackgroundChanged = {},
        onIncludeOverviewChanged = {},
        onBannerBlurChanged = {},
        onGridDensityChanged = { },
        onGridSpacingChanged = {  },
        onPosterElevationChanged = {  },
        onPosterRadiusChanged = {},
        onApplyGloballyChanged = {}
    )
}