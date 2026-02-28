package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ConfirmableButton(
    isConfirming: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,//.scale(scale),
        colors = colors,
        enabled = enabled
    ) {
        AnimatedContent(
            targetState = isConfirming,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) +
                        scaleIn(initialScale = 0.9f, animationSpec = tween(300))) togetherWith
                        (fadeOut(animationSpec = tween(200)) +
                                scaleOut(targetScale = 0.9f, animationSpec = tween(200)))
            },
            label = "ConfirmableButtonContent"
        ) { confirm ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (confirm) {
                    Icon(Icons.Default.Warning, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Are you sure?")
                } else {
                    content()
                }
            }
        }
    }
}