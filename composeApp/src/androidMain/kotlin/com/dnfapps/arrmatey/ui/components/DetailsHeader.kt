package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.RatingItem
import com.dnfapps.arrmatey.arr.api.model.toRatingItems
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.utils.dp
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoPlural
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun DetailsHeader(
    item: ArrMedia,
    type: InstanceType,
    topPadding: Dp
) {
    var detailHeight by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailHeaderBanner(item.getBanner()?.remoteUrl)

        Box(
            modifier = Modifier
                .height(detailHeight.times(2).dp())
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
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
            PosterItem(
                item = item,
                modifier = Modifier.width(150.dp),
                aspectRatio = type.aspectRatio
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClearLogo(item)

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.onGloballyPositioned {
                        detailHeight = it.size.height
                    }
                ) {
                    val ratings = item.ratings?.toRatingItems() ?: emptyList()
                    RatingsSection(ratings)

                    if (item !is Arrtist && item !is Author) {
                        Text(
                            text = listOfNotNull(
                                item.year,
                                item.runtimeString,
                                item.certification
                            ).joinToString(Bullet),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        item.releasedBy?.let { releasedBy ->
                            Text(
                                text = releasedBy,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Text(
                        text = item.genres.joinToString(Bullet),
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

@Composable
fun RatingsSection(
    ratings: List<RatingItem>
) {
    if (ratings.isNotEmpty()) {
        FlowRow(
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ratings.forEach { rating ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rating.icon?.let { icon ->
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ArrOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = rating.score,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsHeader(
    item: RequestMediaDetails
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        DetailHeaderBanner(item.fullBackdropPath)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 170.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PosterItem(
                item = item,
                modifier = Modifier.height(220.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.displayTitle,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 42.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(
                        item.displayDate?.format("MMM d, yyyy"),
                        (item as? MovieDetails)?.runtime?.formatMinutesAsRuntime(),
                        (item as? TvDetails)?.seasons?.let { mokoPlural(MR.plurals.seasons, it.size) },
                        item.getCertification(LocalLocale.current.platformLocale.country)
                    ).joinToString(Bullet),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    item.genres.joinToString(Bullet) { it.name },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}