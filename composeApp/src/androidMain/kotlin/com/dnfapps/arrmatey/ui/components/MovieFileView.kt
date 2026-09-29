package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MovieFileView(
    movie: ArrMovie,
    movieExtraFiles: List<ExtraFile>,
    searchIds: Set<Long>,
    onAutomaticSearch: () -> Unit,
    onDeleteFile: () -> Unit,
    onNavigateToMovieReleases: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
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
