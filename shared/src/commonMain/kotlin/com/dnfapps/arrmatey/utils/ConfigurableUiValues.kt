package com.dnfapps.arrmatey.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
enum class GridDensity(
    val label: StringResource,
    val minSize: Dp,
    val iosSize: Double
) {
    Small(MR.strings.grid_density_small, 90.dp, 60.toDouble()),
    Normal(MR.strings.grid_density_normal, 120.dp, 90.toDouble()),
    Large(MR.strings.grid_density_large, 150.dp, 120.toDouble())
}

@Serializable
enum class GridSpacing(
    val label: StringResource,
    val spacing: Dp,
    val iosSpacing: Double
) {
    None(MR.strings.grid_spacing_none, 0.dp, 0.toDouble()),
    Small(MR.strings.grid_spacing_small, 4.dp, 8.toDouble()),
    Medium(MR.strings.grid_spacing_medium, 8.dp, 16.toDouble()),
    Large(MR.strings.grid_spacing_large, 12.dp, 24.toDouble()),
}

@Serializable
enum class PosterElevation(
    val label: StringResource,
    val elevation: Dp
) {
    None(MR.strings.poster_elevation_none, 0.dp),
    Low(MR.strings.poster_elevation_low, 6.dp),
    Medium(MR.strings.poster_elevation_medium, 12.dp),
    High(MR.strings.poster_elevation_high, 18.dp)
}

@Serializable
enum class PosterRadius(
    val label: StringResource,
    val radius: Dp
) {
    None(MR.strings.poster_radius_none, 0.dp),
    Small(MR.strings.poster_radius_small, 4.dp),
    Medium(MR.strings.poster_radius_medium, 8.dp),
    Large(MR.strings.poster_radius_large, 12.dp)
}

@Serializable
enum class Blur(
    val label: StringResource,
    val radius: Int,
    val iosRadius: Double
) {
    Off(MR.strings.banner_blur_off, 0, 0.toDouble()),
    Low(MR.strings.banner_blur_low, 5, 1.toDouble()),
    Normal(MR.strings.banner_blur_normal, 10, 2.toDouble()),
    High(MR.strings.banner_blur_high, 20, 4.toDouble())
}
