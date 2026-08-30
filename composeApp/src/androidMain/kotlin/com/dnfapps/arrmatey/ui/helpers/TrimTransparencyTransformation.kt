package com.dnfapps.arrmatey.ui.helpers

import android.graphics.Bitmap
import androidx.core.graphics.get
import coil3.size.Size
import coil3.transform.Transformation

class TrimTransparencyTransformation : Transformation() { // Remove the ()
    override val cacheKey: String = "trim_transparency_v1"

    override suspend fun transform(
        input: Bitmap,
        size: Size,
    ): Bitmap {
        // Ensure we have a readable bitmap
        val config = input.config ?: Bitmap.Config.ARGB_8888
        val softwareBitmap = if (input.isMutable) input else input.copy(config, true)

        var firstX = softwareBitmap.width
        var firstY = softwareBitmap.height
        var lastX = 0
        var lastY = 0

        for (y in 0 until softwareBitmap.height) {
            for (x in 0 until softwareBitmap.width) {
                val alpha = (softwareBitmap[x, y] shr 24) and 0xFF
                if (alpha > 5) { // Threshold of 5 to ignore stray compression artifacts
                    if (x < firstX) firstX = x
                    if (y < firstY) firstY = y
                    if (x > lastX) lastX = x
                    if (y > lastY) lastY = y
                }
            }
        }

        if (firstX >= lastX || firstY >= lastY) return input

        val width = lastX - firstX + 1
        val height = lastY - firstY + 1

        return Bitmap.createBitmap(softwareBitmap, firstX, firstY, width, height)
    }
}
