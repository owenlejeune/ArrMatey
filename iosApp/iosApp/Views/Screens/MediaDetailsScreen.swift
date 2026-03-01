//
//  MediaDetailsScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-11.
//

import SwiftUI
import Shared

struct MediaDetailsScreen: View {
    private let id: Int64
    private let type: InstanceType
    
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var navigation: NavigationManager
    
    @ObservedObject private var viewModel: ArrMediaDetailsViewModelS
    
    @State private var showConfirmSheet: Bool = false
    @State private var showEditSheet: Bool = false
    @State private var confirmDeleteSeason: Int32? = nil
    @State private var confirmDeleteAlbum: ArrAlbum? = nil
    
    init(id: Int64, type: InstanceType) {
        self.id = id
        self.type = type
        self.viewModel = ArrMediaDetailsViewModelS(id: id, type: type)
    }
    
    var body: some View {
        contentForState()
            .toolbar { toolbarContent }
            .task { viewModel.refreshDetails() }
            .sheet(isPresented: $showConfirmSheet) {
                DeleteMediaSheet(isLoading: viewModel.deleteInProgress, onConfirm: { addExclusion, deleteFiles in
                    viewModel.delete(addExclusion, deleteFiles)
                })
                .presentationDetents([.fraction(0.33)])
                .presentationBackground(.ultraThinMaterial)
            }
            .sheet(isPresented: $showEditSheet) {
                sheetContent
            }
            .onChange(of: viewModel.deleteSucceeded) { _, success in if success { dismiss() } }
            .onChange(of: viewModel.editItemSucceeded) { _, success in
                if success {
                    showEditSheet = false
                    viewModel.refreshDetails()
                }
            }
            .confirmationAlert(item: $confirmDeleteSeason) { season in
                AlertConfig(
                    title: MR.strings().delete_season.formatted(args: [season]),
                    message: MR.strings().delete_season_confirm.formatted(args: [season]),
                    action: { viewModel.deleteSeasonFiles(season) }
                )
            }
            .confirmationAlert(item: $confirmDeleteAlbum) { album in
                AlertConfig(
                    title: MR.strings().delete_album.localized(),
                    message: MR.strings().delete_album_confirm.formatted(args: [album.title ?? MR.strings().unknown.localized()]),
                    action: { viewModel.deleteAlbumFiles(album.id) }
                )
            }
    }
    
    @ViewBuilder
    private func contentForState() -> some View {
        switch viewModel.uiState {
        case is MediaDetailsUiStateInitial:
            ZStack {
                EmptyView()
            }
        case is MediaDetailsUiStateLoading:
            ZStack {
                ProgressView()
                    .progressViewStyle(.circular)
            }
        case let state as MediaDetailsUiStateSuccess:
            let item = state.item
            
            ScrollView {
                VStack(alignment: .leading, spacing: 0){
                    MediaDetailsHeader(item: item, type: type)
                        .frame(height: 400)
                    
                    VStack(alignment: .leading, spacing: 12) {
                        if let airingString = makeAiringString(for: item) {
                            Text(airingString)
                                .font(.system(size: 20, weight: .medium))
                                .foregroundColor(.themePrimary)
                        }
                        
                        ItemDescriptionCard(overview: item.overview)
                        
                        filesArea(for: item, state.extraFiles, state.episodes, state.albums, state.tracks, state.trackFiles)
                        
                        MediaInfoArea(item: item, qualityProfiles: viewModel.qualityProfiles, tags: viewModel.tags)
                        
                        Spacer()
                            .frame(height: 12)
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 12)
                }
            }
            .ignoresSafeArea(edges: .top)
        case _ as MediaDetailsUiStateError:
            VStack{}
        default:
            VStack {
                EmptyView()
            }
        }
    }
    
    private func makeAiringString(for item: ArrMedia) -> String? {
        switch item {
        case let series as ArrSeries:
            if series.status == .continuing {
                if let airing = series.nextAiring?.format(pattern: "HH:mm MMMM d, yyyy") {
                    return "\(MR.strings().airing_next.localized()) \(airing)"
                } else {
                    return MR.strings().continuing_unknown.localized()
                }
            } else { return nil }
        case let movie as ArrMovie:
            if let inCinemas = movie.inCinemas?.format(pattern: "HH:mm MMMM d, yyyy"), movie.digitalRelease == nil, movie.physicalRelease == nil {
                return "\(MR.strings().in_cinemas.localized()) \(inCinemas)"
            } else {
                return nil
            }
        default: return nil
        }
    }
    
    @ViewBuilder
    private func filesArea(
        for item: ArrMedia,
        _ extraFiles: [ExtraFile],
        _ episodes: [Episode],
        _ albums: [ArrAlbum],
        _ tracks: [KotlinLong: [LidarrTrack]],
        _ trackFiles: [KotlinLong: [LidarrTrackFile]]
    ) -> some View {
        if let series = item as? ArrSeries {
            SeriesFilesView(
                series: series,
                episodes: episodes,
                searchIds: viewModel.automaticSearchIds,
                searchResult: viewModel.lastSearchResult,
                onToggleSeasonMonitor: { sn in
                    viewModel.toggleSeasonMonitor(seasonNumber: sn)
                },
                onToggleEpisodeMonitor: { ep in
                    viewModel.toggleEpisodeMonitor(episode: ep)
                },
                onEpisodeAutomaticSearch: { id in
                    viewModel.performEpisodeAutomaticLookup(episodeId: id)
                },
                onSeasonAutomaticSearch: { sn in
                    viewModel.performSeasonAutomaticLookup(seasonNumber: sn)
                },
                onDeleteSeasonFiles: { seasonNumber in
                    confirmDeleteSeason = seasonNumber
                },
                seasonDeleteInProgress: viewModel.deleteSeasonInProgress
            )
        } else if let movie = item as? ArrMovie {
            MovieFilesView(
                movie: movie,
                movieExtraFiles: extraFiles,
                searchIds: viewModel.automaticSearchIds,
                searchResult: viewModel.lastSearchResult,
                onAutomaticSearch: {
                    viewModel.performAutomaticLookup()
                }
            )
        } else if let artist = item as? Arrtist {
            ArtistFilesView(
                artist: artist,
                albums: albums,
                tracks: tracks,
                trackFiles: trackFiles,
                searchIds: viewModel.automaticSearchIds,
                onToggleAlbumMonitor: {
                    viewModel.toggleAlbumMonitored(album: $0)
                },
                onAlbumAutomaticSearch: {
                    viewModel.performAlbumAutomaticLookup(albumId: $0)
                },
                deleteAlbumFiles: {
                    confirmDeleteAlbum = $0
                },
                albumDeleteInProgress: viewModel.deleteAlbumInProgress
            )
        } else {
            EmptyView()
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .primaryAction) {
            Image(systemName: viewModel.isMonitored ? "bookmark.fill" : "bookmark")
                .imageScale(.medium)
                .onTapGesture {
                    viewModel.toggleMonitor()
                }
        }
        ToolbarItem(placement: .primaryAction) {
            Menu {
                Section {
                    Button(MR.strings().refresh.localized(), systemImage: "arrow.clockwise") {
                        viewModel.performRefresh()
                    }
                    if type.includeTopLevelAutomaticSearchOption {
                        Button(MR.strings().search_monitored.localized(), systemImage: "magnifyingglass") {
                            viewModel.performAutomaticLookup()
                        }
                        .disabled(!viewModel.isMonitored)
                    }
                }
                Section {
                    Button(MR.strings().edit.localized(), systemImage: "pencil") {
                        showEditSheet = true
                    }
                    Button(MR.strings().delete.localized(), systemImage: "trash") {
                        showConfirmSheet = true
                    }
                    .tint(.red)
                }
            } label: {
                Image(systemName: "ellipsis")
                    .imageScale(.medium)
            }
        }
    }
    
    @ViewBuilder
    private var sheetContent: some View {
        switch viewModel.item {
        case nil: EmptyView()
            
        case let movie as ArrMovie: EditMovieSheet(item: movie, qualityProfiles: viewModel.qualityProfiles, rootFolders: viewModel.rootFolders, tags: viewModel.tags, editInProgress: viewModel.editInProgress, onEditItem: { newMovie, moveFiles in
            viewModel.editItem(newMovie, moveFiles: moveFiles)
        })
        .presentationBackground(.ultraThinMaterial)
            
        case let series as ArrSeries: EditSeriesSheet(item: series, qualityProfiles: viewModel.qualityProfiles, rootFolders: viewModel.rootFolders, tags: viewModel.tags, editInProgress: viewModel.editInProgress, onEditItem: { newSeries, moveFiles in
            viewModel.editItem(newSeries, moveFiles: moveFiles)
        })
        .presentationBackground(.ultraThinMaterial)
            
        case let artist as Arrtist: EditArtistSheet(item: artist, qualityProfiles: viewModel.qualityProfiles, rootFolders: viewModel.rootFolders, tags: viewModel.tags, editInProgress: viewModel.editInProgress, onEditItem: { newSeries, moveFiles in
            viewModel.editItem(newSeries, moveFiles: moveFiles)
        })
            
        default: EmptyView()
        }
    }
    
}
