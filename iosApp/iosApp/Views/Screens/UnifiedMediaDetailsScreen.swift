//
//  UnifiedMediaDetailsScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct UnifiedMediaDetailsScreen: View {
    @StateObject private var viewModel: UnifiedMediaDetailsViewModelS
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var navigationManager: NavigationManager

    @State private var showConfirmSheet = false
    @State private var showSeriesMonitoringSheet = false
    @State private var showArtistMonitoringSheet = false
    @State private var showBookMonitoringSheet = false
    @State private var showEditSheet = false
    @State private var showEditPathSheet = false
    @State private var showAddSheet = false
    @State private var moveFilesItem: ArrMedia? = nil
    @State private var editAlbum: ArrAlbum? = nil

    @State private var confirmDeleteSeasonNumber: Int32? = nil
    @State private var confirmDeleteAlbumId: Int64? = nil
    @State private var confirmDeleteEpisodeId: Int64? = nil
    @State private var confirmDeleteMovie = false
    @State private var confirmRemoveFromService = false
    @State private var confirmClearData = false
    @State private var selectedQueueItem: QueueItem? = nil
    @State private var selectedTracearrSession: TracearrStreamSession? = nil

    private let initialEpisodeId: Int64?

    @State private var toastMessage: String? = nil
    @State private var selectedTab: DetailsTab = .overview
    @State private var selectedBookMediaFilter: BookMediaFilterBy = .all
    @State private var previousHasSeasonsOrFiles: Bool? = nil

    private typealias DetailsTab = UnifiedMediaDetailsTab

    private var removeServiceName: String {
        viewModel.buttonState.serviceName ?? (viewModel.resolvedRequestType == RequestType.movie ? "Radarr" : "Sonarr")
    }

    private var seasonsOrFilesTabTitle: String {
        switch viewModel.resolvedInstanceType {
        case .sonarr:
            return MR.strings().seasons_header.localized()
        case .lidarr:
            return MR.strings().albums_header.localized()
        case .bookshelf, .chaptarr:
            return MR.strings().books.localized()
        default:
            return MR.strings().files.localized()
        }
    }

    init(
        arrId: Int64? = nil,
        tmdbId: Int64? = nil,
        tvdbId: Int64? = nil,
        instanceType: InstanceType? = nil,
        requestType: RequestType? = nil,
        instanceId: Int64? = nil,
        initialEpisodeId: Int64? = nil
    ) {
        self.initialEpisodeId = initialEpisodeId
        _viewModel = StateObject(wrappedValue: UnifiedMediaDetailsViewModelS(
            arrId: arrId,
            tmdbId: tmdbId,
            tvdbId: tvdbId,
            instanceType: instanceType,
            requestType: requestType,
            instanceId: instanceId
        ))
    }

    var body: some View {
        ZStack(alignment: .top) {
            contentForState()
            if viewModel.isRefreshing {
                ProgressView()
                    .progressViewStyle(.linear)
                    .tint(.accentColor)
                    .padding(.top, 44)
            }
            toastOverlay
        }
        .ignoresSafeArea(edges: .top)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { toolbarContent }
        .task {
            viewModel.refresh()
            checkInitialEpisode()
        }
        .onReceive(viewModel.$uiState) { _ in
            checkInitialEpisode()
        }
        .modifier(UnifiedMediaDetailsSheetsModifier(
            viewModel: viewModel,
            showEditSheet: $showEditSheet,
            showEditPathSheet: $showEditPathSheet,
            showAddSheet: $showAddSheet,
            showConfirmSheet: $showConfirmSheet,
            showSeriesMonitoringSheet: $showSeriesMonitoringSheet,
            showArtistMonitoringSheet: $showArtistMonitoringSheet,
            showBookMonitoringSheet: $showBookMonitoringSheet,
            moveFilesItem: $moveFilesItem,
            editAlbum: $editAlbum,
            selectedQueueItem: $selectedQueueItem,
            selectedTracearrSession: $selectedTracearrSession,
            initialBookMediaType: selectedBookMediaFilter == .ebook ? .ebook : (selectedBookMediaFilter == .audiobook ? .audiobook : nil)
        ))
        .modifier(UnifiedMediaDetailsArrAlertsModifier(
            viewModel: viewModel,
            confirmDeleteMovie: $confirmDeleteMovie,
            confirmDeleteSeasonNumber: $confirmDeleteSeasonNumber,
            confirmDeleteAlbumId: $confirmDeleteAlbumId,
            confirmDeleteEpisodeId: $confirmDeleteEpisodeId,
            moveFilesItem: $moveFilesItem
        ))
        .modifier(UnifiedMediaDetailsSeerrAlertsModifier(
            viewModel: viewModel,
            confirmRemoveFromService: $confirmRemoveFromService,
            confirmClearData: $confirmClearData,
            removeServiceName: removeServiceName
        ))
        .modifier(UnifiedMediaDetailsEventsModifier(
            viewModel: viewModel,
            onToast: { msg in
                withAnimation { toastMessage = msg }
            },
            onDismiss: { dismiss() },
            onEditSuccess: {
                showEditSheet = false
                editAlbum = nil
            }
        ))
        .modifier(UnifiedMediaDetailsSmartAddSeerrModifier(
            viewModel: viewModel
        ))
    }
}

// MARK: - State Rendering
extension UnifiedMediaDetailsScreen {
    private func checkInitialEpisode() {
        guard let episodeId = initialEpisodeId, !viewModel.hasNavigatedToInitialEpisode(episodeId: episodeId) else { return }
        if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess,
           let series = (success.arrMedia as? ArrSeries) ?? success.episodes.compactMap({ $0.arrEpisode?.series }).first {
            let episodes = success.episodes.compactMap { $0.arrEpisode }
            if let episode = episodes.first(where: { $0.id == episodeId }) {
                viewModel.markInitialEpisodeNavigated(episodeId: episodeId)
                navigationManager.go(to: .episodeDetails(series.toJson(), episode.toJson(), instanceId: success.selectedInstanceId?.int64Value), of: .sonarr)
            }
        }
    }

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
        let tracearrState = viewModel.tracearrState
        let isMovieOrTv = success.isMovieOrTv
        let hasSeasonsOrFiles = success.hasSeasonsOrFiles
        let hasTracearr = success.hasTracearr(isTracearrConfigured: tracearrState.isTracearrConfigured)

        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                UnifiedMediaDetailsHeader(success: success, type: viewModel.resolvedInstanceType)

                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(success.displayTitle ?? MR.strings().unknown.localized())
                            .font(.title.weight(.bold))
                            .foregroundStyle(.primary)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        if let tagline = success.tagline, !tagline.isEmpty {
                            Text(tagline)
                                .font(.subheadline)
                                .italic()
                                .foregroundStyle(.secondary)
                        }

                        if let airingString = success.upcomingDateString {
                            Text(airingString)
                                .font(.subheadline.weight(.semibold))
                                .foregroundStyle(Color.accentColor)
                        }

                        if isMovieOrTv {
                            TracearrSummaryChipRowView(uiState: tracearrState)
                                .padding(.top, 4)
                        }

                        if success.availableInstances.count > 1 && !success.instancePresences.isEmpty {
                            InstancePresenceChipsView(
                                presences: success.instancePresences,
                                selectedInstanceId: success.selectedInstanceId?.int64Value,
                                onSelectInstance: { instId in
                                    withAnimation(.easeInOut(duration: 0.3)) {
                                        viewModel.selectInstance(instanceId: instId)
                                    }
                                },
                                onAddInstance: { missingInst in
                                    viewModel.setAddSheetTargetInstance(instance: missingInst)
                                    showAddSheet = true
                                }
                            )
                            .padding(.top, 4)
                        }
                    }

                    if hasSeasonsOrFiles || success.hasArrId || hasTracearr {
                        Picker("View Mode", selection: $selectedTab) {
                            if hasSeasonsOrFiles {
                                Text(seasonsOrFilesTabTitle)
                                    .tag(DetailsTab.seasonsFiles)
                            }
                            Text(MR.strings().overview.localized())
                                .tag(DetailsTab.overview)
                            if success.hasArrId {
                                Text(MR.strings().activity.localized())
                                    .tag(DetailsTab.activity)
                                    .badge(success.queueItems.count)
                            }
                            if hasTracearr {
                                Text(MR.strings().statistics.localized())
                                    .tag(DetailsTab.analytics)
                                Text(MR.strings().streams_label.localized())
                                    .tag(DetailsTab.steams)
                            }
                        }
                        .pickerStyle(.segmented)
                    }

                    switch selectedTab {
                    case .seasonsFiles:
                        seasonsAndFilesTabContent(success)
                    case .overview:
                        overviewTabContent(success)
                    case .activity:
                        VStack(alignment: .leading, spacing: 16) {
                            if !success.queueItems.isEmpty {
                                VStack(alignment: .leading, spacing: 12) {
                                    Text(MR.strings().activity.localized())
                                        .font(.title3.bold())
                                    ForEach(success.queueItems, id: \.id) { item in
                                        ActivityQueueItem(item: item, onClick: { selectedQueueItem = item })
                                    }
                                }
                            }

                            Text(MR.strings().history.localized())
                                .font(.title3.bold())

                            if success.history.isEmpty {
                                Text(MR.strings().no_history.localized())
                                    .font(.system(size: 14))
                                    .foregroundColor(.secondary)
                                    .frame(maxWidth: .infinity, alignment: .center)
                                    .padding(.vertical, 12)
                            } else {
                                ForEach(success.history, id: \.id) { historyItem in
                                    HistoryItemView(item: historyItem)
                                }
                            }
                        }
                    case .analytics:
                        TracearrAnalyticsSectionView(
                            uiState: tracearrState,
                            onWindowSelected: { viewModel.selectTracearrStatsWindow(window: $0) }
                        )
                    case .steams:
                        TracearrHistorySectionView(
                            uiState: tracearrState,
                            onLoadMore: { viewModel.loadMoreTracearrHistory() },
                            onClickItem: { selectedTracearrSession = $0.toStreamSession() }
                        )
                    }
                }
                .padding(.top, 12)
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
                .animation(.spring(response: 0.35, dampingFraction: 0.8), value: selectedTab)
                .animation(.easeInOut(duration: 0.3), value: success.selectedInstanceId?.int64Value)
            }
        }
        .onChange(of: hasSeasonsOrFiles, initial: true) { _, newValue in
            if previousHasSeasonsOrFiles == nil {
                if newValue {
                    selectedTab = .seasonsFiles
                }
            } else if previousHasSeasonsOrFiles == false && newValue == true {
                withAnimation {
                    selectedTab = .seasonsFiles
                }
            } else if previousHasSeasonsOrFiles == true && newValue == false {
                if selectedTab == .seasonsFiles {
                    withAnimation {
                        selectedTab = .overview
                    }
                }
            }
            previousHasSeasonsOrFiles = newValue
        }
        .onChange(of: hasTracearr) { _, newValue in
            if !newValue && (selectedTab == .analytics || selectedTab == .steams) {
                withAnimation {
                    selectedTab = success.defaultTab
                }
            }
        }
        .refreshable {
            viewModel.refresh()
        }
        .ignoresSafeArea(edges: .top)
    }

    @ViewBuilder
    private func overviewTabContent(_ success: UnifiedMediaDetailsUiStateSuccess) -> some View {
        OverviewTabContentView(
            success: success,
            viewModel: viewModel,
            onEditPathClick: { showEditPathSheet = true },
            onPersonClick: { personId in
                navigationManager.goToSeerrDetails(tmdbId: personId, requestType: .person)
            }
        )
    }

    @ViewBuilder
    private func seasonsAndFilesTabContent(_ success: UnifiedMediaDetailsUiStateSuccess) -> some View {
        SeasonsFilesTabContentView(
            success: success,
            viewModel: viewModel,
            onSelectQueueItem: { selectedQueueItem = $0 },
            onConfirmDeleteSeasonNumber: { confirmDeleteSeasonNumber = $0 },
            onConfirmDeleteEpisodeId: { confirmDeleteEpisodeId = $0 },
            onConfirmDeleteMovie: { confirmDeleteMovie = true },
            onEditAlbum: { editAlbum = $0 },
            onConfirmDeleteAlbumId: { confirmDeleteAlbumId = $0 },
            onNavigateToEpisodeDetails: { series, episode in
                navigationManager.go(to: .episodeDetails(series.toJson(), episode.toJson(), instanceId: success.selectedInstanceId?.int64Value), of: .sonarr)
            },
            onNavigateToSeriesRelease: { sId, seasonNum, epId in
                if let sId = sId {
                    let instId = success.selectedInstanceId?.int64Value
                    let route: MediaRoute = .seriesReleases(seriesId: sId, seasonNumber: seasonNum, episodeId: epId, instanceId: instId)
                    navigationManager.go(to: route, of: .sonarr)
                }
            },
            selectedBookMediaFilter: selectedBookMediaFilter,
            onSelectBookMediaTypeFilter: { selectedBookMediaFilter = $0 },
            onEditAuthor: { showEditSheet = true }
        )
    }
}

// MARK: - Toolbar Content
extension UnifiedMediaDetailsScreen {
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess {
            ToolbarItem(placement: .navigationBarTrailing) {
                UnifiedMediaDetailsToolbarTrailingView(
                    success: success,
                    viewModel: viewModel,
                    onShowEditSheet: { showEditSheet = true },
                    onShowConfirmSheet: { showConfirmSheet = true },
                    onShowAddSheet: { showAddSheet = true },
                    onShowAddSheetForInstance: { instance in
                        viewModel.setAddSheetTargetInstance(instance: instance)
                        showAddSheet = true
                    },
                    onConfirmDeleteFile: {
                        if viewModel.resolvedInstanceType == .radarr {
                            confirmDeleteMovie = true
                        }
                    },
                    onConfirmRemoveFromService: { confirmRemoveFromService = true },
                    onConfirmClearData: { confirmClearData = true },
                    onAddNewInstance: { instanceType in
                        navigationManager.goToNewInstance(of: instanceType)
                    },
                    onShowMonitoring: {
                        if viewModel.resolvedInstanceType == .sonarr {
                            showSeriesMonitoringSheet = true
                        } else if viewModel.resolvedInstanceType == .lidarr {
                            showArtistMonitoringSheet = true
                        } else if viewModel.resolvedInstanceType == .bookshelf || viewModel.resolvedInstanceType == .chaptarr {
                            showBookMonitoringSheet = true
                        }
                    }
                )
            }
        }
    }
}

// MARK: - Toast Overlay
extension UnifiedMediaDetailsScreen {
    @ViewBuilder
    private var toastOverlay: some View {
        if let message = toastMessage {
            VStack {
                Spacer()
                Text(message)
                    .font(.subheadline.weight(.medium))
                    .foregroundStyle(.primary)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .floatingCapsuleBackground(material: .regularMaterial)
                    .padding(.bottom, 24)
            }
            .transition(.move(edge: .bottom).combined(with: .opacity))
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                        toastMessage = nil
                    }
                }
            }
        }
    }
}
