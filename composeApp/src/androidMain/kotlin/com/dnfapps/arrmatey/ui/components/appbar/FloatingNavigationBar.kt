package com.dnfapps.arrmatey.ui.components.appbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.ui.theme.ArrMateyTheme

private const val AnimationDurationMillis = 300

/**
 * A floating pill-style navigation bar matching the Google Photos bottom navigation design.
 *
 * It hosts navigation items inside a rounded pill/capsule container with elevation.
 * Active items display both an icon and label with an accent pill background,
 * while inactive items display only their icon.
 */
@Composable
fun FloatingNavigationBar(
    modifier: Modifier = Modifier,
    shape: Shape = FloatingNavigationBarDefaults.shape,
    containerColor: Color = FloatingNavigationBarDefaults.containerColor,
    contentColor: Color = FloatingNavigationBarDefaults.contentColor,
    tonalElevation: Dp = FloatingNavigationBarDefaults.TonalElevation,
    shadowElevation: Dp = FloatingNavigationBarDefaults.ShadowElevation,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        var maxRowWidth by remember { mutableIntStateOf(0) }

        Layout(
            content = {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content,
                )
            },
        ) { measurables, constraints ->
            val placeable = measurables.first().measure(constraints.copy(minWidth = 0))
            if (placeable.width > maxRowWidth) {
                maxRowWidth = placeable.width
            }
            val targetWidth = maxOf(maxRowWidth, placeable.width)
            layout(targetWidth, placeable.height) {
                val x = (targetWidth - placeable.width) / 2
                placeable.placeRelative(x, 0)
            }
        }
    }
}

/**
 * An item for use inside a [FloatingNavigationBar].
 *
 * When selected, the item expands smoothly to show an accent pill container containing
 * both the [icon] and the animated [label]. When unselected, only the [icon] is displayed.
 */
@Composable
fun FloatingNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    colors: FloatingNavigationBarItemColors = FloatingNavigationBarItemDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val horizontalPadding by animateDpAsState(
        targetValue = if (selected) 16.dp else 12.dp,
        animationSpec = tween(
            durationMillis = AnimationDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "FloatingNavItemPadding",
    )

    val containerColor by animateColorAsState(
        targetValue = colors.containerColor(selected = selected, enabled = enabled),
        animationSpec = tween(
            durationMillis = AnimationDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "FloatingNavItemContainerColor",
    )

    val contentColor by animateColorAsState(
        targetValue = colors.contentColor(selected = selected, enabled = enabled),
        animationSpec = tween(
            durationMillis = AnimationDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "FloatingNavItemContentColor",
    )

    Box(
        modifier = modifier
            .semantics {
                this.selected = selected
                this.role = Role.Tab
            }
            .clip(shape)
            .background(containerColor)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(
                value = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        icon()
                    }

                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationDurationMillis,
                                easing = LinearEasing,
                            ),
                        ) + expandHorizontally(
                            animationSpec = tween(
                                durationMillis = AnimationDurationMillis,
                                easing = FastOutSlowInEasing,
                            ),
                            expandFrom = Alignment.Start,
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = AnimationDurationMillis / 2,
                                easing = LinearEasing,
                            ),
                        ) + shrinkHorizontally(
                            animationSpec = tween(
                                durationMillis = AnimationDurationMillis,
                                easing = FastOutSlowInEasing,
                            ),
                            shrinkTowards = Alignment.Start,
                        ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(8.dp))
                            label()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Default values and colors used by [FloatingNavigationBar].
 */
object FloatingNavigationBarDefaults {
    val shape: Shape = CircleShape

    val TonalElevation: Dp = 3.dp

    val ShadowElevation: Dp = 6.dp

    val containerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.surfaceContainer

    val contentColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.onSurface
}

/**
 * Default color values for [FloatingNavigationBarItem].
 */
object FloatingNavigationBarItemDefaults {
    @Composable
    fun colors(
        selectedIconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedTextColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        unselectedIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): FloatingNavigationBarItemColors = FloatingNavigationBarItemColors(
        selectedIconColor = selectedIconColor,
        selectedTextColor = selectedTextColor,
        selectedContainerColor = selectedContainerColor,
        unselectedIconColor = unselectedIconColor,
        unselectedTextColor = unselectedTextColor,
        disabledIconColor = disabledIconColor,
        disabledTextColor = disabledTextColor,
    )
}

/**
 * Color configuration for [FloatingNavigationBarItem].
 */
@Immutable
class FloatingNavigationBarItemColors(
    val selectedIconColor: Color,
    val selectedTextColor: Color,
    val selectedContainerColor: Color,
    val unselectedIconColor: Color,
    val unselectedTextColor: Color,
    val disabledIconColor: Color,
    val disabledTextColor: Color,
) {
    fun containerColor(selected: Boolean, enabled: Boolean): Color {
        return when {
            !enabled -> Color.Transparent
            selected -> selectedContainerColor
            else -> Color.Transparent
        }
    }

    fun contentColor(selected: Boolean, enabled: Boolean): Color {
        return when {
            !enabled -> disabledTextColor
            selected -> selectedTextColor
            else -> unselectedTextColor
        }
    }

    fun iconColor(selected: Boolean, enabled: Boolean): Color {
        return when {
            !enabled -> disabledIconColor
            selected -> selectedIconColor
            else -> unselectedIconColor
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is FloatingNavigationBarItemColors) return false

        if (selectedIconColor != other.selectedIconColor) return false
        if (selectedTextColor != other.selectedTextColor) return false
        if (selectedContainerColor != other.selectedContainerColor) return false
        if (unselectedIconColor != other.unselectedIconColor) return false
        if (unselectedTextColor != other.unselectedTextColor) return false
        if (disabledIconColor != other.disabledIconColor) return false
        if (disabledTextColor != other.disabledTextColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selectedIconColor.hashCode()
        result = 31 * result + selectedTextColor.hashCode()
        result = 31 * result + selectedContainerColor.hashCode()
        result = 31 * result + unselectedIconColor.hashCode()
        result = 31 * result + unselectedTextColor.hashCode()
        result = 31 * result + disabledIconColor.hashCode()
        result = 31 * result + disabledTextColor.hashCode()
        return result
    }
}

@Preview(name = "Floating Navigation Bar - Photos Selected")
@Composable
private fun FloatingNavigationBarPreview() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val items = listOf("Photos" to Icons.Default.Photo, "Collections" to Icons.Default.Collections, "Create" to Icons.Default.Add)

    ArrMateyTheme {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            FloatingNavigationBar {
                items.forEachIndexed { index, (label, icon) ->
                    FloatingNavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                    )
                }
            }
        }
    }
}
