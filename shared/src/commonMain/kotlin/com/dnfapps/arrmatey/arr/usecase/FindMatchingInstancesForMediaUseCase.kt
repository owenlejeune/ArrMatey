package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.arr.api.model.LidarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.ListenarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RadarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.ReadarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.asSuccess

sealed interface ResolvedMediaDestination {
    val instance: Instance

    data class Movie(
        override val instance: Instance,
        val movieId: Long?,
        val tmdbId: Long?
    ) : ResolvedMediaDestination

    data class Series(
        override val instance: Instance,
        val seriesId: Long?,
        val tmdbId: Long?,
        val tvdbId: Long?
    ) : ResolvedMediaDestination

    data class EpisodeDetails(
        override val instance: Instance,
        val series: ArrSeries,
        val episode: Episode
    ) : ResolvedMediaDestination

    data class Artist(
        override val instance: Instance,
        val artistId: Long?,
        val albumId: Long? = null
    ) : ResolvedMediaDestination

    data class BookDetails(
        override val instance: Instance,
        val author: Author,
        val book: Book
    ) : ResolvedMediaDestination

    data class AudiobookDetails(
        override val instance: Instance,
        val audiobookId: Long?
    ) : ResolvedMediaDestination
}

class FindMatchingInstancesForMediaUseCase(
    private val instanceManager: InstanceManager
) {
    suspend fun resolve(item: CalendarItem): List<ResolvedMediaDestination> {
        return when (item) {
            is ArrMovie -> resolveMovie(item, item.instanceId)
            is EpisodeGroup -> resolveSeries(item.first.series, item.instanceId)
            is Episode -> resolveEpisode(item, item.instanceId)
            is ArrAlbum -> resolveAlbum(item, item.instanceId)
            is Book -> resolveBook(item, item.instanceId)
            is Audiobook -> resolveAudiobook(item, item.instanceId)
        }
    }

    suspend fun resolve(item: QueueItem): List<ResolvedMediaDestination> {
        return when (item) {
            is RadarrQueueItem -> {
                val movie = item.movie
                if (movie != null) {
                    resolveMovie(movie, item.instanceId)
                } else {
                    resolveMovieById(item.movieId, item.instanceId)
                }
            }
            is SonarrQueueItem -> {
                val episode = item.episode
                resolveEpisode(episode, item.instanceId, item.series)
            }
            is LidarrQueueItem -> {
                val album = item.album
                if (album != null) {
                    resolveAlbum(album, item.instanceId)
                } else {
                    resolveAlbumById(item.albumId, item.artistId, item.instanceId)
                }
            }
            is ReadarrQueueItem -> {
                val book = item.book
                val author = item.author
                if (book != null) {
                    resolveBook(book, item.instanceId, author)
                } else {
                    resolveBookById(item.bookId, item.authorId, item.instanceId)
                }
            }
            is ListenarrQueueItem -> {
                resolveAudiobookById(item.mediaId, item.instanceId)
            }
        }
    }

    private suspend fun resolveMovieById(movieId: Long?, fallbackInstanceId: Long?): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Radarr).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val matches = mutableListOf<ResolvedMediaDestination>()
        repos.forEach { repo ->
            val lib = repo.library.value
            val items = if (lib is NetworkResult.Success) lib.data.filterIsInstance<ArrMovie>() else emptyList()
            val found = items.firstOrNull { m -> movieId != null && m.id == movieId }
            if (found != null) {
                matches.add(ResolvedMediaDestination.Movie(
                    instance = repo.instance,
                    movieId = found.id,
                    tmdbId = found.tmdbId.takeIf { it > 0 }
                ))
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(ResolvedMediaDestination.Movie(
                    instance = repo.instance,
                    movieId = movieId,
                    tmdbId = null
                ))
            }
        }

        return matches
    }

    private suspend fun resolveMovie(movie: ArrMovie, fallbackInstanceId: Long?): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Radarr).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val matches = mutableListOf<ResolvedMediaDestination>()
        repos.forEach { repo ->
            val lib = repo.library.value
            val items = if (lib is NetworkResult.Success) lib.data.filterIsInstance<ArrMovie>() else {
                if (repo.instance.id == fallbackInstanceId) {
                    listOf(movie)
                } else {
                    val lookupQuery = if (movie.tmdbId > 0) "tmdb:${movie.tmdbId}" else movie.title
                    if (!lookupQuery.isNullOrBlank()) {
                        (repo.directLookup(lookupQuery) as? NetworkResult.Success)?.data?.filterIsInstance<ArrMovie>() ?: emptyList()
                    } else emptyList()
                }
            }

            val found = items.firstOrNull { m ->
                (movie.tmdbId > 0 && m.tmdbId == movie.tmdbId) ||
                (movie.id != null && m.id == movie.id && repo.instance.id == fallbackInstanceId) ||
                (!movie.title.isNullOrBlank() && m.title?.equals(movie.title, ignoreCase = true) == true)
            }

            if (found != null && ((found.id != null && found.id != 0L) || repo.instance.id == fallbackInstanceId)) {
                matches.add(
                    ResolvedMediaDestination.Movie(
                        instance = repo.instance,
                        movieId = found.id ?: movie.id,
                        tmdbId = if (found.tmdbId > 0) found.tmdbId else movie.tmdbId.takeIf { it > 0 }
                    )
                )
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(
                    ResolvedMediaDestination.Movie(
                        instance = repo.instance,
                        movieId = movie.id,
                        tmdbId = movie.tmdbId.takeIf { it > 0 }
                    )
                )
            }
        }

        return matches
    }

    private fun resolveSeries(series: ArrSeries?, fallbackInstanceId: Long?): List<ResolvedMediaDestination> {
        val seriesId = series?.id
        val tmdbId = series?.tmdbId
        val tvdbId = series?.tvdbId

        val repos = instanceManager.getRepositoriesByType(InstanceType.Sonarr).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val matches = mutableListOf<ResolvedMediaDestination>()
        repos.forEach { repo ->
            val lib = repo.library.value
            val items = if (lib is NetworkResult.Success) lib.data.filterIsInstance<ArrSeries>() else emptyList()
            val found = items.firstOrNull { s ->
                (seriesId != null && s.id == seriesId) ||
                (tvdbId != null && tvdbId > 0 && s.tvdbId == tvdbId) ||
                (tmdbId != null && tmdbId > 0 && s.tmdbId == tmdbId)
            }
            if (found != null) {
                matches.add(ResolvedMediaDestination.Series(
                    instance = repo.instance,
                    seriesId = found.id,
                    tmdbId = found.tmdbId?.takeIf { it > 0L },
                    tvdbId = found.tvdbId.takeIf { it > 0 }
                ))
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(ResolvedMediaDestination.Series(
                    instance = repo.instance,
                    seriesId = seriesId,
                    tmdbId = tmdbId,
                    tvdbId = tvdbId
                ))
            }
        }

        return matches
    }

    private suspend fun resolveEpisode(episode: Episode?, fallbackInstanceId: Long?, providedSeries: ArrSeries? = null): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Sonarr).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val series = providedSeries ?: episode?.series
        val matches = mutableListOf<ResolvedMediaDestination>()

        repos.forEach { repo ->
            val lib = repo.library.value
            val seriesList = if (lib is NetworkResult.Success) lib.data.filterIsInstance<ArrSeries>() else {
                if (repo.instance.id == fallbackInstanceId && series != null) {
                    listOf(series)
                } else {
                    val lookupQuery = if ((series?.tvdbId ?: 0) > 0) "tvdb:${series?.tvdbId}" else if ((series?.tmdbId ?: 0) > 0) "tmdb:${series?.tmdbId}" else series?.title
                    if (!lookupQuery.isNullOrBlank()) {
                        (repo.directLookup(lookupQuery) as? NetworkResult.Success)?.data?.filterIsInstance<ArrSeries>() ?: emptyList()
                    } else emptyList()
                }
            }

            val foundSeries = seriesList.firstOrNull { s ->
                (series?.tvdbId != null && series.tvdbId > 0 && s.tvdbId == series.tvdbId) ||
                (series?.tmdbId != null && series.tmdbId > 0 && s.tmdbId == series.tmdbId) ||
                (series?.id != null && s.id == series.id && repo.instance.id == fallbackInstanceId) ||
                (!series?.title.isNullOrBlank() && s.title?.equals(series?.title, ignoreCase = true) == true)
            }

            if (foundSeries != null && ((foundSeries.id != null && foundSeries.id != 0L) || repo.instance.id == fallbackInstanceId)) {
                if (episode != null) {
                    val effectiveSeriesId = foundSeries.id ?: series?.id ?: episode.seriesId
                    val epMap = repo.episodes.value[effectiveSeriesId]
                    val matchingEp = epMap?.firstOrNull { ep ->
                        (episode.id > 0 && ep.id == episode.id) ||
                        (ep.seasonNumber == episode.seasonNumber && ep.episodeNumber == episode.episodeNumber)
                    } ?: episode.copy(seriesId = effectiveSeriesId, series = foundSeries)

                    matches.add(
                        ResolvedMediaDestination.EpisodeDetails(
                            instance = repo.instance,
                            series = foundSeries,
                            episode = matchingEp
                        )
                    )
                }
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null && series != null && episode != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(
                    ResolvedMediaDestination.EpisodeDetails(
                        instance = repo.instance,
                        series = series,
                        episode = episode
                    )
                )
            }
        }

        return matches
    }

    private suspend fun resolveAlbumById(albumId: Long?, artistId: Long?, fallbackInstanceId: Long?): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Lidarr).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val matches = mutableListOf<ResolvedMediaDestination>()
        repos.forEach { repo ->
            val lib = repo.library.value
            val artistList = if (lib is NetworkResult.Success) lib.data.filterIsInstance<Arrtist>() else emptyList()
            val found = artistList.firstOrNull { a -> artistId != null && a.id == artistId }
            if (found != null) {
                matches.add(ResolvedMediaDestination.Artist(
                    instance = repo.instance,
                    artistId = found.id ?: artistId,
                    albumId = albumId
                ))
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(ResolvedMediaDestination.Artist(
                    instance = repo.instance,
                    artistId = artistId,
                    albumId = albumId
                ))
            }
        }

        return matches
    }

    private suspend fun resolveAlbum(album: ArrAlbum, fallbackInstanceId: Long?): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Lidarr).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val artist = album.artist
        val matches = mutableListOf<ResolvedMediaDestination>()

        repos.forEach { repo ->
            val lib = repo.library.value
            val artistList = if (lib is NetworkResult.Success) lib.data.filterIsInstance<Arrtist>() else {
                if (repo.instance.id == fallbackInstanceId && artist != null) {
                    listOf(artist)
                } else {
                    val lookupQuery = artist?.foreignArtistId ?: artist?.title
                    if (!lookupQuery.isNullOrBlank()) {
                        (repo.directLookup(lookupQuery) as? NetworkResult.Success)?.data?.filterIsInstance<Arrtist>() ?: emptyList()
                    } else emptyList()
                }
            }

            val foundArtist = artistList.firstOrNull { a ->
                (!artist?.foreignArtistId.isNullOrBlank() && a.foreignArtistId == artist?.foreignArtistId) ||
                (album.artistId != null && a.id == album.artistId && repo.instance.id == fallbackInstanceId) ||
                (!artist?.title.isNullOrBlank() && a.title?.equals(artist?.title, ignoreCase = true) == true)
            }

            if (foundArtist != null && ((foundArtist.id != null && foundArtist.id != 0L) || repo.instance.id == fallbackInstanceId)) {
                matches.add(
                    ResolvedMediaDestination.Artist(
                        instance = repo.instance,
                        artistId = foundArtist.id ?: album.artistId ?: artist?.id,
                        albumId = album.id
                    )
                )
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(
                    ResolvedMediaDestination.Artist(
                        instance = repo.instance,
                        artistId = album.artistId ?: artist?.id,
                        albumId = album.id
                    )
                )
            }
        }

        return matches
    }

    private suspend fun resolveBookById(bookId: Long?, authorId: Long?, fallbackInstanceId: Long?): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Booksehelf).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val matches = mutableListOf<ResolvedMediaDestination>()
        repos.forEach { repo ->
            val lib = repo.library.value
            val authorList = if (lib is NetworkResult.Success) lib.data.filterIsInstance<Author>() else emptyList()
            val foundAuthor = authorList.firstOrNull { a -> authorId != null && a.id == authorId }
            if (foundAuthor != null) {
                val bookList = repo.booksLibrary.value.takeIf { it.isNotEmpty() }
                    ?: (repo.library.value as? NetworkResult.Success)?.data?.filterIsInstance<Book>()
                    ?: emptyList()
                val foundBook = bookList.firstOrNull { b -> bookId != null && b.id == bookId }
                if (foundBook != null) {
                    matches.add(ResolvedMediaDestination.BookDetails(
                        instance = repo.instance,
                        author = foundAuthor,
                        book = foundBook
                    ))
                }
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                // Can't construct minimal Author/Book objects here without required fields
                // Just find what we have in cache from the fallback instance
                val lib = repo.library.value
                val authorList = if (lib is NetworkResult.Success) lib.data.filterIsInstance<Author>() else emptyList()
                val author = authorList.firstOrNull { a -> authorId != null && a.id == authorId }
                val bookList = repo.booksLibrary.value.takeIf { it.isNotEmpty() }
                    ?: (lib as? NetworkResult.Success)?.data?.filterIsInstance<Book>()
                    ?: emptyList()
                val book = bookList.firstOrNull { b -> bookId != null && b.id == bookId }
                if (author != null && book != null) {
                    matches.add(ResolvedMediaDestination.BookDetails(
                        instance = repo.instance,
                        author = author,
                        book = book
                    ))
                }
            }
        }

        return matches
    }

    private suspend fun resolveBook(book: Book, fallbackInstanceId: Long?, providedAuthor: Author? = null): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Booksehelf).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val matches = mutableListOf<ResolvedMediaDestination>()

        repos.forEach { repo ->
            val lib = repo.library.value
            val authorList = if (lib is NetworkResult.Success) lib.data.filterIsInstance<Author>() else {
                if (repo.instance.id == fallbackInstanceId && providedAuthor != null) {
                    listOf(providedAuthor)
                } else {
                    val lookupQuery = book.authorTitle ?: providedAuthor?.title
                    if (!lookupQuery.isNullOrBlank()) {
                        (repo.directLookup(lookupQuery) as? NetworkResult.Success)?.data?.filterIsInstance<Author>() ?: emptyList()
                    } else emptyList()
                }
            }

            val foundAuthor = authorList.firstOrNull { a ->
                (providedAuthor != null && !providedAuthor.foreignAuthorId.isNullOrBlank() && a.foreignAuthorId == providedAuthor.foreignAuthorId) ||
                (book.authorId != null && a.id == book.authorId && repo.instance.id == fallbackInstanceId) ||
                (!book.authorTitle.isNullOrBlank() && a.title?.equals(book.authorTitle, ignoreCase = true) == true) ||
                (!providedAuthor?.title.isNullOrBlank() && a.title?.equals(providedAuthor?.title, ignoreCase = true) == true)
            }

            if (foundAuthor != null && ((foundAuthor.id != null && foundAuthor.id != 0L) || repo.instance.id == fallbackInstanceId)) {
                val authorId = foundAuthor.id ?: book.authorId ?: 0L
                val bookList = repo.booksLibrary.value.takeIf { it.isNotEmpty() }
                    ?: (repo.library.value as? NetworkResult.Success)?.data?.filterIsInstance<Book>()
                    ?: emptyList()
                val foundBook = bookList.firstOrNull { b ->
                    (b.id == book.id && repo.instance.id == fallbackInstanceId) ||
                    (!book.foreignBookId.isNullOrBlank() && b.foreignBookId == book.foreignBookId) ||
                    (b.authorId == authorId && b.title.equals(book.title, ignoreCase = true))
                } ?: book.copy(authorId = authorId)

                matches.add(
                    ResolvedMediaDestination.BookDetails(
                        instance = repo.instance,
                        author = foundAuthor,
                        book = foundBook
                    )
                )
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null && providedAuthor != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(
                    ResolvedMediaDestination.BookDetails(
                        instance = repo.instance,
                        author = providedAuthor,
                        book = book
                    )
                )
            }
        }

        return matches
    }

    private suspend fun resolveAudiobookById(audiobookId: Long?, fallbackInstanceId: Long?): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Listenarr).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val matches = mutableListOf<ResolvedMediaDestination>()
        repos.forEach { repo ->
            val lib = repo.library.value
            val audiobookList = if (lib is NetworkResult.Success) lib.data.filterIsInstance<Audiobook>() else emptyList()
            val found = audiobookList.firstOrNull { a -> audiobookId != null && a.id == audiobookId }
            if (found != null) {
                matches.add(ResolvedMediaDestination.AudiobookDetails(
                    instance = repo.instance,
                    audiobookId = found.id ?: audiobookId
                ))
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(ResolvedMediaDestination.AudiobookDetails(
                    instance = repo.instance,
                    audiobookId = audiobookId
                ))
            }
        }

        return matches
    }

    private suspend fun resolveAudiobook(audiobook: Audiobook, fallbackInstanceId: Long?): List<ResolvedMediaDestination> {
        val repos = instanceManager.getRepositoriesByType(InstanceType.Listenarr).filterIsInstance<ArrInstanceRepository>()
        if (repos.isEmpty()) return emptyList()

        val matches = mutableListOf<ResolvedMediaDestination>()

        repos.forEach { repo ->
            val lib = repo.library.value
            val audiobookList = if (lib is NetworkResult.Success) lib.data.filterIsInstance<Audiobook>() else {
                if (repo.instance.id == fallbackInstanceId) {
                    listOf(audiobook)
                } else {
                    val lookupQuery = audiobook.asin ?: audiobook.title
                    if (!lookupQuery.isNullOrBlank()) {
                        (repo.directLookup(lookupQuery) as? NetworkResult.Success)?.data?.filterIsInstance<Audiobook>() ?: emptyList()
                    } else emptyList()
                }
            }

            val foundAudiobook = audiobookList.firstOrNull { a ->
                (!audiobook.asin.isNullOrBlank() && a.asin == audiobook.asin) ||
                (audiobook.id != null && a.id == audiobook.id && repo.instance.id == fallbackInstanceId) ||
                (!audiobook.title.isNullOrBlank() && a.title?.equals(audiobook.title, ignoreCase = true) == true)
            }

            if (foundAudiobook != null && ((foundAudiobook.id != null && foundAudiobook.id != 0L) || repo.instance.id == fallbackInstanceId)) {
                matches.add(
                    ResolvedMediaDestination.AudiobookDetails(
                        instance = repo.instance,
                        audiobookId = foundAudiobook.id ?: audiobook.id
                    )
                )
            }
        }

        if (matches.isEmpty() && fallbackInstanceId != null) {
            val repo = repos.firstOrNull { it.instance.id == fallbackInstanceId } ?: repos.firstOrNull()
            if (repo != null) {
                matches.add(
                    ResolvedMediaDestination.AudiobookDetails(
                        instance = repo.instance,
                        audiobookId = audiobook.id
                    )
                )
            }
        }

        return matches
    }
}
