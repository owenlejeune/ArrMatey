//
//  SeasonsFilesTabContentView.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeasonsFilesTabContentView: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onSelectQueueItem: (QueueItem) -> Void
    let onConfirmDeleteSeasonNumber: (Int32) -> Void
    let onConfirmDeleteEpisodeId: (Int64) -> Void
    let onConfirmDeleteMovie: () -> Void
    let onEditAlbum: (ArrAlbum) -> Void
    let onConfirmDeleteAlbumId: (Int64) -> Void
    let onNavigateToEpisodeDetails: (ArrSeries, Episode) -> Void
    let onNavigateToSeriesRelease: (Int64?, Int32?, Int64?) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            if !success.queueItems.isEmpty {
                VStack(alignment: .leading, spacing: 12) {
                    Text(MR.strings().activity.localized())
                        .font(.title3.bold())
                    ForEach(success.queueItems, id: \.id) { item in
                        ActivityQueueItem(item: item, onClick: { onSelectQueueItem(item) })
                    }
                }
                .transition(.opacity.combined(with: .move(edge: .top)))
            }

            let arrSeries = success.arrMedia as? ArrSeries
            let seriesId = arrSeries?.id?.int64Value
            SeasonsArea(
                seasons: success.seasons,
                seriesId: seriesId,
                instanceId: success.selectedInstanceId?.int64Value,
                searchIds: viewModel.automaticSearchIds,
                onToggleSeasonMonitor: { viewModel.toggleSeasonMonitored(seasonNumber: $0) },
                onToggleEpisodeMonitor: { viewModel.toggleEpisodeMonitored(episode: $0) },
                onEpisodeAutomaticSearch: { viewModel.performEpisodeAutomaticLookup(episodeId: $0) },
                onSeasonAutomaticSearch: { viewModel.performSeasonAutomaticLookup(seasonNumber: $0) },
                deleteSeasonFiles: onConfirmDeleteSeasonNumber,
                onDeleteEpisodeFile: onConfirmDeleteEpisodeId,
                seasonDeleteInProgress: viewModel.deleteSeasonStatus is OperationStatusInProgress,
                onNavigateToEpisodeDetails: { episode in
                    if let series = arrSeries {
                        onNavigateToEpisodeDetails(series, episode)
                    }
                },
                onNavigateToSeriesRelease: onNavigateToSeriesRelease,
                bazarrDetailsIntegration: success.bazarrDetailsIntegration
            )

            if success.hasArrId {
                ArrLibraryFilesAreaView(
                    success: success,
                    viewModel: viewModel,
                    onConfirmDeleteMovie: onConfirmDeleteMovie,
                    onEditAlbum: onEditAlbum,
                    onConfirmDeleteAlbumId: onConfirmDeleteAlbumId
                )
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
    }
}
