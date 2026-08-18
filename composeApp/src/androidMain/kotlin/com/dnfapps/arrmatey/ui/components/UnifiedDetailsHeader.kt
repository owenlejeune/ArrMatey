package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.RatingItem
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.AspectRatio

@Composable
fun UnifiedDetailsHeader(
    type: InstanceType?,
    topPadding: Dp,
    bannerUrl: String?,
    posterUrl: String?,
    clearLogo: String?,
    ratings: List<RatingItem> = emptyList(),
    year: String?,
    runtimeString: String?,
    certification: String?,
    releasedBy: String?,
    seasonCount: String?,
    genres: List<String>,
    bannerGradientHeight: Dp? = null
) {
    var detailHeight by remember { mutableIntStateOf(0) }
    Box(modifier = Modifier.fillMaxWidth()) {
        DetailHeaderBanner(
            bannerUrl = bannerUrl,
            gradientHeight = bannerGradientHeight ?: detailHeight.times(2).dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPadding)
                .padding(horizontal = 12.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            BasePosterItem(
                model = rememberRemoteImageData(posterUrl),
                modifier = Modifier.width(150.dp),
                aspectRatio = type?.aspectRatio ?: AspectRatio.Poster
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.onGloballyPositioned {
                    detailHeight = it.size.height
                }
            ) {
                clearLogo?.let { clearLogo ->
                    Box(modifier = Modifier.wrapContentSize()) {
                        AsyncImage(
                            model = rememberRemoteImageData(clearLogo),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .height(120.dp)
                                .padding(horizontal = 6.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }

                RatingsSection(ratings)

                listOfNotNull(
                    year, runtimeString, seasonCount, certification
                ).joinToString(Bullet).takeUnless { it.isEmpty() }?.let { info ->
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                releasedBy?.let { releasedBy ->
                    Text(
                        text = releasedBy,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                genres.takeUnless { it.isEmpty() }?.let { genres ->
                    Text(
                        text = genres.joinToString(Bullet),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}