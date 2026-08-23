package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.dp
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun EpisodeDetailsHeader(
    episode: Episode,
    series: ArrSeries
) {
    var detailHeight by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailHeaderBanner(
            bannerUrl = episode.getBanner()?.remoteUrl,
            gradientHeight = detailHeight.times(2).dp()
        )

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
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.onGloballyPositioned {
                    detailHeight = it.size.height
                }
            ) {
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