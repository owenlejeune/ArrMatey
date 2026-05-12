package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoPlural
import java.util.Locale

@Composable
fun DetailsHeader(
    item: ArrMedia,
    type: InstanceType,
    topPadding: Dp
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailHeaderBanner(item.getBanner()?.remoteUrl)
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
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ClearLogo(item)

                if (item !is Arrtist && item !is Author) {
                    Text(
                        text = listOfNotNull(
                            item.year,
                            item.runtimeString,
                            item.certification
                        ).joinToString(Bullet),
                        fontSize = 16.sp
                    )
                    if (item !is Audiobook) {
                        Text(
                            text = listOf(item.releasedBy, item.statusString).joinToString(Bullet),
                            fontSize = 14.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Text(
                    text = item.genres.joinToString(Bullet),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
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
                        item.getCertification(Locale.getDefault().country)
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