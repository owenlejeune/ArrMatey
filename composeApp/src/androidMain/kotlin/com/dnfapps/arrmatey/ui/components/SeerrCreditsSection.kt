package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.seerr.api.model.Credits
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun SeerrCreditsSection(
    credits: Credits,
    onPersonClick: (Long) -> Unit = {}
) {
    Text(
        text = mokoString(MR.strings.cast),
        style = MaterialTheme.typography.titleLarge
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(credits.cast) { castMember ->
            CastCrewItem(
                profilePath = castMember.fullProfilePath,
                name = castMember.name,
                credit = castMember.character,
                modifier = Modifier.clickable { onPersonClick(castMember.id) }
            )
        }
    }

    Text(
        text = mokoString(MR.strings.crew),
        style = MaterialTheme.typography.titleLarge
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
    ) {
        credits.crew.forEach { crewMember ->
            CastCrewItem(
                profilePath = crewMember.fullProfilePath,
                name = crewMember.name,
                credit = crewMember.job,
                modifier = Modifier.clickable { onPersonClick(crewMember.id) }
            )
        }
    }
}

@Composable
fun CastCrewItem(
    profilePath: String?,
    name: String,
    credit: String,
    modifier: Modifier = Modifier
) {
    ContainerCard(
        modifier = modifier
            .height(200.dp)
            .width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            AsyncImage(
                model = rememberRemoteImageData(profilePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = credit,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}