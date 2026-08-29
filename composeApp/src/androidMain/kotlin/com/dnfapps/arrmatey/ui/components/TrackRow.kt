package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.extensions.formatAsDuration
import com.dnfapps.arrmatey.extensions.toOneDecimal
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun TrackRow(
    track: LidarrTrack,
    trackFile: LidarrTrackFile?,
) {
    val file = track.trackFile ?: trackFile
    val mediaInfo = file?.mediaInfo

    val mediaInfoStatus =
        remember(mediaInfo) {
            listOfNotNull(
                mediaInfo?.audioCodec,
                mediaInfo?.audioChannels?.toOneDecimal(),
                mediaInfo?.audioBitrate,
                mediaInfo?.audioSampleRate,
                mediaInfo?.audioBits,
            ).joinToString(" - ")
        }
    val mediaInfoStatusCondensed =
        remember(mediaInfo) {
            listOfNotNull(
                mediaInfo?.audioCodec,
                mediaInfo?.audioBits,
            ).joinToString(" ")
                .takeUnless { it.isEmpty() }
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            val titleString =
                buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 16.sp)) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("${track.absoluteTrackNumber}.")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                            append(track.title)
                        }
                    }
                }
            Text(
                text = titleString,
                lineHeight = 16.sp,
                overflow = TextOverflow.MiddleEllipsis,
                maxLines = 1,
            )

            val (statusText, statusColor) =
                when {
                    file == null -> mokoString(MR.strings.missing) to MaterialTheme.colorScheme.error
                    mediaInfo == null -> mokoString(MR.strings.no_media_info) to MaterialTheme.colorScheme.error
                    else -> mediaInfoStatus to MaterialTheme.colorScheme.tertiary
                }

            val styledStatusText =
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary,
                        ),
                    ) {
                        append(track.duration.formatAsDuration())
                    }
                    bullet()
                    withStyle(
                        SpanStyle(
                            fontSize = 14.sp,
                            color = statusColor,
                            fontStyle = FontStyle.Italic,
                        ),
                    ) {
                        append(statusText)
                    }
                }
            Text(styledStatusText)
        }

        mediaInfoStatusCondensed?.let { staus ->
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = staus,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        } ?: Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
    }
}
