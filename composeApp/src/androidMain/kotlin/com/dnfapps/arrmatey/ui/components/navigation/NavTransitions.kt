package com.dnfapps.arrmatey.ui.components.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

private const val DURATION_MS = 300
private const val PEEK_FRACTION = 4 // incoming/outgoing peek offset = width / 4

// New content slides in from the right, old content slides partway left with a fade
fun forwardSlideTransform(): ContentTransform =
    slideInHorizontally(tween(DURATION_MS)) { it } +
        fadeIn(tween(DURATION_MS)) togetherWith
        slideOutHorizontally(tween(DURATION_MS)) { -it / PEEK_FRACTION } +
        fadeOut(tween(DURATION_MS))

// Non-predictive back (button / on-screen up): old content slides out to the right
fun popSlideTransform(): ContentTransform =
    slideInHorizontally(tween(DURATION_MS)) { -it / PEEK_FRACTION } +
        fadeIn(tween(DURATION_MS)) togetherWith
        slideOutHorizontally(tween(DURATION_MS)) { it } +
        fadeOut(tween(DURATION_MS))

// Predictive-back pop: same shape as popSlideTransform, driven by gesture progress
fun predictivePopSlideTransform(): ContentTransform = popSlideTransform()

