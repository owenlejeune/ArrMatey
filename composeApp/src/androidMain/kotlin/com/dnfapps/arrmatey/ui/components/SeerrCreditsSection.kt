package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.seerr.api.model.Credits
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun SeerrCreditsSection(
    credits: Credits,
    modifier: Modifier = Modifier,
    onPersonClick: (Long) -> Unit = {},
) {
    val cast = remember(credits) { credits.groupedCast }
    val crew = remember(credits) { credits.groupedCrew }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        if (cast.isNotEmpty()) {
            Text(
                text = mokoString(MR.strings.cast),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                items(cast, key = { it.id }) { castMember ->
                    CastCrewItem(
                        profilePath = castMember.fullProfilePath,
                        name = castMember.name,
                        credit = castMember.character,
                        modifier = Modifier.clickable { onPersonClick(castMember.id) },
                    )
                }
            }
        }

        if (cast.isNotEmpty() && crew.isNotEmpty()) {
            Spacer(modifier = Modifier.height(0.dp))
        }

        if (crew.isNotEmpty()) {
            Text(
                text = mokoString(MR.strings.crew),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                items(crew, key = { it.id }) { crewMember ->
                    CastCrewItem(
                        profilePath = crewMember.fullProfilePath,
                        name = crewMember.name,
                        credit = crewMember.job,
                        modifier = Modifier.clickable { onPersonClick(crewMember.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun CastCrewItem(
    profilePath: String?,
    name: String,
    credit: String,
    modifier: Modifier = Modifier,
) {
    ContainerCard(
        modifier =
            modifier
                .width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PersonProfileImage(profilePath)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                minLines = 2,
                maxLines = 2,
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart,
            ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.labelMedium,
                    minLines = 3,
                    maxLines = 3,
                )
                Text(
                    text = credit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun PersonProfileImage(profilePath: String?) {
    Box(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .size(88.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.Person, contentDescription = null)
        AsyncImage(
            model = rememberRemoteImageData(profilePath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(88.dp)
                    .clip(CircleShape),
        )
    }
}
