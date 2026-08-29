package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GridBadge(
    count: Int,
    containerColor: Color,
    contentColor: Color,
) {
    if (count <= 0) return
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            color = containerColor,
            shape = CircleShape,
            modifier = Modifier.size(16.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    fontSize = 8.sp,
                    color = contentColor,
                    style =
                        LocalTextStyle.current.copy(
                            platformStyle =
                                PlatformTextStyle(
                                    includeFontPadding = false,
                                ),
                            lineHeight = 8.sp,
                            textAlign = TextAlign.Center,
                        ),
                )
            }
        }
    }
}
