//
//  MediaRequestOrAddSheet.swift
//  iosApp
//

import SwiftUI
import Shared

enum MediaSheetMode: String, CaseIterable, Identifiable {
    case addDirectly
    case request

    var id: String { rawValue }
}

struct MediaRequestOrAddSheet: View {
    private let source: ContentSource
    let onDismiss: () -> Void

    private enum ContentSource {
        case discover(DiscoverResult)
        case existing(UnifiedMediaDetailsViewModelS)
    }

    init(item: DiscoverResult, onDismiss: @escaping () -> Void) {
        self.source = .discover(item)
        self.onDismiss = onDismiss
    }

    init(viewModel: UnifiedMediaDetailsViewModelS, onDismiss: @escaping () -> Void) {
        self.source = .existing(viewModel)
        self.onDismiss = onDismiss
    }

    var body: some View {
        switch source {
        case .discover(let item):
            DiscoverMediaRequestOrAddSheetHost(item: item, onDismiss: onDismiss)
        case .existing(let vm):
            MediaRequestOrAddSheetContent(viewModel: vm, onDismiss: onDismiss)
        }
    }
}

private struct DiscoverMediaRequestOrAddSheetHost: View {
    let item: DiscoverResult
    let onDismiss: () -> Void
    @StateObject private var viewModel: UnifiedMediaDetailsViewModelS

    init(item: DiscoverResult, onDismiss: @escaping () -> Void) {
        self.item = item
        self.onDismiss = onDismiss
        self._viewModel = StateObject(wrappedValue: UnifiedMediaDetailsViewModelS(
            arrId: nil,
            tmdbId: item.id,
            tvdbId: nil,
            instanceType: item.mediaType == .tv ? .sonarr : .radarr,
            requestType: item.mediaType
        ))
    }

    var body: some View {
        MediaRequestOrAddSheetContent(viewModel: viewModel, onDismiss: onDismiss)
    }
}

private struct MediaRequestOrAddSheetContent: View {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onDismiss: () -> Void

    @State private var selectedMode: MediaSheetMode = .addDirectly
    @State private var hasManuallySelectedMode: Bool = false
    @State private var is4k: Bool = false

    // Request State
    @State private var selectedProfileId: Int64?
    @State private var selectedRootFolder: String?
    @State private var selectedUserId: Int64?
    @State private var selectedSeasons: Set<Int32> = []

    // Add Movie State
    @State private var isMovieMonitored: Bool = true
    @State private var selectedMovieMinimumAvailability: MediaStatus = .released
    @State private var selectedMovieQualityProfileId: Int32?
    @State private var selectedMovieRootFolderId: Int32?
    @State private var selectedMovieTags: Set<Int> = []
    @State private var searchMovieOnAdd: Bool = false

    // Add Series State
    @State private var seriesMonitorType: SeriesMonitorType = .all
    @State private var selectedSeriesQualityProfileId: Int32?
    @State private var selectedSeriesType: SeriesType = .standard
    @State private var useSeasonFolders: Bool = true
    @State private var selectedSeriesRootFolderId: Int32?
    @State private var selectedSeriesTags: Set<Int> = []
    @State private var searchSeriesOnAdd: Bool = false

    private var uiSuccess: UnifiedMediaDetailsUiStateSuccess? {
        viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess
    }

    private var seerrMedia: RequestMediaDetails? {
        uiSuccess?.seerrMedia
    }

    private var arrMedia: ArrMedia? {
        uiSuccess?.arrMedia
    }

    private var canAddDirectly: Bool {
        !viewModel.addSheetUiState.availableInstances.isEmpty
    }

    private var canRequest: Bool {
        seerrMedia != nil
    }

    private var resolvedMediaType: RequestType {
        viewModel.resolvedRequestType ?? (seerrMedia is TvDetails || arrMedia is ArrSeries ? .tv : .movie)
    }

    private var resolvedInstanceType: InstanceType {
        viewModel.resolvedInstanceType ?? (resolvedMediaType == .tv ? .sonarr : .radarr)
    }

    private var isBusy: Bool {
        viewModel.addItemStatus is OperationStatusInProgress || viewModel.requestStatus is OperationStatusInProgress
    }

    private var effectiveQualityProfiles: [QualityProfile] {
        viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles
    }

    private var effectiveRootFolders: [RootFolder] {
        viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders
    }

    private var effectiveTags: [Tag] {
        viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags
    }

    private var selectedMovieRootFolderPath: String? {
        effectiveRootFolders.first { $0.id == selectedMovieRootFolderId }?.path
    }

    private var selectedSeriesRootFolderPath: String? {
        effectiveRootFolders.first { $0.id == selectedSeriesRootFolderId }?.path
    }

    private var isActionEnabled: Bool {
        if isBusy { return false }
        if selectedMode == .request {
            return true
        } else {
            if arrMedia is ArrSeries {
                return selectedSeriesQualityProfileId != nil && selectedSeriesRootFolderPath != nil
            } else if arrMedia is ArrMovie {
                return selectedMovieQualityProfileId != nil && selectedMovieRootFolderPath != nil
            }
            return false
        }
    }

    var body: some View {
        NavigationStack {
            sheetMainContent
        }
        .onAppear {
            initializeState()
        }
        .onChange(of: viewModel.addSheetUiState.availableInstances.count) { _, _ in
            if !hasManuallySelectedMode {
                selectedMode = canAddDirectly ? .addDirectly : .request
            }
        }
        .onChange(of: viewModel.serviceDetails?.server.id) { _, _ in
            if let sp = viewModel.serviceDetails {
                if selectedProfileId == nil {
                    selectedProfileId = Int64(sp.server.activeProfileId)
                }
                if selectedRootFolder == nil {
                    selectedRootFolder = sp.server.activeDirectory
                }
            }
        }
        .onChange(of: viewModel.currentUser?.id) { _, userId in
            if selectedUserId == nil {
                selectedUserId = userId
            }
        }
        .onChange(of: seerrMedia?.id) { _, _ in
            if let tv = seerrMedia as? TvDetails, selectedSeasons.isEmpty {
                selectedSeasons = Set(tv.seasons.map { $0.seasonNumber })
            }
        }
        .onChange(of: viewModel.addSheetUiState.qualityProfiles.count) { _, _ in
            syncQualityProfiles()
        }
        .onChange(of: viewModel.qualityProfiles.count) { _, _ in
            syncQualityProfiles()
        }
        .onChange(of: viewModel.addSheetUiState.rootFolders.count) { _, _ in
            syncRootFolders()
        }
        .onChange(of: viewModel.rootFolders.count) { _, _ in
            syncRootFolders()
        }
        .onChange(of: viewModel.addItemStatus is OperationStatusSuccess) { _, isSuccess in
            if isSuccess {
                onDismiss()
            }
        }
        .onChange(of: viewModel.requestStatus is OperationStatusSuccess) { _, isSuccess in
            if isSuccess {
                onDismiss()
            }
        }
    }

    private var sheetMainContent: some View {
        VStack(spacing: 0) {
            headerView
                .padding(.horizontal, 20)
                .padding(.top, 20)
                .padding(.bottom, 12)

            Divider()

            ScrollView {
                VStack(spacing: 16) {
                    if selectedMode == .request {
                        requestConfigView
                    } else if let series = arrMedia as? ArrSeries {
                        seriesAddConfigView(series: series)
                    } else if let movie = arrMedia as? ArrMovie {
                        movieAddConfigView(movie: movie)
                    } else {
                        ProgressView()
                            .frame(maxWidth: .infinity, minHeight: 180)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
            .animation(.easeInOut(duration: 0.25), value: selectedMode)

            Divider()

            footerView
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
        }
        .background(Color(UIColor.systemBackground))
    }

    private func syncQualityProfiles() {
        if let firstId = effectiveQualityProfiles.first?.id {
            if selectedMovieQualityProfileId == nil {
                selectedMovieQualityProfileId = firstId
            }
            if selectedSeriesQualityProfileId == nil {
                selectedSeriesQualityProfileId = firstId
            }
        }
    }

    private func syncRootFolders() {
        if let firstId = effectiveRootFolders.first?.id {
            if selectedMovieRootFolderId == nil {
                selectedMovieRootFolderId = firstId
            }
            if selectedSeriesRootFolderId == nil {
                selectedSeriesRootFolderId = firstId
            }
        }
    }

    private func initializeState() {
        let prefs = viewModel.preferences
        isMovieMonitored = prefs.addMovieMonitored
        selectedMovieMinimumAvailability = prefs.addMovieMinimumAvailability
        searchMovieOnAdd = prefs.addSearchOnAdd
        seriesMonitorType = prefs.addSeriesMonitor
        selectedSeriesType = prefs.addSeriesType
        useSeasonFolders = prefs.addSeriesSeasonFolder
        searchSeriesOnAdd = prefs.addSearchOnAdd

        if let qp = effectiveQualityProfiles.first(where: { $0.id == prefs.addQualityProfileId?.int32Value }) ?? effectiveQualityProfiles.first {
            selectedMovieQualityProfileId = qp.id
            selectedSeriesQualityProfileId = qp.id
        }

        if let rf = effectiveRootFolders.first(where: { $0.path == prefs.addRootFolderPath }) ?? effectiveRootFolders.first {
            selectedMovieRootFolderId = rf.id
            selectedSeriesRootFolderId = rf.id
        }

        if let tv = seerrMedia as? TvDetails {
            selectedSeasons = Set(tv.seasons.map { $0.seasonNumber })
        }

        if let serviceDetails = viewModel.serviceDetails {
            selectedProfileId = Int64(serviceDetails.server.activeProfileId)
            selectedRootFolder = serviceDetails.server.activeDirectory
        }

        if let user = viewModel.currentUser {
            selectedUserId = user.id
        }

        if !hasManuallySelectedMode {
            selectedMode = canAddDirectly ? .addDirectly : .request
        }
    }

    // MARK: - Header
    @ViewBuilder
    private var headerView: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                Text(resolvedMediaType == .tv ? MR.strings().type_series.localized().uppercased() : MR.strings().type_movie.localized().uppercased())
                    .font(.caption.bold())
                    .foregroundColor(.accentColor)

                Text(seerrMedia?.displayTitle ?? arrMedia?.title ?? "")
                    .font(.title2.bold())
                    .lineLimit(2)
            }

            if canAddDirectly && canRequest {
                Picker("", selection: Binding(
                    get: { selectedMode },
                    set: {
                        selectedMode = $0
                        hasManuallySelectedMode = true
                    }
                )) {
                    Text(MR.strings().add_to_arr.localized().replacingOccurrences(of: "%s", with: resolvedInstanceType.name))
                        .tag(MediaSheetMode.addDirectly)
                    Text(MR.strings().request.localized())
                        .tag(MediaSheetMode.request)
                }
                .pickerStyle(.segmented)
                .disabled(isBusy)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    // MARK: - Request Section
    @ViewBuilder
    private var requestConfigView: some View {
        VStack(alignment: .leading, spacing: 20) {
            if viewModel.buttonState.showRequest4kButton {
                Toggle(isOn: $is4k) {
                    Label(MR.strings().request_in_4k.localized(), systemImage: "aqi.medium")
                        .font(.subheadline.bold())
                }
                .disabled(isBusy)
                .padding(.vertical, 4)
            }

            if let tv = seerrMedia as? TvDetails {
                seasonsSelectionView(tv: tv)
            }

            requestAdvancedSettingsView
        }
    }

    @ViewBuilder
    private func seasonsSelectionView(tv: TvDetails) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(MR.strings().seasons_header.localized())
                .font(.headline)

            Toggle(MR.strings().all_seasons.localized(), isOn: Binding(
                get: { selectedSeasons.count == tv.seasons.count },
                set: { isOn in
                    if isOn {
                        selectedSeasons = Set(tv.seasons.map { $0.seasonNumber })
                    } else {
                        selectedSeasons = []
                    }
                }
            ))
            .disabled(isBusy)

            ForEach(tv.seasons, id: \.seasonNumber) { season in
                Toggle(isOn: Binding(
                    get: { selectedSeasons.contains(season.seasonNumber) },
                    set: { isOn in
                        if isOn {
                            selectedSeasons.insert(season.seasonNumber)
                        } else {
                            selectedSeasons.remove(season.seasonNumber)
                        }
                    }
                )) {
                    VStack(alignment: .leading) {
                        Text(season.seasonNumber == 0 ? MR.strings().specials.localized() : MR.strings().season_label.formatted(args: [season.seasonNumber]))
                        Text(MR.plurals().episodes.localized(season.episodeCount))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .disabled(isBusy)
            }
        }
    }

    @ViewBuilder
    private var requestAdvancedSettingsView: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(MR.strings().advanced.localized())
                .font(.headline)

            Picker(MR.strings().quality_profile.localized(), selection: $selectedProfileId) {
                Text(MR.strings().unknown.localized()).tag(nil as Int64?)
                ForEach(viewModel.serviceDetails?.profiles ?? [], id: \.id) { profile in
                    Text(profile.name).tag(Int64(profile.id) as Int64?)
                }
            }
            .disabled(isBusy)

            Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolder) {
                Text(MR.strings().unknown.localized()).tag(nil as String?)
                ForEach(viewModel.serviceDetails?.rootFolders ?? [], id: \.path) { folder in
                    Text(folder.path).tag(folder.path as String?)
                }
            }
            .disabled(isBusy)

            if viewModel.currentUser?.hasPermission(permission: .admin) == true && !viewModel.users.isEmpty {
                Picker(MR.strings().request_as.localized(), selection: $selectedUserId) {
                    ForEach(viewModel.users, id: \.id) { user in
                        Text(user.displayName).tag(user.id as Int64?)
                    }
                }
                .disabled(isBusy)
            }
        }
    }

    // MARK: - Series Section
    @ViewBuilder
    private func seriesAddConfigView(series: ArrSeries) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            let instances = viewModel.addSheetUiState.availableInstances
            if instances.count > 1, let target = viewModel.addSheetUiState.targetInstance {
                Picker(MR.strings().instances.localized(), selection: Binding(
                    get: { target },
                    set: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )) {
                    ForEach(instances, id: \.id) { inst in
                        Text(inst.label).tag(inst)
                    }
                }
                .disabled(isBusy)
            }

            Picker(MR.strings().monitor.localized(), selection: $seriesMonitorType) {
                ForEach([SeriesMonitorType.all, SeriesMonitorType.future, SeriesMonitorType.missing, SeriesMonitorType.existing, SeriesMonitorType.pilot, SeriesMonitorType.firstSeason, SeriesMonitorType.lastSeason, SeriesMonitorType.none], id: \.self) { type in
                    Text(type.resource.localized()).tag(type)
                }
            }
            .disabled(isBusy)

            if !effectiveQualityProfiles.isEmpty {
                Picker(MR.strings().quality_profile.localized(), selection: $selectedSeriesQualityProfileId) {
                    ForEach(effectiveQualityProfiles, id: \.self) { qp in
                        if let name = qp.name {
                            Text(name).tag(qp.id as Int32?)
                        }
                    }
                }
                .disabled(isBusy)
            }

            Picker(MR.strings().series_type.localized(), selection: $selectedSeriesType) {
                ForEach(SeriesType.allCases, id: \.self) { type in
                    Text(type.resource.localized()).tag(type)
                }
            }
            .disabled(isBusy)

            Toggle(MR.strings().season_folders.localized(), isOn: $useSeasonFolders)
                .disabled(isBusy)

            if !effectiveTags.isEmpty {
                NavigationLink {
                    TagSelectionView(tags: effectiveTags, selectedTags: $selectedSeriesTags)
                } label: {
                    LabeledContent(MR.strings().tags.localized(), value: MR.plurals().tag_count.localized(selectedSeriesTags.count))
                }
                .disabled(isBusy)
            }

            Toggle(MR.strings().search_on_add_label.localized(), isOn: $searchSeriesOnAdd)
                .disabled(isBusy)

            if !effectiveRootFolders.isEmpty {
                Picker(MR.strings().root_folder.localized(), selection: $selectedSeriesRootFolderId) {
                    ForEach(effectiveRootFolders, id: \.self) { rf in
                        Text("\(rf.path) (\(rf.freeSpaceString))")
                            .tag(rf.id as Int32?)
                    }
                }
                .disabled(isBusy)
            }
        }
    }

    // MARK: - Movie Section
    @ViewBuilder
    private func movieAddConfigView(movie: ArrMovie) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            let instances = viewModel.addSheetUiState.availableInstances
            if instances.count > 1, let target = viewModel.addSheetUiState.targetInstance {
                Picker(MR.strings().instances.localized(), selection: Binding(
                    get: { target },
                    set: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )) {
                    ForEach(instances, id: \.id) { inst in
                        Text(inst.label).tag(inst)
                    }
                }
                .disabled(isBusy)
            }

            Toggle(MR.strings().monitored.localized(), isOn: $isMovieMonitored)
                .disabled(isBusy)

            if !effectiveQualityProfiles.isEmpty {
                Picker(MR.strings().quality_profile.localized(), selection: $selectedMovieQualityProfileId) {
                    ForEach(effectiveQualityProfiles, id: \.self) { qp in
                        if let name = qp.name {
                            Text(name).tag(qp.id as Int32?)
                        }
                    }
                }
                .disabled(isBusy)
            }

            Picker(MR.strings().minimum_availability.localized(), selection: $selectedMovieMinimumAvailability) {
                ForEach([MediaStatus.announced, MediaStatus.inCinemas, MediaStatus.released], id: \.self) { status in
                    Text(status.resource.localized()).tag(status)
                }
            }
            .disabled(isBusy)

            if !effectiveTags.isEmpty {
                NavigationLink {
                    TagSelectionView(tags: effectiveTags, selectedTags: $selectedMovieTags)
                } label: {
                    LabeledContent(MR.strings().tags.localized(), value: MR.plurals().tag_count.localized(selectedMovieTags.count))
                }
                .disabled(isBusy)
            }

            Toggle(MR.strings().search_on_add_label.localized(), isOn: $searchMovieOnAdd)
                .disabled(isBusy)

            if !effectiveRootFolders.isEmpty {
                Picker(MR.strings().root_folder.localized(), selection: $selectedMovieRootFolderId) {
                    ForEach(effectiveRootFolders, id: \.self) { rf in
                        Text("\(rf.path) (\(rf.freeSpaceString))")
                            .tag(rf.id as Int32?)
                    }
                }
                .disabled(isBusy)
            }
        }
    }

    // MARK: - Footer
    @ViewBuilder
    private var footerView: some View {
        HStack(spacing: 12) {
            Button(action: onDismiss) {
                Text(MR.strings().cancel.localized())
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .disabled(isBusy)

            Button(action: handleAction) {
                HStack(spacing: 6) {
                    if isBusy {
                        ProgressView()
                    } else if selectedMode == .request {
                        Text(is4k ? MR.strings().request_in_4k.localized() : MR.strings().request.localized())
                            .bold()
                    } else {
                        Image(systemName: "checkmark")
                        Text(MR.strings().save.localized())
                            .bold()
                    }
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(!isActionEnabled)
        }
    }

    private func handleAction() {
        if selectedMode == .request {
            let seasons = seerrMedia is TvDetails ? Array(selectedSeasons).map { KotlinInt(value: $0) } : nil
            viewModel.submitRequest(
                profileId: selectedProfileId,
                rootFolder: selectedRootFolder,
                languageProfileId: nil,
                seasons: seasons,
                is4k: is4k,
                userId: selectedUserId
            )
        } else {
            let prefs = viewModel.preferences
            if let series = arrMedia as? ArrSeries, let qp = selectedSeriesQualityProfileId, let path = selectedSeriesRootFolderPath {
                viewModel.updatePreferences(
                    preferences: prefs.doCopy(
                        sortBy: prefs.sortBy,
                        sortOrder: prefs.sortOrder,
                        filterBy: prefs.filterBy,
                        customFilterId: prefs.customFilterId,
                        viewType: prefs.viewType,
                        posterElevation: prefs.posterElevation,
                        posterRadius: prefs.posterRadius,
                        showFullDetails: prefs.showFullDetails,
                        showOverlay: prefs.showOverlay,
                        gridDensity: prefs.gridDensity,
                        gridSpacing: prefs.gridSpacing,
                        showBannerBackground: prefs.showBannerBackground,
                        includeOverview: prefs.includeOverview,
                        bannerBlur: prefs.bannerBlur,
                        applyGlobally: prefs.applyGlobally,
                        addQualityProfileId: qp.asKotlinInt,
                        addRootFolderPath: path,
                        addSearchOnAdd: searchSeriesOnAdd,
                        addSeriesMonitor: seriesMonitorType,
                        addSeriesType: selectedSeriesType,
                        addSeriesSeasonFolder: useSeasonFolders,
                        addMovieMonitored: prefs.addMovieMonitored,
                        addMovieMinimumAvailability: prefs.addMovieMinimumAvailability,
                        addArtistMonitor: prefs.addArtistMonitor,
                        addArtistMonitorNew: prefs.addArtistMonitorNew,
                        addAuthorMonitor: prefs.addAuthorMonitor,
                        addAuthorMonitorNew: prefs.addAuthorMonitorNew,
                        addAudiobookMonitored: prefs.addAudiobookMonitored,
                        deleteDeleteFiles: prefs.deleteDeleteFiles,
                        deleteAddExclusion: prefs.deleteAddExclusion
                    )
                )
                let newSeries = series.doCopyForCreation(
                    monitor: seriesMonitorType,
                    qualityProfileId: qp,
                    seriesType: selectedSeriesType,
                    seasonFolder: useSeasonFolders,
                    rootFolderPath: path,
                    tags: Array(selectedSeriesTags.map { $0.asKotlinInt })
                )
                viewModel.smartAdd(item: newSeries, searchOnAdd: searchSeriesOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
            } else if let movie = arrMedia as? ArrMovie, let qp = selectedMovieQualityProfileId, let path = selectedMovieRootFolderPath {
                viewModel.updatePreferences(
                    preferences: prefs.doCopy(
                        sortBy: prefs.sortBy,
                        sortOrder: prefs.sortOrder,
                        filterBy: prefs.filterBy,
                        customFilterId: prefs.customFilterId,
                        viewType: prefs.viewType,
                        posterElevation: prefs.posterElevation,
                        posterRadius: prefs.posterRadius,
                        showFullDetails: prefs.showFullDetails,
                        showOverlay: prefs.showOverlay,
                        gridDensity: prefs.gridDensity,
                        gridSpacing: prefs.gridSpacing,
                        showBannerBackground: prefs.showBannerBackground,
                        includeOverview: prefs.includeOverview,
                        bannerBlur: prefs.bannerBlur,
                        applyGlobally: prefs.applyGlobally,
                        addQualityProfileId: qp.asKotlinInt,
                        addRootFolderPath: path,
                        addSearchOnAdd: searchMovieOnAdd,
                        addSeriesMonitor: prefs.addSeriesMonitor,
                        addSeriesType: prefs.addSeriesType,
                        addSeriesSeasonFolder: prefs.addSeriesSeasonFolder,
                        addMovieMonitored: isMovieMonitored,
                        addMovieMinimumAvailability: selectedMovieMinimumAvailability,
                        addArtistMonitor: prefs.addArtistMonitor,
                        addArtistMonitorNew: prefs.addArtistMonitorNew,
                        addAuthorMonitor: prefs.addAuthorMonitor,
                        addAuthorMonitorNew: prefs.addAuthorMonitorNew,
                        addAudiobookMonitored: prefs.addAudiobookMonitored,
                        deleteDeleteFiles: prefs.deleteDeleteFiles,
                        deleteAddExclusion: prefs.deleteAddExclusion
                    )
                )
                let newMovie = movie.doCopyForCreation(
                    monitored: isMovieMonitored,
                    minimumAvailability: selectedMovieMinimumAvailability,
                    qualityProfileId: qp,
                    rootFolderPath: path,
                    tags: Array(selectedMovieTags.map { $0.asKotlinInt })
                )
                viewModel.smartAdd(item: newMovie, searchOnAdd: searchMovieOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
            }
        }
    }
}
