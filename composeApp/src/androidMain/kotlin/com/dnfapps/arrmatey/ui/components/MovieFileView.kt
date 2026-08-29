package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MovieFileView(
    movie: ArrMovie,
    movieExtraFiles: List<ExtraFile>,
    searchIds: Set<Long>,
    onAutomaticSearch: () -> Unit,
    onDeleteFile: () -> Unit,
    onNavigateToMovieFiles: (ArrMovie) -> Unit,
    onNavigateToMovieReleases: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = mokoString(MR.strings.files),
                style = MaterialTheme.typography.titleLarge,
            )
//            Image(
//                painter = painterResource(MR.images.radarr),
//                contentDescription = null,
//                modifier = Modifier.size(24.dp)
//            )
            Spacer(modifier = Modifier.weight(1f))
            if (movie.movieFile != null || movieExtraFiles.isNotEmpty()) {
                Text(
                    text = mokoString(MR.strings.history),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier =
                        Modifier.clickable {
                            onNavigateToMovieFiles(movie)
                        },
                )
            }
        }
        ReleaseDownloadButtons(
            onInteractiveClicked = {
                onNavigateToMovieReleases(movie.id!!)
            },
            onAutomaticClicked = onAutomaticSearch,
            automaticSearchEnabled = movie.monitored,
            automaticSearchInProgress = searchIds.contains(movie.id),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
        )
        movie.movieFile?.let { file ->
            FileCard(file, onDelete = onDeleteFile)
        }
        movieExtraFiles.takeUnless { it.isEmpty() }?.forEach { extraFile ->
            ExtraFileCard(extraFile)
        }

        if (movie.movieFile == null && movieExtraFiles.isEmpty()) {
            Text(
                text = mokoString(MR.strings.no_files),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
