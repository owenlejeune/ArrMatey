package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun EpisodeDetailsHeader(episode: Episode, series: ArrSeries) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailHeaderBanner(episode.getBanner()?.remoteUrl, height = 300.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 170.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            EpisodePosterItem(episode)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = episode.displayTitle,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 1.em,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 38.sp,
                        stepSize = 2.sp
                    )
                )
                Text(
                    text = series.title ?: mokoString(MR.strings.unknown),
                    fontSize = 18.sp
                )
                val statusRow = listOfNotNull(
                    episode.seasonEpLabel,
                    episode.runtimeString,
                    episode.formatAirDateUtc()
                ).joinToString(Bullet)
                Text(
                    text = statusRow,
                    fontSize = 14.sp
                )
            }
        }
    }
}