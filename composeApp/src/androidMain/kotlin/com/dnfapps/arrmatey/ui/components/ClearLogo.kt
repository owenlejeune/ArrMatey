package com.dnfapps.arrmatey.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ClearLogo(
    item: ArrMedia
) {
    item.getClearLogo()?.remoteUrl?.let { logo ->
        val isLightTheme = !isSystemInDarkTheme()
        Box(
            modifier = Modifier
                .wrapContentSize()
                .drawBehind {
                    if (isLightTheme) {
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = Color.Black.copy(alpha = 0.4f)
                                asFrameworkPaint().maskFilter =
                                    BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
                            }
                            canvas.drawRoundRect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = size.height,
                                radiusX = 16.dp.toPx(),
                                radiusY = 16.dp.toPx(),
                                paint = paint
                            )
                        }
                    }
                }
        ) {
            AsyncImage(
                model = rememberRemoteImageData(logo),
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 6.dp),
                contentScale = ContentScale.Fit,
            )
        }
    } ?: run {
        Text(
            text = item.title ?: mokoString(MR.strings.unknown),
            fontWeight = FontWeight.Medium,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 1.em,
            maxLines = 6,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 16.sp,
                maxFontSize = 38.sp,
                stepSize = 2.sp
            )
        )
    }
}