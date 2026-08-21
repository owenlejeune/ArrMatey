//
//  UnifiedMediaDetailsScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct UnifiedMediaDetailsScreen: View {
    @StateObject private var viewModel: UnifiedMediaDetailsViewModelS
    @Environment(\.dismiss) private var dismiss
    @Environment(\.openURL) private var openURL
    @EnvironmentObject private var navigationManager: NavigationManager
    
    @State private var showConfirmSheet = false
    @State private var showEditSheet = false
    @State private var showAddSheet = false
    @State private var editAlbum: ArrAlbum? = nil
    
    @State private var confirmDeleteSeasonNumber: Int32? = nil
    @State private var confirmDeleteAlbumId: Int64? = nil
    @State private var confirmDeleteMovie = false
    @State private var confirmRemoveFromService = false
    @State private var confirmClearData = false
    @State private var selectedQueueItem: QueueItem? = nil
    
    @State private var toastMessage: String? = nil
    
    private var removeServiceName: String {
        viewModel.buttonState.serviceName ?? (viewModel.resolvedRequestType == RequestType.movie ? "Radarr" : "Sonarr")
    }
    
    init(
        arrId: Int64? = nil,
        tmdbId: Int64? = nil,
        tvdbId: Int64? = nil,
        instanceType: InstanceType? = nil,
        requestType: RequestType? = nil
    ) {
        _viewModel = StateObject(wrappedValue: UnifiedMediaDetailsViewModelS(
            arrId: arrId,
            tmdbId: tmdbId,
            tvdbId: tvdbId,
            instanceType: instanceType,
            requestType: requestType
        ))
    }
    
    var body: some View {
        ZStack {
            contentForState()
            toastOverlay
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { toolbarContent }
        .task { viewModel.refresh() }
        .modifier(UnifiedMediaDetailsSheetsModifier(
            viewModel: viewModel,
            showEditSheet: $showEditSheet,
            showAddSheet: $showAddSheet,
            showConfirmSheet: $showConfirmSheet,
            editAlbum: $editAlbum,
            selectedQueueItem: $selectedQueueItem,
            screen: self
        ))
        .modifier(UnifiedMediaDetailsAlertsModifier(
            viewModel: viewModel,
            confirmDeleteMovie: $confirmDeleteMovie,
            confirmRemoveFromService: $confirmRemoveFromService,
            confirmClearData: $confirmClearData,
            confirmDeleteSeasonNumber: $confirmDeleteSeasonNumber,
            confirmDeleteAlbumId: $confirmDeleteAlbumId,
            removeServiceName: removeServiceName,
            screen: self
        ))
        .modifier(UnifiedMediaDetailsEventsModifier(
            viewModel: viewModel,
            screen: self
        ))
    }
}

// MARK: - State Rendering
extension UnifiedMediaDetailsScreen {
    @ViewBuilder
    private func contentForState() -> some View {
        switch viewModel.uiState {
        case is UnifiedMediaDetailsUiStateInitial, is UnifiedMediaDetailsUiStateLoading:
            ProgressView()
                .progressViewStyle(.circular)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        case let errorState as UnifiedMediaDetailsUiStateError:
            VStack(spacing: 16) {
                Text(errorState.message ?? MR.strings().error.localized())
                    .foregroundColor(.secondary)
                Button(MR.strings().retry.localized()) {
                    viewModel.refresh()
                }
            }
        case let success as UnifiedMediaDetailsUiStateSuccess:
            successView(success)
        default:
            EmptyView()
        }
    }
    
    @ViewBuilder
    private func successView(_ success: UnifiedMediaDetailsUiStateSuccess) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                UnifiedMediaDetailsHeader(success: success, type: viewModel.resolvedInstanceType)
                
                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(success.displayTitle ?? MR.strings().unknown.localized())
                            .font(.system(size: 28, weight: .bold))
                            .frame(maxWidth: .infinity, alignment: .leading)
                        
                        if let tagline = success.tagline, !tagline.isEmpty {
                            Text(tagline)
                                .font(.system(size: 16))
                                .italic()
                                .foregroundColor(.secondary)
                        }
                        
                        if let airingString = success.upcomingDateString {
                            Text(airingString)
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.themePrimary)
                        }
                    }
                    
                    UnifiedMediaDetailsActions(
                        buttonState: viewModel.buttonState,
                        onWatch: { url in
                            if let urlObj = URL(string: url) { openURL(urlObj) }
                        },
                        onWatchTrailer: { url in
                            if let urlObj = URL(string: url) { openURL(urlObj) }
                        },
                        onRequest: { viewModel.showRequestSheet(is4k: false) },
                        onRequest4k: { viewModel.showRequestSheet(is4k: true) },
                        onViewRequest: { viewModel.showViewRequestSheet() },
                        onApproveRequest: { viewModel.showViewRequestSheet() },
                        onDeclineRequest: { viewModel.showViewRequestSheet() },
                        onManage: { viewModel.showViewRequestSheet() }
                    )
                    
                    if let overview = success.overview {
                        ItemDescriptionCard(overview: overview)
                    }
                    
                    if !success.queueItems.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text(MR.strings().activity.localized())
                                .font(.title3.bold())
                            ForEach(success.queueItems, id: \.id) { item in
                                ActivityQueueItem(item: item, onClick: { selectedQueueItem = item })
                            }
                        }
                        .transition(.opacity.combined(with: .move(edge: .top)))
                    }
                    
                    seasonsArea(success)
                    
                    if success.hasArrId {
                        arrLibraryFilesArea(success)
                            .transition(.opacity.combined(with: .move(edge: .top)))
                    }
                    
                    if let credits = success.seerrMedia?.credits {
                        creditsSection(credits)
                    }
                    
                    unifiedInfoArea(success)
                    
                    if !success.keywords.isEmpty {
                        keywordsSection(success.keywords)
                    }
                }
                .padding(.top, 12)
                .padding(.horizontal, 24)
                .padding(.bottom, 24)
                .animation(.easeInOut(duration: 0.3), value: success.selectedInstanceId?.int64Value)
            }
        }
        .ignoresSafeArea(edges: .top)
        .refreshable {
            viewModel.refresh()
        }
    }
}

// MARK: - Components and Sections
extension UnifiedMediaDetailsScreen {
    
    @ViewBuilder
    private func seasonsArea(_ success: UnifiedMediaDetailsUiStateSuccess) -> some View {
        let arrSeries = success.arrMedia as? ArrSeries
        let seriesId = arrSeries?.id?.int64Value
        SeasonsArea(
            seasons: success.seasons,
            seriesId: seriesId,
            searchIds: viewModel.automaticSearchIds,
            onToggleSeasonMonitor: { viewModel.toggleSeasonMonitored(seasonNumber: $0) },
            onToggleEpisodeMonitor: { viewModel.toggleEpisodeMonitored(episode: $0) },
            onEpisodeAutomaticSearch: { viewModel.performEpisodeAutomaticLookup(episodeId: $0) },
            onSeasonAutomaticSearch: { viewModel.performSeasonAutomaticLookup(seasonNumber: $0) },
            deleteSeasonFiles: { confirmDeleteSeasonNumber = $0 },
            seasonDeleteInProgress: viewModel.deleteSeasonStatus is NetworkingOperationStatusInProgress,
            onNavigateToEpisodeDetails: { episode in
                if let series = arrSeries {
                    navigationManager.go(to: .episodeDetails(series.toJson(), episode.toJson()), of: .sonarr)
                }
            },
            onNavigateToSeriesRelease: { sId, seasonNum in
                if let sId = sId {
                    let route: MediaRoute = .seriesReleases(seriesId: sId, seasonNumber: seasonNum, episodeId: nil)
                    navigationManager.go(to: route, of: .sonarr)
                }
            }
        )
    }


    /// Shows file-level content (movie files, albums, books, audiobooks).
    /// Only called when success.hasArrId is true, matching Android AnimatedVisibility(visible = state.hasArrId).
    @ViewBuilder
    private func arrLibraryFilesArea(_ success: UnifiedMediaDetailsUiStateSuccess) -> some View {
        if let movie = success.arrMedia as? ArrMovie {
            VStack(spacing: 12) {
                MovieFilesView(
                    movie: movie,
                    movieExtraFiles: success.extraFiles,
                    searchIds: viewModel.automaticSearchIds,
                    searchResult: viewModel.lastSearchResult,
                    onAutomaticSearch: { viewModel.performAutomaticLookup() },
                    onDeleteFile: { confirmDeleteMovie = true }
                )
                
                if let arrId = movie.id?.int64Value {
                    BazarrSubtitlesSection(
                        target: BazarrMediaTargetMovie(radarrId: arrId)
                    )
                }
            }
        } else if let artist = success.arrMedia as? Arrtist {
            ArtistFilesView(
                artist: artist,
                albums: success.albums,
                tracks: success.tracks,
                trackFiles: success.trackFiles,
                searchIds: viewModel.automaticSearchIds,
                onToggleAlbumMonitor: { viewModel.toggleAlbumMonitored(album: $0) },
                onEditAlbum: { editAlbum = $0 },
                onAlbumAutomaticSearch: { viewModel.performAlbumAutomaticLookup(albumId: $0) },
                deleteAlbumFiles: { confirmDeleteAlbumId = $0.id },
                albumDeleteInProgress: viewModel.deleteAlbumStatus is NetworkingOperationStatusInProgress
            )
        } else if let author = success.arrMedia as? Author {
            BooksArea(
                author: author,
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
                searchIds: viewModel.automaticSearchIds,
                onAutomaticSearch: { viewModel.performAutomaticLookup() }
            )
        }
    }
    
    @ViewBuilder
    private func creditsSection(_ credits: Credits) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            if !credits.cast.isEmpty {
                Text(MR.strings().cast.localized())
                    .font(.title3.bold())
                
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(credits.cast.prefix(20), id: \.id) { member in
                            CastMemberView(member: member) { personId in
                                navigationManager.goToSeerrDetails(tmdbId: personId, requestType: .person)
                            }
                        }
                    }
                }
            }
            
            if !credits.crew.isEmpty {
                Text(MR.strings().crew.localized())
                    .font(.title3.bold())
                
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(credits.crew.prefix(20), id: \.creditId) { member in
                            CrewMemberView(member: member) { personId in
                                navigationManager.goToSeerrDetails(tmdbId: personId, requestType: .person)
                            }
                        }
                    }
                }
            }
        }
    }
    
    @ViewBuilder
    private func unifiedInfoArea(_ success: UnifiedMediaDetailsUiStateSuccess) -> some View {
        let arrItems = buildArrInfoItems(success: success)
        let seerrItems = buildSeerrInfoItems(success: success)
        let arrInstance = success.availableInstances.first(where: { $0.id == success.selectedInstanceId?.int64Value }) ?? viewModel.activeInstance
        let seerrInstance = viewModel.activeSeerrInstance
        if !arrItems.isEmpty || !seerrItems.isEmpty {
            MediaInfoArea(
                arrItems: arrItems,
                seerrItems: seerrItems,
                arrInstance: arrInstance,
                seerrInstance: seerrInstance
            )
        }
    }
    
    @ViewBuilder
    private func keywordsSection(_ keywords: [Keyword]) -> some View {
        let rowCount = min(3, max(1, keywords.count))
        let rows = (0..<rowCount).map { rowIndex in
            keywords.enumerated().filter { $0.offset % rowCount == rowIndex }.map { $0.element }
        }
        
        ScrollView(.horizontal, showsIndicators: false) {
            VStack(alignment: .leading, spacing: 8) {
                ForEach(0..<rows.count, id: \.self) { rowIndex in
                    HStack(spacing: 8) {
                        ForEach(rows[rowIndex], id: \.id) { keyword in
                            Text(keyword.name)
                                .font(.system(size: 14))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(
                                    Capsule()
                                        .stroke(Color.secondary.opacity(0.3), lineWidth: 1)
                                )
                        }
                    }
                }
            }
        }
    }
    
    private func buildArrInfoItems(success: UnifiedMediaDetailsUiStateSuccess) -> [InfoItem] {
        if success.hasArrId, let arrMedia = success.arrMedia {
            return buildArrInfoItems(
                arrMedia: arrMedia,
                qualityProfiles: viewModel.qualityProfiles,
                tags: viewModel.tags
            )
        }
        return []
    }
    
    private func buildSeerrInfoItems(success: UnifiedMediaDetailsUiStateSuccess) -> [InfoItem] {
        var items: [InfoItem] = []
        if let seerrMedia = success.seerrMedia {
            items.append(InfoItem(label: MR.strings().status.localized(), value: seerrMedia.status))
            
            if let movie = seerrMedia as? MovieDetails {
                if let releaseDate = movie.releaseDate {
                    items.append(InfoItem(label: MR.strings().release_date.localized(), value: releaseDate.format(pattern: "MMM dd, yyyy")))
                }
            }
            
            let countries = seerrMedia.productionCountries.map { $0.name }.joined(separator: "\n")
            if !countries.isEmpty {
                items.append(InfoItem(label: MR.strings().production_countries.localized(), value: countries))
            }
            
            let studios = seerrMedia.productionCompanies.map { $0.name }.joined(separator: "\n")
            if !studios.isEmpty {
                items.append(InfoItem(label: MR.strings().studios.localized(), value: studios))
            }
        }
        return items
    }
    
    private func buildUnifiedInfoItems(success: UnifiedMediaDetailsUiStateSuccess) -> [InfoItem] {
        buildArrInfoItems(success: success) + buildSeerrInfoItems(success: success)
    }
    
    private func buildArrInfoItems(arrMedia: ArrMedia, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        if let series = arrMedia as? ArrSeries {
            return seriesInfoItems(series, qualityProfiles: qualityProfiles, tags: tags)
        } else if let movie = arrMedia as? ArrMovie {
            return movieInfoItems(movie, qualityProfiles: qualityProfiles, tags: tags)
        } else if let artist = arrMedia as? Arrtist {
            return artistInfoItems(artist, qualityProfiles: qualityProfiles, tags: tags)
        } else if let author = arrMedia as? Author {
            return authorInfoItems(author, qualityProfiles: qualityProfiles, tags: tags)
        } else if let audiobook = arrMedia as? Audiobook {
            return audiobookInfoItems(audiobook)
        }
        return []
    }
    
    private func seriesInfoItems(_ series: ArrSeries, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let qualityLabel = qualityProfiles.first(where: { $0.id == series.qualityProfileId })?.name ?? unknown
        let tagsLabel = series.formatTags(availableTags: tags) ?? MR.strings().none.localized()
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
    }
    
    private func movieInfoItems(_ movie: ArrMovie, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let qualityLabel = qualityProfiles.first(where: { $0.id == movie.qualityProfileId })?.name ?? unknown
        let tagsLabel = movie.formatTags(availableTags: tags) ?? MR.strings().none.localized()
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
    }
    
    private func artistInfoItems(_ artist: Arrtist, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let qualityLabel = qualityProfiles.first(where: { $0.id == artist.qualityProfileId })?.name ?? unknown
        let tagsLabel = artist.formatTags(availableTags: tags) ?? MR.strings().none.localized()
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
    }
    
    private func authorInfoItems(_ author: Author, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let qualityLabel = qualityProfiles.first(where: { $0.id == author.qualityProfileId })?.name ?? unknown
        let tagsLabel = author.formatTags(availableTags: tags) ?? MR.strings().none.localized()
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
    }
    
    private func audiobookInfoItems(_ audiobook: Audiobook) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
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
}


// MARK: - Modals and Sheets
extension UnifiedMediaDetailsScreen {
    @ViewBuilder
    fileprivate var addSheetContent: some View {
        if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess {
            switch success.arrMedia {
            case let series as ArrSeries:
                AddSeriesForm(
                    series: series,
                    addItemStatus: viewModel.editStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    tags: viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags,
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { item, searchOnAdd in
                        viewModel.smartAdd(item: item, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        showAddSheet = false
                    },
                    onDismiss: { showAddSheet = false },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            case let movie as ArrMovie:
                AddMovieForm(
                    movie: movie,
                    addItemStatus: viewModel.editStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    tags: viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags,
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { item, searchOnAdd in
                        viewModel.smartAdd(item: item, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        showAddSheet = false
                    },
                    onDismiss: { showAddSheet = false },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            case let artist as Arrtist:
                AddArtistForm(
                    artist: artist,
                    addItemStatus: viewModel.editStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    tags: viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags,
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { item, searchOnAdd in
                        viewModel.smartAdd(item: item, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        showAddSheet = false
                    },
                    onDismiss: { showAddSheet = false },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            case let author as Author:
                AddAuthorForm(
                    author: author,
                    addItemStatus: viewModel.editStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    tags: viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags,
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { item, searchOnAdd in
                        viewModel.smartAdd(item: item, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        showAddSheet = false
                    },
                    onDismiss: { showAddSheet = false },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            case let audiobook as Audiobook:
                let searchAudiobook = SearchAudiobookKt.createSearchAudiobook(audiobook: audiobook)
                AddAudiobookForm(
                    audiobook: searchAudiobook,
                    addItemStatus: viewModel.editStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    relativePath: "",
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { item, searchOnAdd in
                        viewModel.smartAdd(item: item, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        showAddSheet = false
                    },
                    onDismiss: { showAddSheet = false },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            default: EmptyView()
            }
        } else {
            EmptyView()
        }
    }
    
    @ViewBuilder
    fileprivate var editSheetContent: some View {
        if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess {
            switch success.arrMedia {
            case let movie as ArrMovie:
                EditMovieSheet(
                    item: movie,
                    qualityProfiles: viewModel.qualityProfiles,
                    rootFolders: viewModel.rootFolders,
                    tags: viewModel.tags,
                    editInProgress: viewModel.editStatus is NetworkingOperationStatusInProgress,
                    onEditItem: { newMovie, moveFiles in
                        viewModel.editItem(item: newMovie, moveFiles: moveFiles)
                    }
                )
                .presentationDetents([.medium])
            case let series as ArrSeries:
                EditSeriesSheet(
                    item: series,
                    qualityProfiles: viewModel.qualityProfiles,
                    rootFolders: viewModel.rootFolders,
                    tags: viewModel.tags,
                    editInProgress: viewModel.editStatus is NetworkingOperationStatusInProgress,
                    onEditItem: { newSeries, moveFiles in
                        viewModel.editItem(item: newSeries, moveFiles: moveFiles)
                    }
                )
                .presentationDetents([.medium])
            case let artist as Arrtist:
                EditArtistSheet(
                    item: artist,
                    qualityProfiles: viewModel.qualityProfiles,
                    rootFolders: viewModel.rootFolders,
                    tags: viewModel.tags,
                    editInProgress: viewModel.editStatus is NetworkingOperationStatusInProgress,
                    onEditItem: { newArtist, moveFiles in
                        viewModel.editItem(item: newArtist, moveFiles: moveFiles)
                    }
                )
                .presentationDetents([.medium])
            case let author as Author:
                EditAuthorSheet(
                    item: author,
                    qualityProfiles: viewModel.qualityProfiles,
                    rootFolders: viewModel.rootFolders,
                    tags: viewModel.tags,
                    editInProgress: viewModel.editStatus is NetworkingOperationStatusInProgress,
                    onEditItem: { newAuthor, moveFiles in
                        viewModel.editItem(item: newAuthor, moveFiles: moveFiles)
                    }
                )
                .presentationDetents([.medium])
            case let audiobook as Audiobook:
                EditAudiobookSheet(
                    item: audiobook,
                    qualityProfiles: viewModel.qualityProfiles,
                    rootFolders: viewModel.rootFolders,
                    editInProgress: viewModel.editStatus is NetworkingOperationStatusInProgress,
                    onEditItem: { newAudiobook in
                        viewModel.editItem(item: newAudiobook, moveFiles: false)
                    }
                )
                .presentationDetents([.medium])
            default: EmptyView()
            }
        } else {
            EmptyView()
        }
    }
    
    @ViewBuilder
    fileprivate var confirmSheetContent: some View {
        DeleteMediaSheet(
            isLoading: viewModel.deleteStatus is NetworkingOperationStatusInProgress,
            initialAddExclusion: viewModel.preferences.deleteAddExclusion,
            initialDeleteFiles: viewModel.preferences.deleteDeleteFiles,
            onConfirm: { addExclusion, deleteFiles in
                viewModel.deleteMedia(deleteFiles: deleteFiles, addImportExclusion: addExclusion)
            }
        )
    }
    
    @ViewBuilder
    fileprivate func queueItemSheetContent(_ wrapper: IdentifiableQueueItem) -> some View {
        QueueItemInfoSheet(
            item: wrapper.item,
            deleteInProgress: viewModel.removeQueueItemStatus is NetworkingOperationStatusInProgress,
            onDelete: { remove, block, skip in
                viewModel.removeQueueItem(item: wrapper.item, removeFromClient: remove, addToBlocklist: block, skipRedownload: skip)
                selectedQueueItem = nil
            }
        )
        .presentationDetents([.medium])
    }
    
    @ViewBuilder
    fileprivate func editAlbumSheetContent(_ album: ArrAlbum) -> some View {
        EditAlbumSheet(album: album, editInProgress: viewModel.editStatus is NetworkingOperationStatusInProgress, onEditAlbum: { updatedAlbum in
            viewModel.updateAlbum(album: updatedAlbum)
        })
    }
    
    @ViewBuilder
    fileprivate var requestSheetContent: some View {
        if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess, let seerrMedia = success.seerrMedia {
            SeerrRequestSheet(
                details: seerrMedia,
                serviceDetails: viewModel.serviceDetails,
                currentUser: viewModel.currentUser,
                users: viewModel.users,
                is4k: viewModel.isRequest4k,
                onDismiss: { viewModel.hideRequestSheet() },
                onSubmit: { profileId, rootFolder, langId, seasons, is4k, userId in
                    viewModel.submitRequest(
                        profileId: profileId,
                        rootFolder: rootFolder,
                        languageProfileId: langId,
                        seasons: seasons,
                        is4k: is4k,
                        userId: userId
                    )
                }
            )
        }
    }
    
    @ViewBuilder
    fileprivate var reportIssueSheetContent: some View {
        SeerrReportIssueSheet(viewModel: viewModel, onDismiss: { viewModel.hideReportIssueSheet() })
    }
    
    @ViewBuilder
    fileprivate var viewRequestSheetContent: some View {
        if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess, let seerrMedia = success.seerrMedia {
            SeerrViewRequestSheet(details: seerrMedia, viewModel: viewModel, onDismissRequest: { viewModel.hideViewRequestSheet() })
        }
    }
    
    @ViewBuilder
    fileprivate var deleteMovieAlertContent: some View {
        Button(MR.strings().cancel.localized(), role: .cancel) { }
        Button(MR.strings().confirm.localized(), role: .destructive) {
            viewModel.deleteMovieFile()
        }
    }
    
    @ViewBuilder
    fileprivate var deleteSeasonDialogContent: some View {
        Button(MR.strings().confirm.localized(), role: .destructive) {
            if let season = confirmDeleteSeasonNumber {
                viewModel.deleteSeasonFiles(seasonNumber: season)
            }
            confirmDeleteSeasonNumber = nil
        }
        Button(MR.strings().cancel.localized(), role: .cancel) {
            confirmDeleteSeasonNumber = nil
        }
    }
    
    @ViewBuilder
    fileprivate var deleteSeasonDialogMessage: some View {
        if let season = confirmDeleteSeasonNumber {
            Text(MR.strings().delete_season_confirm.formatted(args: [season]))
        }
    }
    
    @ViewBuilder
    fileprivate var deleteAlbumDialogContent: some View {
        Button(MR.strings().confirm.localized(), role: .destructive) {
            if let albumId = confirmDeleteAlbumId {
                viewModel.deleteAlbumFiles(albumId: albumId)
            }
            confirmDeleteAlbumId = nil
        }
        Button(MR.strings().cancel.localized(), role: .cancel) {
            confirmDeleteAlbumId = nil
        }
    }
}

// MARK: - Handlers & Utilities
extension UnifiedMediaDetailsScreen {
    @ViewBuilder
    fileprivate var toastOverlay: some View {
        if let message = toastMessage {
            VStack {
                Spacer()
                Text(message)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(Color.black.opacity(0.75))
                    .cornerRadius(20)
                    .padding(.bottom, 24)
            }
            .transition(.opacity)
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                    withAnimation {
                        toastMessage = nil
                    }
                }
            }
        }
    }
    
    fileprivate func onLastSearchResultChanged(_ newVal: Bool?) {
        if let result = newVal {
            withAnimation {
                toastMessage = result ? MR.strings().search_queued.localized() : MR.strings().search_error.localized()
            }
        }
    }
    
    fileprivate func onEditSuccess() {
        withAnimation {
            toastMessage = MR.strings().item_edited_successfully.localized()
        }
        showEditSheet = false
        editAlbum = nil
    }
    
    fileprivate func onEditError() {
        withAnimation {
            toastMessage = MR.strings().error_editing_item.localized()
        }
    }
    
    fileprivate func onDeleteSuccess() {
        withAnimation {
            toastMessage = MR.strings().item_deleted_successfully.localized()
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            dismiss()
        }
    }
    
    fileprivate func onDeleteError() {
        withAnimation {
            toastMessage = MR.strings().error_deleting_item.localized()
        }
    }
}

// MARK: - Toolbar Content
extension UnifiedMediaDetailsScreen {
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess {
            let canAddDirectly = !success.hasArrId && success.arrMedia != nil && viewModel.isArrConfigured
            let showArrActions = success.hasArrId && viewModel.isArrConfigured
            let showSeerrActions = viewModel.isSeerrConfigured && (viewModel.buttonState.showRemoveFromServiceButton || viewModel.buttonState.showClearDataButton || viewModel.buttonState.showMarkAsAvailableButton)
            let showMissingInstances = !success.missingInstances.isEmpty
            let showMenuButton = showArrActions || showSeerrActions || showMissingInstances
            let showReportIssue = viewModel.buttonState.showReportIssueButton
            let showInstancePicker = success.availableInstances.count > 1
            
            if showReportIssue || showArrActions || canAddDirectly || showMenuButton || showInstancePicker {
                ToolbarItem(placement: .navigationBarTrailing) {
                    HStack(spacing: 12) {
                        if showInstancePicker, let resolvedType = viewModel.resolvedInstanceType {
                            InstancePickerMenu(
                                instances: success.availableInstances,
                                selectedInstanceId: success.selectedInstanceId?.int64Value,
                                onChangeInstance: { inst in
                                    withAnimation(.easeInOut(duration: 0.3)) {
                                        viewModel.selectInstance(instanceId: inst.id)
                                    }
                                },
                                onAddNewInstance: { navigationManager.goToNewInstance(of: resolvedType) }
                            )
                            .menuIndicator(.hidden)
                        }

                        if showReportIssue {
                            Button(action: { viewModel.showReportIssueSheet() }) {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(.orange)
                            }
                        }
                        
                        if showArrActions {
                            Button(action: { viewModel.toggleMonitored() }) {
                                Image(systemName: viewModel.isMonitored ? "bookmark.fill" : "bookmark")
                            }
                        }
                        
                        if canAddDirectly {
                            Button(action: { showAddSheet = true }) {
                                Image(systemName: "plus")
                            }
                        }
                        
                        if showMenuButton {
                            Menu {
                                if showArrActions {
                                    Section {
                                        Button(action: { viewModel.performRefresh() }) {
                                            Label(MR.strings().refresh.localized(), systemImage: "arrow.clockwise")
                                        }
                                        
                                        if viewModel.resolvedInstanceType?.includeTopLevelAutomaticSearchOption == true {
                                            Button(action: { viewModel.performAutomaticLookup() }) {
                                                Label(MR.strings().search_monitored.localized(), systemImage: "magnifyingglass")
                                            }
                                            .disabled(!viewModel.isMonitored)
                                        }
                                        
                                        Button(action: { showEditSheet = true }) {
                                            Label(MR.strings().edit.localized(), systemImage: "pencil")
                                        }
                                        
                                        Button(role: .destructive, action: { showConfirmSheet = true }) {
                                            Label(MR.strings().delete.localized(), systemImage: "trash")
                                        }
                                    }
                                }
                                
                                if showMissingInstances {
                                    Section {
                                        ForEach(success.missingInstances, id: \.id) { instance in
                                            Button(action: {
                                                viewModel.setAddSheetTargetInstance(instance: instance)
                                                showAddSheet = true
                                            }) {
                                                Label(MR.strings().add_to_arr.formatted(args: [instance.label]), systemImage: "plus")
                                            }
                                        }
                                    }
                                }
                                
                                if showSeerrActions {
                                    Section {
                                        if viewModel.buttonState.showMarkAsAvailableButton {
                                            let markTitle = viewModel.resolvedRequestType == RequestType.movie ? MR.strings().mark_as_available.localized() : MR.strings().mark_all_seasons_as_available.localized()
                                            Button(action: { viewModel.markSeerrMediaAsAvailable() }) {
                                                Label(markTitle, systemImage: "checkmark.circle")
                                            }
                                        }
                                        
                                        if viewModel.buttonState.showRemoveFromServiceButton {
                                            let removeTitle = viewModel.resolvedRequestType == RequestType.movie ? MR.strings().remove_from_radarr.localized() : MR.strings().remove_from_sonarr.localized()
                                            Button(role: .destructive, action: { confirmRemoveFromService = true }) {
                                                Label(removeTitle, systemImage: "trash")
                                            }
                                        }
                                        
                                        if viewModel.buttonState.showClearDataButton {
                                            Button(role: .destructive, action: { confirmClearData = true }) {
                                                Label(MR.strings().clear_data.localized(), systemImage: "xmark.bin")
                                            }
                                        }
                                    }
                                }
                            } label: {
                                Image(systemName: "ellipsis.circle")
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Header
struct UnifiedMediaDetailsHeader: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    let type: InstanceType?
    @State private var infoHeight: CGFloat = 0
    
    private var infoString: String {
        var items: [String] = []
        if let year = success.year {
            items.append(year)
        }
        if let runtime = success.runtimeString, !runtime.isEmpty {
            items.append(runtime)
        }
        if let seasonCount = success.seasonCount {
            items.append(MR.plurals().seasons.localized(Int32(seasonCount)))
        }
        if let certification = success.getCertification(countryCode: Locale.current.region?.identifier ?? "") {
            items.append(certification)
        }
        return items.joined(separator: " • ")
    }
    
    var body: some View {
        ZStack(alignment: .bottom) {
            MediaHeaderBanner(
                bannerUrl: URL(string: success.bannerUrl ?? ""),
                height: 350,
                gradientHeight: infoHeight * 2
            )
            
            HStack(alignment: .bottom, spacing: 24) {
                if let posterUrl = success.posterUrl {
                    GenericPosterItem(posterUrl: posterUrl)
                        .frame(width: 150)
                } else if let arrMedia = success.arrMedia {
                    PosterItem(item: arrMedia, aspectRatio: type?.aspectRatio ?? .poster)
                        .frame(width: 150)
                }
                
                VStack(alignment: .leading, spacing: 8) {
                    if let arrMedia = success.arrMedia {
                        ClearLogoView(item: arrMedia)
                    }
                    
                    VStack(alignment: .leading, spacing: 4) {
                        let ratings = success.ratings
                        if !ratings.isEmpty {
                            FlowLayout(spacing: 12) {
                                ForEach(ratings, id: \.self) { rating in
                                    HStack(spacing: 4) {
                                        if let icon = rating.icon {
                                            Image(resource: icon)
                                                .resizable()
                                                .frame(width: 16, height: 16)
                                        } else {
                                            Image(systemName: "star.fill")
                                                .foregroundColor(.arrOrange)
                                                .font(.system(size: 16))
                                        }
                                        Text(rating.score)
                                            .font(.system(size: 16, weight: .bold))
                                            .fixedSize(horizontal: true, vertical: false)
                                    }
                                }
                            }
                        }
                        
                        if !infoString.isEmpty {
                            Text(infoString)
                                .font(.system(size: 16))
                        }
                        
                        if let releasedBy = success.releasedBy {
                            Text(releasedBy)
                                .font(.system(size: 14))
                        }
                        
                        Text(success.genres.joined(separator: " • "))
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                    }
                }
                .background(GeometryReader { geometry in
                    Color.clear.preference(key: ViewHeightKey.self, value: geometry.size.height)
                })
                .frame(maxWidth: .infinity, alignment: .leading)
                .onPreferenceChange(ViewHeightKey.self) { height in
                    if height > 0 {
                        self.infoHeight = height
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
    }
}

// MARK: - Actions
struct UnifiedMediaDetailsActions: View {
    let buttonState: MediaButtonState
    let onWatch: (String) -> Void
    let onWatchTrailer: (String) -> Void
    let onRequest: () -> Void
    let onRequest4k: () -> Void
    let onViewRequest: () -> Void
    let onApproveRequest: () -> Void
    let onDeclineRequest: () -> Void
    let onManage: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            // Watch Button / Trailer Button
            if buttonState.showWatchButton, let url = buttonState.watchButtonUrl {
                if buttonState.showWatchTrailerOption, let trailerUrl = buttonState.trailerUrl {
                    HStack(spacing: 0) {
                        Button(action: { onWatch(url) }) {
                            HStack {
                                Image(systemName: "play.fill")
                                Text(buttonState.watchButtonLabel.localized())
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                        }
                        
                        Divider()
                            .frame(height: 24)
                            .background(Color.white.opacity(0.5))
                        
                        Menu {
                            Button(action: { onWatchTrailer(trailerUrl) }) {
                                Label(MR.strings().watch_trailer.localized(), systemImage: "film")
                            }
                        } label: {
                            Image(systemName: "chevron.down")
                                .padding(.horizontal, 12)
                                .padding(.vertical, 12)
                        }
                    }
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                } else {
                    Button(action: { onWatch(url) }) {
                        HStack {
                            Image(systemName: "play.fill")
                            Text(buttonState.watchButtonLabel.localized())
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.accentColor)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                }
            } else if buttonState.showWatchTrailerOption, let url = buttonState.trailerUrl {
                Button(action: { onWatchTrailer(url) }) {
                    HStack {
                        Image(systemName: "film")
                        Text(MR.strings().watch_trailer.localized())
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color(.secondarySystemBackground))
                    .foregroundColor(.primary)
                    .cornerRadius(8)
                }
            }
            
            // View Request Button
            if buttonState.showViewRequestButton {
                if buttonState.showApproveRequestButton || buttonState.showDeclineRequestButton {
                    HStack(spacing: 0) {
                        Button(action: onViewRequest) {
                            HStack {
                                Image(systemName: "clock")
                                Text(MR.strings().view_request.localized())
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                        }
                        
                        Divider()
                            .frame(height: 24)
                            .background(Color.primary.opacity(0.2))
                        
                        Menu {
                            if buttonState.showApproveRequestButton {
                                Button(action: onApproveRequest) {
                                    Label(MR.strings().approve_request.localized(), systemImage: "checkmark")
                                }
                            }
                            if buttonState.showDeclineRequestButton {
                                Button(role: .destructive, action: onDeclineRequest) {
                                    Label(MR.strings().decline_request.localized(), systemImage: "xmark")
                                }
                            }
                        } label: {
                            Image(systemName: "chevron.down")
                                .padding(.horizontal, 12)
                                .padding(.vertical, 12)
                        }
                    }
                    .background(Color(.secondarySystemBackground))
                    .foregroundColor(.primary)
                    .cornerRadius(8)
                } else {
                    Button(action: onViewRequest) {
                        HStack {
                            Image(systemName: "clock")
                            Text(MR.strings().view_request.localized())
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color(.secondarySystemBackground))
                        .foregroundColor(.primary)
                        .cornerRadius(8)
                    }
                }
            }
            
            // Request More Button
            if buttonState.showRequestMoreButton {
                Button(action: onRequest) {
                    HStack {
                        Image(systemName: "plus")
                        Text(MR.strings().request_more.localized())
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
            }
            
            // Request Button
            if buttonState.showRequestButton {
                if buttonState.showRequest4kButton {
                    HStack(spacing: 0) {
                        Button(action: onRequest) {
                            HStack {
                                Image(systemName: "plus")
                                Text(MR.strings().request.localized())
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                        }
                        
                        Divider()
                            .frame(height: 24)
                            .background(Color.white.opacity(0.5))
                        
                        Menu {
                            Button(action: onRequest4k) {
                                Label(MR.strings().request_in_4k.localized(), systemImage: "aqi.medium")
                            }
                        } label: {
                            Image(systemName: "chevron.down")
                                .padding(.horizontal, 12)
                                .padding(.vertical, 12)
                        }
                    }
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                } else {
                    Button(action: onRequest) {
                        HStack {
                            Image(systemName: "plus")
                            Text(MR.strings().request.localized())
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(Color.accentColor)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                }
            } else if buttonState.showRequest4kButton {
                Button(action: onRequest4k) {
                    HStack {
                        Image(systemName: "plus")
                        Text(MR.strings().request_in_4k.localized())
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
            }
        }
    }
}

// MARK: - View Modifiers
fileprivate struct UnifiedMediaDetailsSheetsModifier: ViewModifier {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    @Binding var showEditSheet: Bool
    @Binding var showAddSheet: Bool
    @Binding var showConfirmSheet: Bool
    @Binding var editAlbum: ArrAlbum?
    @Binding var selectedQueueItem: QueueItem?
    let screen: UnifiedMediaDetailsScreen
    
    func body(content: Content) -> some View {
        content
            .sheet(isPresented: $showEditSheet) { screen.editSheetContent }
            .sheet(isPresented: $showAddSheet) { screen.addSheetContent }
            .sheet(isPresented: $showConfirmSheet) { screen.confirmSheetContent }
            .sheet(item: $editAlbum) { screen.editAlbumSheetContent($0) }
            .sheet(isPresented: $viewModel.isRequestSheetVisible) { screen.requestSheetContent }
            .sheet(isPresented: $viewModel.isReportIssueSheetVisible) { screen.reportIssueSheetContent }
            .sheet(isPresented: $viewModel.isViewRequestSheetVisible) { screen.viewRequestSheetContent }
            .sheet(item: Binding(
                get: { selectedQueueItem.map { IdentifiableQueueItem(item: $0) } },
                set: { selectedQueueItem = $0?.item }
            )) {
                screen.queueItemSheetContent($0)
            }
    }
}

fileprivate struct UnifiedMediaDetailsAlertsModifier: ViewModifier {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    @Binding var confirmDeleteMovie: Bool
    @Binding var confirmRemoveFromService: Bool
    @Binding var confirmClearData: Bool
    @Binding var confirmDeleteSeasonNumber: Int32?
    @Binding var confirmDeleteAlbumId: Int64?
    let removeServiceName: String
    let screen: UnifiedMediaDetailsScreen
    
    func body(content: Content) -> some View {
        content
            .alert(MR.strings().confirm_delete.localized(), isPresented: $confirmDeleteMovie) {
                screen.deleteMovieAlertContent
            } message: {
                Text(MR.strings().confirm_delete_file.localized())
            }
            .alert(MR.strings().are_you_sure.localized(), isPresented: $confirmRemoveFromService) {
                Button(MR.strings().no.localized(), role: .cancel) {}
                Button(role: .destructive) {
                    viewModel.deleteSeerrMediaFile(is4k: false)
                } label: {
                    Text(MR.strings().yes.localized())
                }
            } message: {
                Text(MR.strings().remove_from_service_confirm.formatted(args: [removeServiceName]))
            }
            .alert(MR.strings().are_you_sure.localized(), isPresented: $confirmClearData) {
                Button(MR.strings().no.localized(), role: .cancel) {}
                Button(role: .destructive) {
                    viewModel.clearSeerrMediaData()
                } label: {
                    Text(MR.strings().yes.localized())
                }
            } message: {
                Text(MR.strings().clear_data_confirm.localized())
            }
            .confirmationDialog("", isPresented: Binding(
                get: { confirmDeleteSeasonNumber != nil },
                set: { if !$0 { confirmDeleteSeasonNumber = nil } }
            )) {
                screen.deleteSeasonDialogContent
            } message: {
                screen.deleteSeasonDialogMessage
            }
            .confirmationDialog("", isPresented: Binding(
                get: { confirmDeleteAlbumId != nil },
                set: { if !$0 { confirmDeleteAlbumId = nil } }
            )) {
                screen.deleteAlbumDialogContent
            } message: {
                Text(MR.strings().delete_album_confirm.localized())
            }
    }
}

fileprivate struct UnifiedMediaDetailsEventsModifier: ViewModifier {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let screen: UnifiedMediaDetailsScreen
    
    func body(content: Content) -> some View {
        content
            .onChange(of: viewModel.lastSearchResult) { _, newVal in
                screen.onLastSearchResultChanged(newVal)
            }
            .onChange(of: viewModel.editSuccessTrigger) { _, _ in
                screen.onEditSuccess()
            }
            .onChange(of: viewModel.editErrorTrigger) { _, _ in
                screen.onEditError()
            }
            .onChange(of: viewModel.deleteSuccessTrigger) { _, _ in
                screen.onDeleteSuccess()
            }
            .onChange(of: viewModel.deleteErrorTrigger) { _, _ in
                screen.onDeleteError()
            }
    }
}
