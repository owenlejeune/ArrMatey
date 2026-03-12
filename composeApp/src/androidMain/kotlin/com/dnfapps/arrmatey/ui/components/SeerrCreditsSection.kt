package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.seerr.api.model.Credits
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData

@Composable
fun SeerrCreditsSection(
    credits: Credits
) {
    Text(
        text = "Cast",
        style = MaterialTheme.typography.titleLarge
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        credits.cast.forEach { castMember ->
            ContainerCard(
                modifier = Modifier
                    .height(200.dp)
                    .width(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = rememberRemoteImageData(castMember.fullProfilePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(88.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = castMember.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = castMember.character,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }

    Text(
        text = "Crew",
        style = MaterialTheme.typography.titleLarge
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        credits.crew.forEach { crewMember ->
            ContainerCard(
                modifier = Modifier
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
                    AsyncImage(
                        model = rememberRemoteImageData(crewMember.fullProfilePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                    )
                    Icon(Icons.Default.Person, contentDescription = null)
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = crewMember.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = crewMember.job,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}