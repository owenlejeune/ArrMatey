package com.dnfapps.arrmatey.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class GridDensity(
    val label: StringResource,
    val minSize: Dp
) {
//    Compact(MR.strings.grid_density_compact, 60.dp),
    Small(MR.strings.grid_density_small, 90.dp),
    Normal(MR.strings.grid_density_normal, 120.dp),
    Large(MR.strings.grid_density_large, 150.dp),
//    XLarge(MR.strings.grid_density_xlarge, 180.dp)
}

enum class GridSpacing(
    val label: StringResource,
    val spacing: Dp
) {
    None(MR.strings.grid_spacing_none, 0.dp),
    Small(MR.strings.grid_spacing_small, 4.dp),
    Medium(MR.strings.grid_spacing_medium, 8.dp),
    Large(MR.strings.grid_spacing_large, 12.dp),
}

enum class PosterElevation(
    val label: StringResource,
    val elevation: Dp
) {
    None(MR.strings.poster_elevation_none, 0.dp),
    Low(MR.strings.poster_elevation_low, 6.dp),
    Medium(MR.strings.poster_elevation_medium, 12.dp),
    High(MR.strings.poster_elevation_high, 18.dp)
}

enum class PosterRadius(
    val label: StringResource,
    val radius: Dp
) {
    None(MR.strings.poster_radius_none, 0.dp),
    Small(MR.strings.poster_radius_small, 4.dp),
    Medium(MR.strings.poster_radius_medium, 8.dp),
    Large(MR.strings.poster_radius_large, 12.dp)
}

enum class Blur(
    val label: StringResource,
    val radius: Int
) {
    Off(MR.strings.banner_blur_off, 0),
    Low(MR.strings.banner_blur_low, 5),
    Normal(MR.strings.banner_blur_normal, 10),
    High(MR.strings.banner_blur_high, 20)
}