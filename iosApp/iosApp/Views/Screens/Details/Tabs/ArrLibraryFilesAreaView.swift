//
//  ArrLibraryFilesAreaView.swift
//  iosApp
//

import SwiftUI
import Shared

struct ArrLibraryFilesAreaView: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onConfirmDeleteMovie: () -> Void
    let onEditAlbum: (ArrAlbum) -> Void
    let onConfirmDeleteAlbumId: (Int64) -> Void

    var body: some View {
        let currentInstanceId = success.selectedInstanceId?.int64Value
        if let movie = success.arrMedia as? ArrMovie {
            VStack(spacing: 12) {
                MovieFilesView(
                    movie: movie,
                    instanceId: currentInstanceId,
                    movieExtraFiles: success.extraFiles,
                    searchIds: viewModel.automaticSearchIds,
                    searchResult: viewModel.lastSearchResult,
                    onAutomaticSearch: { viewModel.performAutomaticLookup() },
                    onDeleteFile: onConfirmDeleteMovie
                )

                if let arrId = movie.id?.int64Value {
                    if success.bazarrDetailsIntegration {
                        BazarrSubtitlesSection(
                            target: BazarrMediaTargetMovie(radarrId: arrId)
                        )
                    }
                }
            }
        } else if let artist = success.arrMedia as? Arrtist {
            ArtistFilesView(
                artist: artist,
                instanceId: currentInstanceId,
                albums: success.albums,
                tracks: success.tracks,
                trackFiles: success.trackFiles,
                searchIds: viewModel.automaticSearchIds,
                onToggleAlbumMonitor: { viewModel.toggleAlbumMonitored(album: $0) },
                onEditAlbum: onEditAlbum,
                onAlbumAutomaticSearch: { viewModel.performAlbumAutomaticLookup(albumId: $0) },
                deleteAlbumFiles: { onConfirmDeleteAlbumId($0.id) },
                albumDeleteInProgress: viewModel.deleteAlbumStatus is OperationStatusInProgress
            )
        } else if let author = success.arrMedia as? Author {
            BooksArea(
                author: author,
                instanceId: currentInstanceId,
                series: success.bookSeries,
                files: success.bookFiles,
                books: success.books,
                searchIds: viewModel.automaticSearchIds,
                onToggleMonitor: { viewModel.toggleBookMonitored(book: $0) },
                onToggleSeriesMonitor: { viewModel.toggleBookSeriesMonitored(books: $0) },
                onAutomaticSearch: { viewModel.performBookAutomaticLookup(bookId: $0) }
            )
        } else if let audiobook = success.arrMedia as? Audiobook {
            AudiobooksArea(
                audiobook: audiobook,
                instanceId: currentInstanceId,
                searchIds: viewModel.automaticSearchIds,
                onAutomaticSearch: { viewModel.performAutomaticLookup() }
            )
        }
    }
}
