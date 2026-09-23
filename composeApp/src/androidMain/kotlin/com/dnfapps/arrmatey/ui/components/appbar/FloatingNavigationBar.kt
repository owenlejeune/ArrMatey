package com.dnfapps.arrmatey.ui.components.appbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.ui.helpers.LocalIsTabActive
import com.dnfapps.arrmatey.ui.theme.ArrMateyTheme

private const val ANIMATION_DURATION_MILLIS = 300

/**
 * CompositionLocal indicating whether the floating navigation bar is currently in compact mode.
 */
val LocalFloatingNavBarCompact = staticCompositionLocalOf { false }

/**
 * An action to be displayed as a circular floating button next to the [FloatingNavigationBar].
 */
@Immutable
data class FloatingBarAction(
    val icon: @Composable () -> Unit,
    val contentDescription: String? = null,
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val onClick: () -> Unit,
)

/**
 * State holder for the active [FloatingBarAction] displayed alongside the navigation bar.
 */
@Stable
class FloatingBarActionState {
    var currentAction by mutableStateOf<FloatingBarAction?>(null)
}

val LocalFloatingBarActionState = staticCompositionLocalOf { FloatingBarActionState() }

/**
 * Registers a [FloatingBarAction] from a screen to be displayed beside the [FloatingNavigationBar].
 */
@Composable
fun ProvideFloatingBarAction(
    visible: Boolean = true,
    action: FloatingBarAction?,
) {
    val state = LocalFloatingBarActionState.current
    val isTabActive = LocalIsTabActive.current
    val shouldShow = isTabActive && visible && action != null

    DisposableEffect(shouldShow, action) {
        if (shouldShow) {
            state.currentAction = action
        }
        onDispose {
            if (state.currentAction == action) {
                state.currentAction = null
            }
        }
    }
}

/**
 * A floating pill-style navigation bar matching the Google Photos bottom navigation design.
 *
 * It hosts navigation items inside a rounded pill/capsule container with elevation.
 * If an active [FloatingBarAction] is provided (e.g. via [ProvideFloatingBarAction] or the [action] parameter),
 * a circular floating action button is displayed alongside the navigation bar.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingNavigationBar(
    modifier: Modifier = Modifier,
    shape: Shape = FloatingNavigationBarDefaults.shape,
    containerColor: Color = FloatingNavigationBarDefaults.containerColor,
    contentColor: Color = FloatingNavigationBarDefaults.contentColor,
    tonalElevation: Dp = FloatingNavigationBarDefaults.TonalElevation,
    shadowElevation: Dp = FloatingNavigationBarDefaults.ShadowElevation,
    action: FloatingBarAction? = LocalFloatingBarActionState.current.currentAction,
    onLongClick: (() -> Unit)? = null,
    compact: Boolean =
        LocalConfiguration.current.screenWidthDp < 400 ||
            (action != null && LocalConfiguration.current.screenWidthDp < 430),
    content: @Composable RowScope.() -> Unit,
) {
    CompositionLocalProvider(LocalFloatingNavBarCompact provides compact) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = shape,
                color = containerColor,
                contentColor = contentColor,
                tonalElevation = tonalElevation,
                shadowElevation = shadowElevation,
                modifier =
                    if (onLongClick != null) {
                        Modifier.combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                            onLongClick = onLongClick,
                        )
                    } else {
                        Modifier
                    },
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal = if (compact) 4.dp else 6.dp,
                            vertical = if (compact) 4.dp else 6.dp,
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            if (compact) 2.dp else 4.dp,
                            Alignment.CenterHorizontally,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content,
                )
            }

            var lastNonNullAction by remember { mutableStateOf<FloatingBarAction?>(null) }
            if (action != null) {
                lastNonNullAction = action
            }

            AnimatedVisibility(
                visible = action != null,
                enter =
                    scaleIn(
                        initialScale = 0.8f,
                        transformOrigin = TransformOrigin.Center,
                        animationSpec =
                            tween(
                                durationMillis = ANIMATION_DURATION_MILLIS,
                                easing = FastOutSlowInEasing,
                            ),
                    ) +
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis = ANIMATION_DURATION_MILLIS,
                                    easing = FastOutSlowInEasing,
                                ),
                        ),
                exit =
                    scaleOut(
                        targetScale = 0.8f,
                        transformOrigin = TransformOrigin.Center,
                        animationSpec =
                            tween(
                                durationMillis = ANIMATION_DURATION_MILLIS / 2,
                                easing = FastOutSlowInEasing,
                            ),
                    ) +
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis = ANIMATION_DURATION_MILLIS / 2,
                                    easing = FastOutSlowInEasing,
                                ),
                        ),
                label = "FloatingBarActionVisibility",
            ) {
                val activeAction = action ?: lastNonNullAction
                val targetContainerColor = activeAction?.containerColor ?: containerColor
                val targetContentColor = activeAction?.contentColor ?: contentColor

                val animatedContainerColor by animateColorAsState(
                    targetValue = targetContainerColor,
                    animationSpec =
                        tween(
                            durationMillis = ANIMATION_DURATION_MILLIS,
                            easing = FastOutSlowInEasing,
                        ),
                    label = "FloatingBarActionContainerColor",
                )
                val animatedContentColor by animateColorAsState(
                    targetValue = targetContentColor,
                    animationSpec =
                        tween(
                            durationMillis = ANIMATION_DURATION_MILLIS,
                            easing = FastOutSlowInEasing,
                        ),
                    label = "FloatingBarActionContentColor",
                )

                Surface(
                    onClick = { (action ?: lastNonNullAction)?.onClick?.invoke() },
                    shape = CircleShape,
                    color = animatedContainerColor,
                    contentColor = animatedContentColor,
                    tonalElevation = tonalElevation,
                    shadowElevation = shadowElevation,
                    modifier = Modifier.size(if (compact) 48.dp else 56.dp),
                ) {
                    AnimatedContent(
                        targetState = action ?: lastNonNullAction,
                        transitionSpec = {
                            (
                                fadeIn(
                                    animationSpec =
                                        tween(
                                            durationMillis = ANIMATION_DURATION_MILLIS,
                                            easing = FastOutSlowInEasing,
                                        ),
                                ) +
                                    scaleIn(
                                        initialScale = 0.7f,
                                        transformOrigin = TransformOrigin.Center,
                                        animationSpec =
                                            tween(
                                                durationMillis = ANIMATION_DURATION_MILLIS,
                                                easing = FastOutSlowInEasing,
                                            ),
                                    )
                            ).togetherWith(
                                fadeOut(
                                    animationSpec =
                                        tween(
                                            durationMillis = ANIMATION_DURATION_MILLIS / 2,
                                            easing = FastOutSlowInEasing,
                                        ),
                                ) +
                                    scaleOut(
                                        targetScale = 0.7f,
                                        transformOrigin = TransformOrigin.Center,
                                        animationSpec =
                                            tween(
                                                durationMillis = ANIMATION_DURATION_MILLIS / 2,
                                                easing = FastOutSlowInEasing,
                                            ),
                                    ),
                            )
                        },
                        contentAlignment = Alignment.Center,
                        label = "FloatingBarActionIconAnimation",
                    ) { currentAction ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            currentAction?.icon?.invoke()
                        }
                    }
                }
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    colors: FloatingNavigationBarItemColors = FloatingNavigationBarItemDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isCompact = LocalFloatingNavBarCompact.current
    val targetHorizontalPadding =
        when {
            selected -> if (isCompact) 12.dp else 16.dp
            else -> if (isCompact) 8.dp else 12.dp
        }
    val verticalPadding = if (isCompact) 8.dp else 10.dp

    val horizontalPadding by animateDpAsState(
        targetValue = targetHorizontalPadding,
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = FastOutSlowInEasing,
            ),
        label = "FloatingNavItemPadding",
    )

    val containerColor by animateColorAsState(
        targetValue = colors.containerColor(selected = selected, enabled = enabled),
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = FastOutSlowInEasing,
            ),
        label = "FloatingNavItemContainerColor",
    )

    val contentColor by animateColorAsState(
        targetValue = colors.contentColor(selected = selected, enabled = enabled),
        animationSpec =
            tween(
                durationMillis = ANIMATION_DURATION_MILLIS,
                easing = FastOutSlowInEasing,
            ),
        label = "FloatingNavItemContentColor",
    )

    Box(
        modifier =
            modifier
                .semantics {
                    this.selected = selected
                    this.role = Role.Tab
                }.clip(shape)
                .background(containerColor)
                .combinedClickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    onClick = onClick,
                    onLongClick = onLongClick,
                ).padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(
                value =
                    (if (isCompact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge).copy(
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
                        enter =
                            fadeIn(
                                animationSpec =
                                    tween(
                                        durationMillis = ANIMATION_DURATION_MILLIS,
                                        easing = LinearEasing,
                                    ),
                            ) +
                                expandHorizontally(
                                    animationSpec =
                                        tween(
                                            durationMillis = ANIMATION_DURATION_MILLIS,
                                            easing = FastOutSlowInEasing,
                                        ),
                                    expandFrom = Alignment.Start,
                                ),
                        exit =
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis = ANIMATION_DURATION_MILLIS / 2,
                                        easing = LinearEasing,
                                    ),
                            ) +
                                shrinkHorizontally(
                                    animationSpec =
                                        tween(
                                            durationMillis = ANIMATION_DURATION_MILLIS,
                                            easing = FastOutSlowInEasing,
                                        ),
                                    shrinkTowards = Alignment.Start,
                                ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 8.dp))
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
    ): FloatingNavigationBarItemColors =
        FloatingNavigationBarItemColors(
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
    fun containerColor(
        selected: Boolean,
        enabled: Boolean,
    ): Color =
        when {
            !enabled -> Color.Transparent
            selected -> selectedContainerColor
            else -> Color.Transparent
        }

    fun contentColor(
        selected: Boolean,
        enabled: Boolean,
    ): Color =
        when {
            !enabled -> disabledTextColor
            selected -> selectedTextColor
            else -> unselectedTextColor
        }

    fun iconColor(
        selected: Boolean,
        enabled: Boolean,
    ): Color =
        when {
            !enabled -> disabledIconColor
            selected -> selectedIconColor
            else -> unselectedIconColor
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
    val items =
        listOf(
            "Photos" to Icons.Default.Photo,
            "Collections" to Icons.Default.Collections,
            "Create" to Icons.Default.Add,
        )

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
