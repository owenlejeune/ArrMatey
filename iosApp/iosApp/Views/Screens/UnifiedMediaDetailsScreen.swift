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
    @State private var selectedQueueItem: QueueItem? = nil
    
    @State private var toastMessage: String? = nil
    
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
        .sheet(isPresented: $showEditSheet) { editSheetContent }
        .sheet(isPresented: $showAddSheet) { addSheetContent }
        .sheet(isPresented: $showConfirmSheet) { confirmSheetContent }
        .sheet(item: $editAlbum) { editAlbumSheetContent($0) }
        .sheet(isPresented: $viewModel.isRequestSheetVisible) { requestSheetContent }
        .sheet(isPresented: $viewModel.isReportIssueSheetVisible) { reportIssueSheetContent }
        .sheet(isPresented: $viewModel.isViewRequestSheetVisible) { viewRequestSheetContent }
        .sheet(item: Binding(
            get: { selectedQueueItem.map { IdentifiableQueueItem(item: $0) } },
            set: { selectedQueueItem = $0?.item }
        )) {
            queueItemSheetContent($0)
        }
        .alert(MR.strings().confirm_delete.localized(), isPresented: $confirmDeleteMovie) {
            deleteMovieAlertContent
        } message: {
            Text(MR.strings().confirm_delete_file.localized())
        }
        .confirmationDialog("", isPresented: Binding(
            get: { confirmDeleteSeasonNumber != nil },
            set: { if !$0 { confirmDeleteSeasonNumber = nil } }
        )) {
            deleteSeasonDialogContent
        } message: {
            deleteSeasonDialogMessage
        }
        .confirmationDialog("", isPresented: Binding(
            get: { confirmDeleteAlbumId != nil },
            set: { if !$0 { confirmDeleteAlbumId = nil } }
        )) {
            deleteAlbumDialogContent
        } message: {
            Text(MR.strings().delete_album_confirm.localized())
        }
        .onChange(of: viewModel.lastSearchResult) { old, newVal in
            onLastSearchResultChanged(newVal)
        }
        .onChange(of: viewModel.editSuccessTrigger) { _, _ in
            onEditSuccess()
        }
        .onChange(of: viewModel.editErrorTrigger) { _, _ in
            onEditError()
        }
        .onChange(of: viewModel.deleteSuccessTrigger) { _, _ in
            onDeleteSuccess()
        }
        .onChange(of: viewModel.deleteErrorTrigger) { _, _ in
            onDeleteError()
        }
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
                    
                    let hasPresence = success.instancePresences.contains { $0.isPresent }
                    if success.instancePresences.count > 1 && hasPresence {
                        InstanceChipsRow(
                            presences: success.instancePresences,
                            selectedInstanceId: success.selectedInstanceId?.int64Value,
                            onInstanceSelected: { id in
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    viewModel.selectInstance(instanceId: id.id)
                                }
                            }
                        )
                    }
                    
                    if !success.queueItems.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text(MR.strings().activity.localized())
                                .font(.title3.bold())
                            ForEach(success.queueItems, id: \.id) { item in
                                ActivityQueueItem(item: item, onClick: { selectedQueueItem = item })
                            }
                        }
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
    
    /// Shows seasons using the unified SeasonsArea component.
    /// Mirrors Android: SeasonsArea takes List<SeasonWrapper> combining arr + seerr data,
    /// and shows arr controls only when arrSeason is present and seriesId > 0.
    @ViewBuilder
    private func seasonsArea(_ success: UnifiedMediaDetailsUiStateSuccess) -> some View {
        let seriesId = (success.arrMedia as? ArrSeries)?.id?.int64Value
        SeasonsArea(
            seasons: success.seasons,
            seriesId: seriesId,
            searchIds: viewModel.automaticSearchIds,
            onToggleSeasonMonitor: { viewModel.toggleSeasonMonitored(seasonNumber: $0) },
            onToggleEpisodeMonitor: { viewModel.toggleEpisodeMonitored(episode: $0) },
            onEpisodeAutomaticSearch: { viewModel.performEpisodeAutomaticLookup(episodeId: $0) },
            onSeasonAutomaticSearch: { viewModel.performSeasonAutomaticLookup(seasonNumber: $0) },
            deleteSeasonFiles: { confirmDeleteSeasonNumber = $0 },
            seasonDeleteInProgress: viewModel.deleteSeasonStatus is NetworkingOperationStatusInProgress
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
        let infoItems = buildUnifiedInfoItems(success: success)
        if !infoItems.isEmpty {
            MediaInfoArea(infoItems: infoItems)
        }
    }
    
    private func buildUnifiedInfoItems(success: UnifiedMediaDetailsUiStateSuccess) -> [InfoItem] {
        var items: [InfoItem] = []
        
        // Arr fields — only when actually in the arr library
        if success.hasArrId, let arrMedia = success.arrMedia {
            let arrItems = buildArrInfoItems(
                arrMedia: arrMedia,
                qualityProfiles: viewModel.qualityProfiles,
                tags: viewModel.tags
            )
            items.append(contentsOf: arrItems)
        }
        
        // Seerr fields — always shown when seerr data is available
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
    private var addSheetContent: some View {
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
    private var editSheetContent: some View {
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
    private var confirmSheetContent: some View {
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
    private func queueItemSheetContent(_ wrapper: IdentifiableQueueItem) -> some View {
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
    private func editAlbumSheetContent(_ album: ArrAlbum) -> some View {
        EditAlbumSheet(album: album, editInProgress: viewModel.editStatus is NetworkingOperationStatusInProgress, onEditAlbum: { updatedAlbum in
            viewModel.updateAlbum(album: updatedAlbum)
        })
    }
    
    @ViewBuilder
    private var requestSheetContent: some View {
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
    private var reportIssueSheetContent: some View {
        SeerrReportIssueSheet(viewModel: viewModel, onDismiss: { viewModel.hideReportIssueSheet() })
    }
    
    @ViewBuilder
    private var viewRequestSheetContent: some View {
        if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess, let seerrMedia = success.seerrMedia {
            SeerrViewRequestSheet(details: seerrMedia, viewModel: viewModel, onDismissRequest: { viewModel.hideViewRequestSheet() })
        }
    }
    
    @ViewBuilder
    private var deleteMovieAlertContent: some View {
        Button(MR.strings().cancel.localized(), role: .cancel) { }
        Button(MR.strings().confirm.localized(), role: .destructive) {
            viewModel.deleteMovieFile()
        }
    }
    
    @ViewBuilder
    private var deleteSeasonDialogContent: some View {
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
    private var deleteSeasonDialogMessage: some View {
        if let season = confirmDeleteSeasonNumber {
            Text(MR.strings().delete_season_confirm.formatted(args: [season]))
        }
    }
    
    @ViewBuilder
    private var deleteAlbumDialogContent: some View {
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
    private var toastOverlay: some View {
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
    
    private func onLastSearchResultChanged(_ newVal: Bool?) {
        if let result = newVal {
            withAnimation {
                toastMessage = result ? MR.strings().search_queued.localized() : MR.strings().search_error.localized()
            }
        }
    }
    
    private func onEditSuccess() {
        withAnimation {
            toastMessage = MR.strings().item_edited_successfully.localized()
        }
        showEditSheet = false
        editAlbum = nil
    }
    
    private func onEditError() {
        withAnimation {
            toastMessage = MR.strings().error_editing_item.localized()
        }
    }
    
    private func onDeleteSuccess() {
        withAnimation {
            toastMessage = MR.strings().item_deleted_successfully.localized()
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            dismiss()
        }
    }
    
    private func onDeleteError() {
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
            let canAddDirectly = success.arrMedia != nil && viewModel.isArrConfigured
            let showArrActions = success.hasArrId && viewModel.isArrConfigured
            let showAddActions = !success.hasArrId && canAddDirectly
            let showReportIssue = viewModel.buttonState.showReportIssueButton
            
            if showReportIssue || showArrActions || showAddActions {
                ToolbarItem(placement: .navigationBarTrailing) {
                    HStack(spacing: 12) {
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
                            
                            Menu {
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
                                
                                if !success.missingInstances.isEmpty {
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
                            } label: {
                                Image(systemName: "ellipsis.circle")
                            }
                        } else if showAddActions {
                            if viewModel.isSeerrConfigured {
                                Menu {
                                    Button(action: { showAddSheet = true }) {
                                        Label(MR.strings().add_to_arr.formatted(args: [viewModel.resolvedInstanceType?.name ?? "Arr"]), systemImage: "plus")
                                    }
                                    
                                    if let _ = viewModel.buttonState.pendingRequestId {
                                        Button(action: { viewModel.showViewRequestSheet() }) {
                                            Label(MR.strings().view_request.localized(), systemImage: "clock")
                                        }
                                    } else {
                                        Button(action: { viewModel.showRequestSheet(is4k: false) }) {
                                            Label(MR.strings().request.localized(), systemImage: "paperplane")
                                        }
                                    }
                                } label: {
                                    Image(systemName: "plus")
                                }
                            } else {
                                Button(action: { showAddSheet = true }) {
                                    Image(systemName: "plus")
                                }
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
                            .background(GeometryReader { geometry in
                                Color.clear.preference(key: ViewHeightKey.self, value: geometry.size.height)
                            })
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .onPreferenceChange(ViewHeightKey.self) { height in
                    self.infoHeight = height
                }
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
//        .frame(height: 350)
//        .onPreferenceChange(ViewHeightKey.self) { height in
//            self.infoHeight = height
//        }
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
            }
        }
    }
}

// MARK: - Switcher Chips
struct InstanceChipsRow: View {
    let presences: [InstanceMediaPresence]
    let selectedInstanceId: Int64?
    let onInstanceSelected: (Instance) -> Void
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(presences, id: \.instance.id) { presence in
                    let isSelected = presence.instance.id == selectedInstanceId
                    Button(action: { onInstanceSelected(presence.instance) }) {
                        HStack(spacing: 4) {
                            Text(presence.instance.label)
                                .font(.system(size: 14, weight: isSelected ? .bold : .regular))
                            if presence.isPresent {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 12))
                                    .foregroundColor(isSelected ? .white : .green)
                            }
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(isSelected ? Color.accentColor : Color(.secondarySystemBackground))
                        .foregroundColor(isSelected ? .white : .primary)
                        .cornerRadius(16)
                    }
                }
            }
        }
    }
}
