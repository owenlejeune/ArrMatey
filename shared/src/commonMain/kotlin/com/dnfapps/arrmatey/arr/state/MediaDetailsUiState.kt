package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.BookSeries
import com.dnfapps.arrmatey.arr.api.model.BookSeriesLink
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile

sealed interface MediaDetailsUiState {
    object Initial: MediaDetailsUiState
    object Loading: MediaDetailsUiState
    data class Error(val message: String?): MediaDetailsUiState
    data class Success(
        val item: ArrMedia,
        val extraFiles: List<ExtraFile> = emptyList(),
        val episodes: List<Episode> = emptyList(),
        val automaticSearchIds: Set<Long> = emptySet(),
        val lastSearchResult: Boolean? = null,
        val albums: List<ArrAlbum> = emptyList(),
        val tracks: Map<Long, List<LidarrTrack>> = emptyMap(),
        val trackFiles: Map<Long, List<LidarrTrackFile>> = emptyMap(),
        val bookFiles: List<BookFile> = emptyList(),
        val bookSeries: List<BookSeries> = emptyList(),
        val books: List<Book> = emptyList()
    ): MediaDetailsUiState
}