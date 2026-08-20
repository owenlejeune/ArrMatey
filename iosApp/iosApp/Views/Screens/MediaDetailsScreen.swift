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
    @State private var confirmDeleteAlbum: Int64? = nil
    @State private var confirmDeleteFile: Bool = false
    @State private var editAlbum: ArrAlbum? = nil
    
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
                DeleteMediaSheet(
                    isLoading: viewModel.deleteInProgress,
                    initialAddExclusion: viewModel.preferences.deleteAddExclusion,
                    initialDeleteFiles: viewModel.preferences.deleteDeleteFiles,
                    onConfirm: { addExclusion, deleteFiles in
                        viewModel.delete(addExclusion, deleteFiles)
                    }
                )
                .presentationDetents([.medium])
            }
            .sheet(isPresented: $showEditSheet) {
                sheetContent
            }
            .sheet(item: $editAlbum) { album in
                EditAlbumSheet(album: album, editInProgress: viewModel.editInProgress, onEditAlbum: { updatedAlbum in
                    viewModel.updateAlbum(album: updatedAlbum)
                })
                .presentationDetents([.medium])
            }
            .onChange(of: viewModel.deleteSucceeded) { old, success in 
                if success && !old { 
                    dismiss() 
                } 
            }
            .onChange(of: viewModel.editItemSucceeded) { _, success in
                if success {
                    showEditSheet = false
                    editAlbum = nil
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
            .confirmationAlert(item: $confirmDeleteAlbum) { albumId in
                AlertConfig(
                    title: MR.strings().delete_album.localized(),
                    message: MR.strings().delete_album_confirm.localized(),
                    action: { viewModel.deleteAlbumFiles(albumId) }
                )
            }
            .alert(MR.strings().confirm_delete.localized(), isPresented: $confirmDeleteFile) {
                Button(MR.strings().cancel.localized(), role: .cancel) { }
                Button(MR.strings().confirm.localized(), role: .destructive) {
                    viewModel.deleteMovieFile()
                }
            } message: {
                Text(MR.strings().confirm_delete_file.localized())
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
            
            List {
                Section {
                    MediaDetailsHeader(item: item, type: type)
                        .listRowInsets(EdgeInsets())
                }
                .listRowSeparator(.hidden)
                .listRowBackground(Color.clear)
                
                Section {
                    VStack(alignment: .leading, spacing: 24) {
                        Text(item.title ?? MR.strings().unknown.localized())
                            .font(.system(size: 28, weight: .bold))
                            .frame(maxWidth: .infinity, alignment: .leading)
                        
                        if let airingString = makeAiringString(for: item) {
                            Text(airingString)
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.themePrimary)
                        }
                        
                        ItemDescriptionCard(overview: item.overview)
                    }
                    .padding(.top, 12)
                }
                .listRowInsets(EdgeInsets(top: 0, leading: 24, bottom: 0, trailing: 24))
                .listRowSeparator(.hidden)
                .listRowBackground(Color.clear)
                
                filesArea(for: item, state.extraFiles, state.episodes, state.albums, state.tracks, state.trackFiles, state.bookFiles, state.bookSeries, state.books)
                    .listRowInsets(EdgeInsets(top: 12, leading: 24, bottom: 0, trailing: 24))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                
                let infoItems = buildInfoItems(for: item, qualityProfiles: viewModel.qualityProfiles, tags: viewModel.tags)
                MediaInfoArea(infoItems: infoItems)
                    .listRowInsets(EdgeInsets(top: 12, leading: 24, bottom: 24, trailing: 24))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
            }
            .listStyle(.plain)
            .scrollContentBackground(.hidden)
            .ignoresSafeArea(edges: .top)
        case let state as MediaDetailsUiStateError:
            VStack {
                Text(state.message ?? "")
            }
        default:
            VStack {
                EmptyView()
            }
        }
    }
    
    private func makeAiringString(for item: ArrMedia) -> String? {
        let pattern = "MMMM d, yyyy"
        switch item {
        case let series as ArrSeries:
            if series.status == .continuing {
                if let airing = series.nextAiring?.format(pattern: MR.strings().airing_next_format.localized()) {
                    return MR.strings().airing_next.formatted(args: [airing])
                } else {
                    return nil
                }
            } else { return nil }
        case let movie as ArrMovie:
            if let digitalRelease = movie.digitalRelease, digitalRelease.isTodayOrAfter() {
                return MR.strings().digital_release_on.formatted(args: [digitalRelease.format(pattern: pattern)])
            } else if let physicalRelease = movie.physicalRelease, physicalRelease.isTodayOrAfter() {
                return MR.strings().physical_release_on.formatted(args: [physicalRelease.format(pattern: pattern)])
            } else if let inCinemas = movie.inCinemas, inCinemas.isTodayOrAfter() {
                return MR.strings().in_cinemas_on.formatted(args: [inCinemas.format(pattern: pattern)])
            } else {
                return nil
            }
        case let artist as Arrtist:
            if artist.status == .continuing {
                if let release = artist.nextAlbum?.releaseDate?.format(pattern: pattern) {
                    return "\(MR.strings().next_album.localized()) \(release)"
                } else {
                    return nil
                }
            } else { return nil }
        case let author as Author:
            if author.status == .continuing {
                if let release = author.nextBook?.releaseDate?.format(pattern: pattern) {
                    return "\(MR.strings().next_book.localized()) \(release)"
                } else {
                    return nil
                }
            } else { return nil }
        case let audiobook as Audiobook:
            if let published = audiobook.publishedDate?.ifTodayOrAfter()?.format(pattern: pattern) {
                return "\(MR.strings().release_date.localized()) \(published)"
            } else { return nil }
        case let searchAudiobook as SearchAudiobook:
            return searchAudiobook.releaseDate?.ifTodayOrAfter()?.format(pattern: "MMM d, yyyy")
        case is MockMedia:
            return "Next Airing: Monday"
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
        _ trackFiles: [KotlinLong: [LidarrTrackFile]],
        _ bookFiles: [BookFile],
        _ bookSeries: [BookSeries],
        _ books: [Book]
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
                },
                onDeleteFile: {
                    confirmDeleteFile = true
                }
            )
            BazarrSubtitlesSection(
                target: BazarrMediaTargetMovie(radarrId: id)
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
                onEditAlbum: {
                    editAlbum = $0
                },
                onAlbumAutomaticSearch: {
                    viewModel.performAlbumAutomaticLookup(albumId: $0)
                },
                deleteAlbumFiles: { album in
                    confirmDeleteAlbum = album.id
                },
                albumDeleteInProgress: viewModel.deleteAlbumInProgress
            )
        } else if let author = item as? Author {
            BooksArea(
                author: author,
                series: bookSeries,
                files: bookFiles,
                books: books,
                searchIds: viewModel.automaticSearchIds,
                onToggleMonitor: { viewModel.toggleBookMonitored(book: $0) },
                onToggleSeriesMonitor: { viewModel.toggleBookSeriesMonitored(books: $0) },
                onAutomaticSearch: { viewModel.performBookAutomaticLookup(bookId: $0) }
            )
        } else if let audiobook = item as? Audiobook {
            AudiobooksArea(audiobook: audiobook, searchIds: viewModel.automaticSearchIds, onAutomaticSearch: { viewModel.performAutomaticLookup() })
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
        .presentationDetents([.medium])
            
        case let series as ArrSeries: EditSeriesSheet(item: series, qualityProfiles: viewModel.qualityProfiles, rootFolders: viewModel.rootFolders, tags: viewModel.tags, editInProgress: viewModel.editInProgress, onEditItem: { newSeries, moveFiles in
            viewModel.editItem(newSeries, moveFiles: moveFiles)
        })
        .presentationDetents([.medium])
            
        case let artist as Arrtist: EditArtistSheet(item: artist, qualityProfiles: viewModel.qualityProfiles, rootFolders: viewModel.rootFolders, tags: viewModel.tags, editInProgress: viewModel.editInProgress, onEditItem: { newArtist, moveFiles in
            viewModel.editItem(newArtist, moveFiles: moveFiles)
        })
        .presentationDetents([.medium])
            
        case let author as Author: EditAuthorSheet(item: author, qualityProfiles: viewModel.qualityProfiles, rootFolders: viewModel.rootFolders, tags: viewModel.tags, editInProgress: viewModel.editInProgress, onEditItem: { newAuthor, moveFiles in
            viewModel.editItem(newAuthor, moveFiles: moveFiles)
        })
        .presentationDetents([.medium])
            
        case let audiobook as Audiobook: EditAudiobookSheet(item: audiobook, qualityProfiles: viewModel.qualityProfiles, rootFolders: viewModel.rootFolders, editInProgress: viewModel.editInProgress, onEditItem: { newAudiobook in
            viewModel.editItem(newAudiobook, moveFiles: false)
        })
        .presentationDetents([.medium])
            
        default: EmptyView()
        }
    }
    
    private func buildInfoItems(for item: ArrMedia, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let none = MR.strings().none.localized()
        
        if let series = item as? ArrSeries {
            let qualityLabel = qualityProfiles.first(where: { $0.id == series.qualityProfileId })?.name ?? unknown
            let tagsLabel = series.formatTags(availableTags: tags) ?? none
            let monitorLabel = series.monitorNewItems == .all ? MR.strings().monitored.localized() : MR.strings().unmonitored.localized()
            let seasonFolderLabel = series.seasonFolder ? MR.strings().yes.localized() : MR.strings().no.localized()
            return [
                InfoItem(label: MR.strings().status.localized(), value: series.status.resource.localized()),
                InfoItem(label: MR.strings().series_type.localized(), value: series.seriesType.name),
                InfoItem(label: MR.strings().size_on_disk.localized(), value: series.fileSize.bytesAsFileSizeString()),
                InfoItem(label: MR.strings().root_folder.localized(), value: series.rootFolderPath ?? unknown),
                InfoItem(label: MR.strings().path.localized(), value: series.path ?? unknown),
                InfoItem(label: MR.strings().new_seasons.localized(), value: monitorLabel),
                InfoItem(label: MR.strings().season_folders.localized(), value: seasonFolderLabel),
                InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
                InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
            ]
        } else if let movie = item as? ArrMovie {
            let qualityLabel = qualityProfiles.first(where: { $0.id == movie.qualityProfileId })?.name ?? unknown
            let tagsLabel = movie.formatTags(availableTags: tags) ?? none
            let rootFolderValue = movie.rootFolderPath.isEmpty ? unknown : movie.rootFolderPath
            var info: [InfoItem] = [
                InfoItem(label: MR.strings().status.localized(), value: movie.status.resource.localized()),
                InfoItem(label: MR.strings().minimum_availability.localized(), value: movie.minimumAvailability.name),
                InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue),
                InfoItem(label: MR.strings().path.localized(), value: movie.path ?? unknown)
            ]
            if let inCinemas = movie.inCinemas?.format(pattern: "MMM d, yyyy") {
                info.append(InfoItem(label: MR.strings().in_cinemas.localized(), value: inCinemas))
            }
            if let physicalRelease = movie.physicalRelease?.format(pattern: "MMM d, yyyy") {
                info.append(InfoItem(label: MR.strings().physical_release.localized(), value: physicalRelease))
            }
            if let digitalRelease = movie.digitalRelease?.format(pattern: "MMM d, yyyy") {
                info.append(InfoItem(label: MR.strings().digital_release.localized(), value: digitalRelease))
            }
            info.append(InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel))
            info.append(InfoItem(label: MR.strings().tags.localized(), value: tagsLabel))
            return info
        } else if let artist = item as? Arrtist {
            let qualityLabel = qualityProfiles.first(where: { $0.id == artist.qualityProfileId })?.name ?? unknown
            let tagsLabel = artist.formatTags(availableTags: tags) ?? none
            let monitorLabel = artist.monitorNewItems == .all ? MR.strings().monitored.localized() : MR.strings().unmonitored.localized()
            let rootFolderValue = (artist.rootFolderPath?.isEmpty == false) ? artist.rootFolderPath! : unknown
            return [
                InfoItem(label: MR.strings().status.localized(), value: artist.status.resource.localized()),
                InfoItem(label: MR.strings().size_on_disk.localized(), value: artist.fileSize.bytesAsFileSizeString()),
                InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue),
                InfoItem(label: MR.strings().path.localized(), value: artist.path ?? unknown),
                InfoItem(label: MR.strings().new_albums.localized(), value: monitorLabel),
                InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
                InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
            ]
        } else if let author = item as? Author {
            let qualityLabel = qualityProfiles.first(where: { $0.id == author.qualityProfileId })?.name ?? unknown
            let tagsLabel = author.formatTags(availableTags: tags) ?? none
            let monitorLabel = author.monitorNewItems == .all ? MR.strings().monitored.localized() : MR.strings().unmonitored.localized()
            let rootFolderValue = (author.rootFolderPath?.isEmpty == false) ? author.rootFolderPath! : unknown
            return [
                InfoItem(label: MR.strings().status.localized(), value: author.status.resource.localized()),
                InfoItem(label: MR.strings().size_on_disk.localized(), value: author.fileSize.bytesAsFileSizeString()),
                InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue),
                InfoItem(label: MR.strings().path.localized(), value: author.path ?? unknown),
                InfoItem(label: MR.strings().new_books.localized(), value: monitorLabel),
                InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
                InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
            ]
        } else if let audiobook = item as? Audiobook {
            let authorString = audiobook.authors.joined(separator: " • ")
            let narratorsString = audiobook.narrators.joined(separator: " • ")
            var info: [InfoItem] = [
                InfoItem(label: MR.strings().audiobook_info_authors.localized(), value: authorString),
                InfoItem(label: MR.strings().audiobook_info_narrators.localized(), value: narratorsString),
                InfoItem(label: MR.strings().publisher.localized(), value: audiobook.publisher ?? unknown)
            ]
            if let language = audiobook.language {
                info.append(InfoItem(label: MR.strings().language.localized(), value: language.capitalized))
            }
            info.append(InfoItem(label: MR.strings().size_on_disk.localized(), value: audiobook.fileSize.bytesAsFileSizeString()))
            info.append(InfoItem(label: MR.strings().path.localized(), value: audiobook.path ?? unknown))
            return info
        }
        return []
    }
    
}
