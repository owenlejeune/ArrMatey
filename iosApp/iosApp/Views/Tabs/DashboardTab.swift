//
//  DashboardTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-06-11.
//

import Shared
import SwiftUI
import Combine

struct DashboardTab: View {
    var body: some View {
        DashboardTabContent()
    }
}

struct DashboardTabContent: View {
    @StateObject private var viewModel = DashboardViewModelS()
    @StateObject private var discoverViewModel = DiscoverViewModelS()
    @StateObject private var activityViewModel = ActivityQueueViewModelS()
    @StateObject private var requestsViewModel = RequestsViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var showAddCardSheet = false
    @State private var showHealthSheet = false
    @State private var showSeerrSheet = false
    @State private var draggedCard: DashboardCards?
    @State private var searchQuery = ""

    @State private var selectedRequestForSheet: MediaRequestPackage? = nil
    @State private var selectedIssueForSheet: MediaIssuePackage? = nil
    @State private var selectedActivityItem: IdentifiableQueueItem? = nil
    @State private var selectedTracearrStreamSession: TracearrStreamSession? = nil
    @State private var selectedMediaForRequest: DiscoverResult? = nil

    @State private var toastMessage: String? = nil

    private let columns = [
        GridItem(.adaptive(minimum: 300, maximum: .infinity), spacing: 16)
    ]

    private var availableCards: [DashboardCards] {
        DashboardCards.allCases.filter { card in
            !viewModel.cards.contains(where: { $0.name == card.name })
        }
    }

    var body: some View {
        Group {
            if viewModel.showDashboardSearch && !searchQuery.isEmpty {
                DiscoverSearchOverlay(
                    items: discoverViewModel.searchResults,
                    isLoading: discoverViewModel.isSearching,
                    showBanners: discoverViewModel.searchShowBanners,
                    onItemClick: { result in
                        handleSearchItemClick(result)
                    }
                )
            } else {
                ZStack {
                    if let success = viewModel.state as? CombinedDashboardStateSuccess {
                        if viewModel.cards.isEmpty {
                            emptyView
                        } else {
                            dashboardGrid(success)
                        }
                    } else if viewModel.state is CombinedDashboardStateLoading {
                        ProgressView()
                    }
                }
            }
        }
        .navigationTitle(MR.strings().dashboard.localized())
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $searchQuery, placement: .navigationBarDrawer(displayMode: .always))
        .onChange(of: searchQuery) { _, newValue in
            if viewModel.showDashboardSearch && !viewModel.isEditing {
                discoverViewModel.updateSearchQuery(newValue)
            }
        }
        .onChange(of: viewModel.isEditing) { _, isEditing in
            if isEditing {
                searchQuery = ""
            }
        }
        .onChange(of: viewModel.showFirstLaunchAlert) { _, show in
            if show {
                toastMessage = MR.strings().dashboard_first_launch.localized()
                viewModel.setFirstLaunchComplete()
            }
        }
        .overlay(alignment: .bottom) {
            if let message = toastMessage {
                ToastView(message: message)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                            withAnimation { toastMessage = nil }
                        }
                    }
                    .padding(.bottom, 16)
            }
        }
        .animation(.easeInOut(duration: 0.3), value: toastMessage != nil)
        .toolbar {
            if !viewModel.isEditing && navigationManager.shouldShowDrawerButton(for: TabItemStandard.dashboard.key) {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        navigationManager.showLauncher = true
                    } label: {
                        Image(systemName: "line.3.horizontal")
                    }
                }
            }

            if viewModel.isEditing {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(MR.strings().close.localized()) {
                        viewModel.toggleEditing()
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    HStack(spacing: 16) {
                        Button(action: { viewModel.toggleDashboardSearch() }) {
                            Image(systemName: viewModel.showDashboardSearch ? "magnifyingglass" : "magnifyingglass.slash")
                        }

                        Button(action: { viewModel.resetCardsOrder() }) {
                            Image(systemName: "arrow.counterclockwise")
                        }

                        if !availableCards.isEmpty {
                            Button(action: { showAddCardSheet = true }) {
                                Image(systemName: "plus")
                            }
                        }
                    }
                }
            } else {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(MR.strings().edit.localized()) {
                        viewModel.toggleEditing()
                    }
                }
            }
        }
        .sheet(isPresented: $showAddCardSheet) {
            AddDashboardCardSheet(viewModel: viewModel)
        }
        .sheet(item: Binding(
            get: { selectedRequestForSheet.map { IdentifiableRequestPackage(package: $0) } },
            set: { selectedRequestForSheet = $0?.package }
        )) { wrapper in
            if let details = wrapper.package.details {
                SeerrViewRequestSheet(
                    details: details,
                    request: wrapper.package.request,
                    serviceDetails: wrapper.package.serviceDetails,
                    onDismissRequest: { selectedRequestForSheet = nil },
                    onApproveRequest: { requestId, profileId, rootFolder, languageProfileId, seasons in
                        viewModel.approveRequest(requestId: requestId, profileId: profileId, rootFolder: rootFolder, languageProfileId: languageProfileId, seasons: seasons)
                        selectedRequestForSheet = nil
                    },
                    onDeclineRequest: { requestId in
                        viewModel.declineRequest(requestId: requestId)
                        selectedRequestForSheet = nil
                    },
                    onViewMedia: { tmdbId, type in
                        selectedRequestForSheet = nil
                        navigationManager.goToSeerrDetailsOnDashboard(tmdbId: tmdbId, requestType: type)
                    }
                )
            }
        }
        .sheet(item: Binding(
            get: { selectedIssueForSheet.map { IdentifiableIssue(package: $0) } },
            set: { selectedIssueForSheet = $0?.package }
        )) { wrapper in
            SeerrIssueDetailsSheet(
                issuePackage: wrapper.package,
                onDismiss: { selectedIssueForSheet = nil },
                onIssueClosed: {
                    selectedIssueForSheet = nil
                    viewModel.refresh()
                }
            )
        }
        .sheet(item: $selectedActivityItem) { wrapper in
            QueueItemInfoSheet(
                item: wrapper.item,
                deleteInProgress: activityViewModel.removeInProgress,
                onDelete: { remove, block, skip in
                    activityViewModel.removeQueueItem(wrapper.item, remove, block, skip)
                }
            )
            .presentationDetents([.fraction(0.7)])
        }
        .sheet(isPresented: $showHealthSheet) {
            if let success = viewModel.state as? CombinedDashboardStateSuccess {
                HealthNoticesSheet(instances: success.instances)
            }
        }
        .sheet(isPresented: $showSeerrSheet) {
            SeerrSheetView(viewModel: requestsViewModel)
        }
        .sheet(item: $selectedTracearrStreamSession) { session in
            TracearrStreamDetailsSheet(
                session: session,
                onNavigateToDetails: { type, tmdbId in
                    selectedTracearrStreamSession = nil
                    if let tmdbId = tmdbId, let reqType = type?.requestType {
                        navigationManager.goToSeerrDetailsOnDashboard(tmdbId: tmdbId, requestType: reqType)
                    }
                },
                onNavigateToUser: { _ in
                    selectedTracearrStreamSession = nil
                    navigationManager.go(to: TracearrRoute.users)
                }
            )
        }
        .sheet(item: Binding(
            get: { selectedMediaForRequest.map { IdentifiableDiscoverResult(result: $0) } },
            set: { selectedMediaForRequest = $0?.result }
        )) { wrapper in
            MediaRequestOrAddSheet(
                item: wrapper.result,
                onDismiss: { selectedMediaForRequest = nil }
            )
        }
    }

    @ViewBuilder
    private func dashboardGrid(_ state: CombinedDashboardStateSuccess) -> some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 16) {
                ForEach(viewModel.cards, id: \.self) { card in
                    DashboardCardWrapper(
                        card: card,
                        state: state,
                        isEditing: viewModel.isEditing,
                        onRequestClick: { selectedRequestForSheet = $0 },
                        onIssueClick: { selectedIssueForSheet = $0 },
                        onActivityClick: { selectedActivityItem = IdentifiableQueueItem(item: $0) },
                        onStreamClick: { selectedTracearrStreamSession = $0 },
                        onHealthClick: { showHealthSheet = true },
                        onSeerrRequestsStatClick: {
                            requestsViewModel.setSelectedTab(.requests)
                            showSeerrSheet = true
                        },
                        onSeerrIssuesStatClick: {
                            requestsViewModel.setSelectedTab(.issues)
                            showSeerrSheet = true
                        },
                        onShuffleQuickPick: {
                            viewModel.shuffleQuickPick()
                        },
                        onMediaRequestClick: { item in
                            selectedMediaForRequest = item
                        },
                        visibleCategories: viewModel.discoverSectionPreferences.visibleCategories
                    ) {
                        viewModel.removeCard(card: card)
                    }
                    .onTapGesture {
                        if !viewModel.isEditing {
                            Task { @MainActor in
                                handleCardClick(card)
                            }
                        }
                    }
                    .onLongPressGesture {
                        if !viewModel.isEditing {
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            viewModel.toggleEditing()
                        }
                    }
                    .onDrag {
                        if !viewModel.isEditing {
                            viewModel.toggleEditing()
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        }
                        self.draggedCard = card
                        return NSItemProvider(object: card.name as NSString)
                    }
                    .onDrop(of: [.text], delegate: DashboardDropDelegate(item: card, items: $viewModel.cards, draggedItem: $draggedCard) { newOrder in
                        viewModel.saveCardOrder(cards: newOrder)
                    })
                }
            }
            .padding()
        }
        .refreshable {
            viewModel.refresh()
        }
    }

    private func handleCardClick(_ card: DashboardCards) {
        switch card {
        case .arrOverview: navigationManager.openSettings()
        case .seerrOverview, .pendingRequests, .pendingIssues: navigationManager.openRequestsTab()
        case .prowlarrOverview: navigationManager.openProwlarrTab()
        case .bazarrOverview: navigationManager.openBazarrTab()
        case .tracearrOverview, .tracearrActiveStreams: navigationManager.openTracearrTab()
        case .downloadClients: navigationManager.openDownloadsTab()
        case .activityQueue: navigationManager.openActivityTab()
        case .onToday, .upcomingReleases: navigationManager.openScheduleTab()
        case .discoverFeed: navigationManager.openDiscoverTab()
        default: break
        }
    }

    private func handleSearchItemClick(_ result: SearchResult) {
        if let arrResult = result as? SearchResultArrMediaResult {
            navigationManager.goToArrDetailsOrPreviewOnDashboard(item: arrResult.media, type: arrResult.instanceType, instanceId: arrResult.instanceId?.int64Value)
        } else if let seerrMedia = result as? SearchResultSeerrMediaResult {
            navigationManager.goToSeerrDetailsOnDashboard(tmdbId: seerrMedia.result.id, requestType: seerrMedia.result.mediaType)
        } else if let seerrPerson = result as? SearchResultSeerrPersonResult {
            navigationManager.goToPersonDetailsOnDashboard(id: seerrPerson.result.id)
        }
    }

    private var emptyView: some View {
        VStack(spacing: 16) {
            Text(MR.strings().empty_library.localized())
                .font(.title)
            Text(MR.strings().empty_dashboard_message.localized())
                .multilineTextAlignment(.center)
                .foregroundColor(.secondary)
            Button(MR.strings().add.localized()) {
                showAddCardSheet = true
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}

struct DashboardDropDelegate: DropDelegate {
    let item: DashboardCards
    @Binding var items: [DashboardCards]
    @Binding var draggedItem: DashboardCards?
    let onOrderChanged: ([DashboardCards]) -> Void

    func performDrop(info: DropInfo) -> Bool {
        onOrderChanged(items)
        draggedItem = nil
        return true
    }

    func dropEntered(info: DropInfo) {
        guard let draggedItem = draggedItem else { return }
        if draggedItem != item {
            let from = items.firstIndex(of: draggedItem)!
            let to = items.firstIndex(of: item)!

            if items[to] != draggedItem {
                items.move(fromOffsets: IndexSet(integer: from), toOffset: to > from ? to + 1 : to)
            }
        }
    }
}

struct DashboardCardWrapper: View {
    let card: DashboardCards
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onRequestClick: ((MediaRequestPackage) -> Void)? = nil
    var onIssueClick: ((MediaIssuePackage) -> Void)? = nil
    var onActivityClick: ((QueueItem) -> Void)? = nil
    var onStreamClick: ((TracearrStreamSession) -> Void)? = nil
    var onHealthClick: (() -> Void)? = nil
    var onSeerrRequestsStatClick: (() -> Void)? = nil
    var onSeerrIssuesStatClick: (() -> Void)? = nil
    var onShuffleQuickPick: (() -> Void)? = nil
    var onMediaRequestClick: ((DiscoverResult) -> Void)? = nil
    var visibleCategories: [DiscoverCategory] = []
    let onRemove: () -> Void

    var body: some View {
        ZStack(alignment: .topTrailing) {
            DashboardCardView(
                card: card,
                state: state,
                isEditing: isEditing,
                onRequestClick: onRequestClick,
                onIssueClick: onIssueClick,
                onActivityClick: onActivityClick,
                onStreamClick: onStreamClick,
                onHealthClick: onHealthClick,
                onSeerrRequestsStatClick: onSeerrRequestsStatClick,
                onSeerrIssuesStatClick: onSeerrIssuesStatClick,
                onShuffleQuickPick: onShuffleQuickPick,
                onMediaRequestClick: onMediaRequestClick,
                visibleCategories: visibleCategories
            )
            .padding(12)
            .background(Color(UIColor.systemBackground).midpoint(with: Color(UIColor.secondarySystemBackground)))
            .cornerRadius(12)

            if isEditing {
                Button(action: onRemove) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.red)
                        .background(Color.white)
                        .clipShape(Circle())
                }
                .padding(4)
                .offset(x: 8, y: -8)
            }
        }
    }
}

struct DashboardCardView: View {
    let card: DashboardCards
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onRequestClick: ((MediaRequestPackage) -> Void)? = nil
    var onIssueClick: ((MediaIssuePackage) -> Void)? = nil
    var onActivityClick: ((QueueItem) -> Void)? = nil
    var onStreamClick: ((TracearrStreamSession) -> Void)? = nil
    var onHealthClick: (() -> Void)? = nil
    var onSeerrRequestsStatClick: (() -> Void)? = nil
    var onSeerrIssuesStatClick: (() -> Void)? = nil
    var onShuffleQuickPick: (() -> Void)? = nil
    var onMediaRequestClick: ((DiscoverResult) -> Void)? = nil
    var visibleCategories: [DiscoverCategory] = []

    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        Group {
            switch card {
            case .arrOverview: DashboardOverviewSection(state: state, isEditing: isEditing, onHealthClick: onHealthClick)
            case .seerrOverview: DashboardSeerrSection(state: state, isEditing: isEditing, onRequestClick: onSeerrRequestsStatClick, onIssueClick: onSeerrIssuesStatClick)
            case .pendingRequests: DashboardPendingRequestsSection(state: state, isEditing: isEditing, onRequestClick: onRequestClick)
            case .pendingIssues: DashboardPendingIssuesSection(state: state, isEditing: isEditing, onIssueClick: onIssueClick)
            case .prowlarrOverview: DashboardProwlarrSection(state: state, isEditing: isEditing)
            case .network: DashboardNetworkSection(state: state, isEditing: isEditing)
            case .recentlyAdded: DashboardRecentlyAddedSection(state: state)
            case .downloadClients: DashboardDownloadClientsSection(state: state, isEditing: isEditing)
            case .activityQueue: DashboardActivityQueueSection(state: state, isEditing: isEditing, onItemClick: onActivityClick)
            case .onToday: DashboardTodaySection(state: state, isEditing: isEditing)
            case .upcomingReleases: DashboardUpcomingSection(state: state, isEditing: isEditing)
            case .bazarrOverview: DashboardBazarrSection(state: state, isEditing: isEditing)
            case .tracearrOverview: DashboardTracearrSection(state: state, isEditing: isEditing)
            case .tracearrActiveStreams: DashboardActiveStreamsSection(state: state, isEditing: isEditing, onItemClick: onStreamClick)
            case .instanceDashboard: DashboardInstanceDashboardSection(state: state, isEditing: isEditing)
            case .discoverFeed:
                DashboardDiscoverFeedSection(
                    state: state,
                    isEditing: isEditing,
                    visibleCategories: visibleCategories,
                    onMediaClick: { id, type in
                        if !isEditing {
                            navigationManager.goToSeerrDetailsOnDashboard(tmdbId: id, requestType: type)
                        }
                    }
                )
            case .discoverSpotlight:
                DashboardDiscoverSpotlightSection(
                    state: state,
                    isEditing: isEditing,
                    onMediaClick: { id, type in
                        if !isEditing {
                            navigationManager.goToSeerrDetailsOnDashboard(tmdbId: id, requestType: type)
                        }
                    },
                    onRequestClick: { item in
                        if !isEditing {
                            onMediaRequestClick?(item)
                        }
                    }
                )
            case .discoverQuickPick:
                DashboardDiscoverQuickPickSection(
                    state: state,
                    isEditing: isEditing,
                    onShuffleClick: {
                        if !isEditing {
                            onShuffleQuickPick?()
                        }
                    },
                    onMediaClick: { id, type in
                        if !isEditing {
                            navigationManager.goToSeerrDetailsOnDashboard(tmdbId: id, requestType: type)
                        }
                    },
                    onRequestClick: { item in
                        if !isEditing {
                            onMediaRequestClick?(item)
                        }
                    }
                )
            }
        }
    }
}

struct StatCard: View {
    let icon: String
    let label: String
    let value: String
    let color: Color
    var onClick: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(color)
                Text(label)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Text(value)
                .font(.headline)
                .bold()
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(color.opacity(0.1))
        .cornerRadius(12)
        .contentShape(Rectangle())
        .onTapGesture {
            onClick?()
        }
    }
}

struct DashboardOverviewSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onHealthClick: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing {
                HStack {
                    Image(systemName: "harddrive")
                    Text(MR.strings().dashboard_arr_overview.localized())
                        .font(.headline)
                        .bold()
                }
            }

            let totalSize = state.instances.reduce(0) { $0 + $1.sizeOnDisk }
            let totalIssues = state.instances.reduce(0) { $0 + $1.healthItems.count }
            let criticalIssues = state.instances
                .flatMap { $0.healthItems }
                .filter { $0.type == .error }
                .count

            HStack(spacing: 12) {
                StatCard(
                    icon: "internaldrive",
                    label: MR.strings().total_space.localized(),
                    value: totalSize.bytesAsFileSizeString(),
                    color: .blue
                )

                let issueColor: Color = if criticalIssues > 0 {
                    .arrRed
                } else if totalIssues > 0 {
                    .arrOrange
                } else {
                    .secondary
                }
                StatCard(
                    icon: totalIssues > 0 ? "exclamationmark.triangle" : "checkmark.circle",
                    label: MR.strings().health.localized(),
                    value: totalIssues == 0 ? MR.strings().no_issues.localized() : "\(totalIssues) Issues",
                    color: issueColor,
                    onClick: isEditing ? nil : onHealthClick
                )
            }
        }
    }
}

struct DashboardSeerrSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onRequestClick: (() -> Void)? = nil
    var onIssueClick: (() -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing {
                HStack {
                    Image(systemName: "tray")
                    Text(MR.strings().dashboard_seerr_overview.localized())
                        .font(.headline)
                        .bold()
                }
            }

            let totalRequests = state.seerrInstances.reduce(0) { $0 + Int($1.pendingRequestsCount) }
            let totalIssues = state.seerrInstances.reduce(0) { $0 + Int($1.openIssuesCount) }

            HStack(spacing: 12) {
                StatCard(icon: "tray", label: MR.strings().dashboard_pending_requests.localized(), value: "\(totalRequests)", color: .purple, onClick: isEditing ? nil : onRequestClick)
                StatCard(icon: "ladybug", label: MR.strings().dashboard_pending_issues.localized(), value: "\(totalIssues)", color: totalIssues > 0 ? .red : .secondary, onClick: isEditing ? nil : onIssueClick)
            }
        }
    }
}

struct DashboardProwlarrSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing {
                HStack {
                    Image(systemName: "magnifyingglass")
                    Text(MR.strings().dashboard_prowlarr_overview.localized())
                        .font(.headline)
                        .bold()
                }
            }

            let healthy = state.prowlarrStats.reduce(0) { $0 + Int($1.healthyIndexers) }
            let failing = state.prowlarrStats.reduce(0) { $0 + Int($1.failingIndexers) }

            HStack(spacing: 12) {
                StatCard(icon: "heart", label: MR.strings().healthy_indexers.localized(), value: "\(healthy)", color: .green)
                StatCard(icon: "exclamationmark.octagon", label: MR.strings().failing_indexers.localized(), value: "\(failing)", color: failing > 0 ? .red : .secondary)
            }
        }
    }
}

struct DashboardBazarrSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing {
                HStack {
                    Image(resource: InstanceType.bazarr.icon)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 24, height: 24)
                    Text(MR.strings().dashboard_bazarr_overview.localized())
                        .font(.headline)
                        .bold()
                }
            }

            let totalEpisodes = state.bazarrStats.reduce(0) { $0 + Int($1.wantedEpisodesCount) }
            let totalMovies = state.bazarrStats.reduce(0) { $0 + Int($1.wantedMoviesCount) }

            HStack(spacing: 12) {
                StatCard(icon: "tv", label: MR.strings().bazarr_wanted_episodes.localized(), value: "\(totalEpisodes)", color: .blue)
                StatCard(icon: "film", label: MR.strings().bazarr_wanted_movies.localized(), value: "\(totalMovies)", color: .secondary)
            }
        }
    }
}

struct DashboardTracearrSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    @EnvironmentObject private var navigationManager: NavigationManager
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    private var isLargeScreen: Bool { horizontalSizeClass == .regular }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing {
                HStack(spacing: 8) {
                    Image(resource: InstanceType.tracearr.icon)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 24, height: 24)
                    Text(MR.strings().dashboard_tracearr_overview.localized())
                        .font(.headline)
                        .bold()
                }
            }

            let rawStats = state.tracearrStats.compactMap { $0.stats }
            let statsList = rawStats.isEmpty ? [
                TracearrTodayStats(activeStreams: 0, todayPlays: 0, todaySessions: 0, watchTimeHours: 0, alertsLast24h: 0, activeUsersToday: 0, timestamp: nil)
            ] : rawStats

            ForEach(statsList.indices, id: \.self) { index in
                let stats = statsList[index]
                TracearrDashboardStatsView(
                    stats: stats,
                    isExpanded: false,
                    showTodayHeader: !isEditing,
                    onNavigateToHistory: { navigationManager.go(to: TracearrRoute.history) },
                    onNavigateToAllUsers: { navigationManager.go(to: TracearrRoute.users) },
                    onNavigateToViolations: { navigationManager.go(to: TracearrRoute.violations) },
                    onNavigateToActivity: { navigationManager.go(to: TracearrRoute.activity) }
                )
            }
        }
    }
}

struct DashboardNetworkSection: View {
    let state: CombinedDashboardStateSuccess
    var isEditing: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: "wifi")
                    .font(.system(size: 20))
                Text(MR.strings().network_status.localized())
                    .font(.headline)
                    .bold()

                Spacer()

                if !isEditing, let ssid = state.networkStatus?.ssid {
                    Text(ssid)
                        .font(.system(size: 10, weight: .bold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(UIColor.tertiarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                }
            }

            Divider()
                .opacity(0.5)

            if let statuses = state.networkStatus?.instanceStatuses {
                VStack(spacing: 12) {
                    ForEach(statuses, id: \.instanceName) { status in
                        HStack(spacing: 12) {
                            Image(resource: status.icon)
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 24, height: 24)

                            VStack(alignment: .leading, spacing: 2) {
                                Text(status.instanceName)
                                    .font(.subheadline)
                                    .bold()
                                Text(status.currentEndpoint)
                                    .font(.system(size: 10))
                                    .foregroundColor(.secondary)
                                    .lineLimit(1)
                            }

                            Spacer()

                            HStack(spacing: 4) {
                                Text(status.isOnline ? MR.strings().online.localized() : MR.strings().offline.localized())
                                    .font(.system(size: 10, weight: .bold))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(status.isOnline ? Color.green.opacity(0.1) : Color.red.opacity(0.1))
                                    .foregroundColor(status.isOnline ? .green : .red)
                                    .clipShape(RoundedRectangle(cornerRadius: 4))

                                if (status.isLocalSwitchingEnabled) {
                                    Text(status.isLocal ? MR.strings().local_network.localized() : MR.strings().remote_vpn.localized())
                                        .font(.system(size: 10, weight: .bold))
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(status.isLocal ? Color.blue.opacity(0.1) : Color.purple.opacity(0.1))
                                        .foregroundColor(status.isLocal ? .blue : .purple)
                                        .clipShape(RoundedRectangle(cornerRadius: 4))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

struct DashboardRecentlyAddedSection: View {
    let state: CombinedDashboardStateSuccess
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "clock")
                Text(MR.strings().recently_added.localized())
                    .font(.headline)
                    .bold()
            }

            if state.recentlyAdded.isEmpty {
                Text(MR.strings().nothing_recently_added.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(state.recentlyAdded, id: \.id) { item in
                            let identifiable = item as? InstanceTypeIdentifiable
                            let type = identifiable?.instanceType
                            let isWide = (type == .lidarr || type == .bookshelf || type == .listenarr)
                            let ratio: Shared.AspectRatio = isWide ? .cover : .poster
                            let width: CGFloat = isWide ? 150 : 100

                            PosterItem(item: item, instanceType: type, aspectRatio: ratio, elevation: .none, posterHeight: 150, showFooter: true) { clickedItem in
                                if let type = type, let id = clickedItem.id {
                                    navigationManager.goToDetailsOnDashboard(arrId: id.int64Value, instanceType: type)
                                }
                            }
                            .frame(width: width)
                        }
                    }
                }
            }
        }
    }
}

struct DashboardDownloadClientsSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "arrow.down.circle")
                Text(MR.strings().download_clients.localized())
                    .font(.headline)
                    .bold()
            }

            ForEach(state.downloadClients, id: \.client.id) { clientState in
                HStack {
                    VStack(alignment: .leading) {
                        Text(clientState.client.label)
                            .font(.subheadline)
                            .bold()
                        Text("\(clientState.activeDownloadsCount) Downloads")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                    if let transfer = clientState.transferInfo {
                        VStack(alignment: .trailing) {
                            Text(transfer.downloadSpeed.bytesAsFileSizeString() + "/s")
                                .font(.caption)
                                .bold()
                                .foregroundColor(.green)
                            Text(transfer.uploadSpeed.bytesAsFileSizeString() + "/s")
                                .font(.caption)
                                .foregroundColor(.blue)
                        }
                    }
                }
                .padding(12)
                .background(Color(UIColor.tertiarySystemBackground))
                .cornerRadius(12)
            }

            if !state.activeDownloads.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    Text(MR.strings().activity.localized())
                        .font(.caption)
                        .bold()
                        .foregroundColor(.secondary)

                    ForEach(state.activeDownloads.prefix(5), id: \.id) { download in
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Text(download.name)
                                    .font(.system(size: 12))
                                    .bold()
                                    .lineLimit(1)
                                Spacer()
                                Text("\(Int(download.progress * 100))%")
                                    .font(.system(size: 10))
                                    .bold()
                            }

                            ProgressView(value: download.progress)
                                .progressViewStyle(.linear)
                                .tint(.green)

                            HStack {
                                Text("\(download.downloaded.bytesAsFileSizeString()) / \(download.size.bytesAsFileSizeString())")
                                    .font(.system(size: 8))
                                    .foregroundColor(.secondary)
                                Spacer()
                                if download.downloadSpeed > 0 {
                                    Text("\(download.downloadSpeed.bytesAsFileSizeString())/s")
                                        .font(.system(size: 8))
                                        .foregroundColor(.green)
                                }
                            }
                        }
                        .padding(8)
                        .background(Color(UIColor.tertiarySystemBackground).opacity(0.5))
                        .cornerRadius(8)
                    }
                }
                .padding(.top, 4)

                if state.activeDownloads.count > 5 {
                    HStack {
                        Spacer()
                        Text(MR.strings().additional_items_count.formatted(args: [Int32(state.activeDownloads.count - 5)]))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }
}

struct DashboardActivityQueueSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onItemClick: ((QueueItem) -> Void)? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "list.bullet")
                Text(MR.strings().activity.localized())
                    .font(.headline)
                    .bold()
            }

            if state.activityQueue.isEmpty {
                Text(MR.strings().no_activity.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ForEach(state.activityQueue.prefix(5), id: \.id) { item in
                    Button(action: {
                        if !isEditing {
                            onItemClick?(item)
                        }
                    }) {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                HStack(spacing: 6) {
                                    Text(item.titleLabel)
                                        .font(.subheadline)
                                        .bold()
                                        .lineLimit(1)
                                    if let groupCount = item.taskGroupCount?.intValue, groupCount > 1 {
                                        Text(MR.strings().additional_items_count.formatted(args: [groupCount]))
                                            .font(.caption2.bold())
                                            .foregroundColor(.accentColor)
                                    }
                                }
                                HStack(spacing: 4) {
                                    if let instanceName = item.instanceName {
                                        Text("\(instanceName) •")
                                            .font(.caption)
                                            .foregroundColor(.accentColor)
                                    }
                                    Text(activityStatusText(for: item))
                                        .font(.caption)
                                        .foregroundColor(item.hasIssue ? .red : .secondary)
                                }
                            }
                            Spacer()
                            if item.sizeleft > 0 {
                                Text(item.progressLabel)
                                    .font(.caption)
                                    .bold()
                            }
                        }
                        .padding(12)
                        .background(Color(UIColor.tertiarySystemBackground))
                        .cornerRadius(12)
                    }
                    .buttonStyle(.plain)
                }

                if state.activityQueue.count > 5 {
                    HStack {
                        Spacer()
                        Text(MR.strings().additional_items_count.formatted(args: [Int32(state.activityQueue.count - 5)]))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }

    private func activityStatusText(for item: QueueItem) -> String {
        var text = item.statusLabel
        if item.trackedDownloadState == .downloading {
            text += " • \(item.progressLabel)"
            if let remainingTime = item.remainingTimeLabel {
                text += " • \(remainingTime) left"
            }
        }
        return text
    }
}

struct DashboardActiveStreamsSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onItemClick: ((TracearrStreamSession) -> Void)? = nil

    private var activeStreams: [TracearrStreamSession] {
        state.tracearrStats.flatMap { $0.activeStreams }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "play.tv")
                Text(MR.strings().active_streams.localized())
                    .font(.headline)
                    .bold()
            }

            if activeStreams.isEmpty {
                Text(MR.strings().no_active_streams.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ForEach(activeStreams.prefix(5), id: \.id) { session in
                    Button(action: {
                        if !isEditing {
                            onItemClick?(session)
                        }
                    }) {
                        HStack {
                            let dotColor: Color = {
                                Color.serverColor(type: session.server?.type ?? session.serverType, name: session.server?.name ?? session.serverName)
                            }()

                            Circle()
                                .fill(dotColor)
                                .frame(width: 4, height: 4)

                            VStack(alignment: .leading, spacing: 2) {
                                Text(displayTitle(for: session))
                                    .font(.subheadline)
                                    .bold()
                                    .lineLimit(1)

                                if let epInfo = episodeInfo(for: session) {
                                    Text(epInfo)
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                        .lineLimit(1)
                                }

                                HStack(spacing: 4) {
                                    if !session.effectiveServerName.isEmpty {
                                        Text("\(session.effectiveServerName) •")
                                            .font(.caption)
                                            .foregroundColor(.accentColor)
                                    }
                                    Text(streamStatusText(for: session))
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                            }
                            Spacer()

                            if let progressText = progressPercentage(for: session) {
                                Text(progressText)
                                    .font(.caption)
                                    .bold()
                            }
                        }
                        .padding(12)
                        .background(Color(UIColor.tertiarySystemBackground))
                        .cornerRadius(12)
                    }
                    .buttonStyle(.plain)
                }

                if activeStreams.count > 5 {
                    HStack {
                        Spacer()
                        Text(MR.strings().additional_items_count.formatted(args: [Int32(activeStreams.count - 5)]))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
        }
    }

    private func displayTitle(for session: TracearrStreamSession) -> String {
        session.grandparentTitle ?? session.showTitle ?? session.mediaTitle ?? session.channelTitle ?? MR.strings().unknown.localized()
    }

    private func episodeInfo(for session: TracearrStreamSession) -> String? {
        let isEpisode = session.mediaType == .episode || (session.seasonNumber != nil && session.episodeNumber != nil)
        guard isEpisode else { return nil }
        let s = session.seasonNumber?.intValue ?? 0
        let e = session.episodeNumber?.intValue ?? 0
        let sStr = String(format: "%02d", s)
        let eStr = String(format: "%02d", e)
        let mainTitle = displayTitle(for: session)
        if let epTitle = session.mediaTitle, !epTitle.isEmpty, epTitle != mainTitle {
            return "S\(sStr)E\(eStr) • \(epTitle)"
        } else {
            return "S\(sStr)E\(eStr)"
        }
    }

    private func streamStatusText(for session: TracearrStreamSession) -> String {
        var parts: [String] = []
        if !session.effectiveUsername.isEmpty {
            parts.append(session.effectiveUsername)
        }
        if let state = session.state {
            if state.lowercased() == "paused" {
                parts.append(MR.strings().paused.localized())
            } else if state.lowercased() == "playing" {
                parts.append(MR.strings().playing.localized())
            } else {
                parts.append(state.capitalized)
            }
        }
        if let quality = session.quality ?? session.resolution {
            parts.append(quality)
        }
        return parts.joined(separator: " • ")
    }

    private func progressPercentage(for session: TracearrStreamSession) -> String? {
        let total = session.totalDurationMs?.int64Value ?? session.durationMs?.int64Value ?? 0
        let progress = session.progressMs?.int64Value ?? 0
        guard total > 0, progress > 0 else { return nil }
        let percent = Int((Double(progress) / Double(total)) * 100)
        return "\(min(max(percent, 0), 100))%"
    }
}

struct DashboardTodaySection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "calendar")
                Text(MR.strings().today.localized())
                    .font(.headline)
                    .bold()
            }

            if state.calendarItems.isEmpty {
                Text(MR.strings().nothing_on_today.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ForEach(state.calendarItems, id: \.uniqueId) { item in
                    CalendarItemRow(dashboardItem: item)
                }
            }
        }
    }
}

struct DashboardUpcomingSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "calendar.badge.clock")
                Text(MR.strings().upcoming.localized())
                    .font(.headline)
                    .bold()
            }

            if state.upcomingCalendarItems.isEmpty {
                Text(MR.strings().nothing_upcoming.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ForEach(state.upcomingCalendarItems.prefix(5), id: \.uniqueId) { item in
                    CalendarItemRow(dashboardItem: item, showDate: true)
                }
            }
        }
    }
}

struct CalendarItemRow: View {
    let dashboardItem: DashboardCalendarItem
    var showDate: Bool = false
    @EnvironmentObject private var navigationManager: NavigationManager

    private var item: CalendarItem { dashboardItem.item }

    var body: some View {
        Button(action: {
            navigateCalendarItemOnDashboard(item: item, navigationManager: navigationManager)
        }) {
            HStack(spacing: 12) {
                let color: Color = {
                    if let type = item.associatedType {
                        // Extract color from Compose Color object
                        return Color(hex: type.associatedColor)
                    }
                    return .accentColor
                }()

                Circle()
                    .fill(color)
                    .frame(width: 4, height: 4)

                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.subheadline)
                        .bold()
                        .lineLimit(1)

                    if !subTitle.isEmpty {
                        Text(subTitle)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }

                    if showDate {
                        Text(formatDate(dashboardItem.date))
                            .font(.system(size: 10))
                            .foregroundColor(.accentColor)
                    }
                }
                Spacer()

                if let icon = statusIcon {
                    Image(systemName: icon)
                        .font(.system(size: 16))
                        .foregroundColor(.secondary)
                }
            }
            .padding(12)
            .background(Color(UIColor.tertiarySystemBackground))
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }

    private var statusIcon: String? {
        if let episode = item as? Episode {
            if episode.hasFile {
                return "checkmark.circle.fill"
            } else if !episode.monitored {
                return "bookmark"
            } else if !episode.hasAired {
                return "clock.fill"
            } else if episode.monitored {
                return "bookmark.fill"
            }
        } else if let group = item as? EpisodeGroup {
            let first = group.first
            if first.hasFile {
                return "checkmark.circle.fill"
            } else if !first.monitored {
                return "bookmark"
            } else if !first.hasAired {
                return "clock.fill"
            } else if first.monitored {
                return "bookmark.fill"
            }
        } else if let movie = item as? ArrMovie {
            if movie.isDownloaded {
                return "checkmark.circle.fill"
            } else if !movie.monitored {
                return "bookmark"
            } else if movie.isWaiting {
                return "clock.fill"
            } else if movie.monitored {
                return "bookmark.fill"
            }
        } else if let album = item as? ArrAlbum {
            if album.isDownloaded {
                return "square.and.arrow.down.fill"
            } else if album.isPartiallyDownloaded {
                return "arrow.down.circle.dotted"
            } else if album.monitored {
                return "bookmark.fill"
            } else if !album.monitored {
                return "bookmark"
            }
        } else if let book = item as? Book {
            if book.isDownloaded {
                return "square.and.arrow.down.fill"
            } else if book.isPartiallyDownloaded {
                return "arrow.down.circle.dotted"
            } else if book.monitored {
                return "bookmark.fill"
            } else if !book.monitored {
                return "bookmark"
            }
        } else if let audiobook = item as? Audiobook {
            if audiobook.isDownloaded {
                return "square.and.arrow.down.fill"
            } else if audiobook.monitored {
                return "bookmark.fill"
            } else if !audiobook.monitored {
                return "bookmark"
            }
        }
        return nil
    }

    private var title: String {
        if let episode = item as? Episode {
            return episode.series?.title ?? ""
        } else if let group = item as? EpisodeGroup {
            return group.first.series?.title ?? ""
        } else if let album = item as? ArrAlbum {
            return album.artist?.title ?? ""
        } else if let movie = item as? ArrMovie {
            return movie.title ?? ""
        } else if let audiobook = item as? Audiobook {
            return audiobook.title ?? ""
        } else if let book = item as? Book {
            return book.title
        }
        return ""
    }

    private var subTitle: String {
        if let episode = item as? Episode {
            return "\(episode.seasonEpLabel): \(episode.title ?? "")"
        } else if let group = item as? EpisodeGroup {
            let first = group.first
            let base = "\(first.seasonEpLabel): \(first.title ?? "")"
            if !group.additional.isEmpty {
                return "\(base) (\(MR.strings().additional_items_count.formatted(args: [group.additional.count])))"
            }
            return base
        } else if let album = item as? ArrAlbum {
            return album.title ?? ""
        } else if let movie = item as? ArrMovie {
            if let physical = movie.physicalRelease, physical.isEqual(date: dashboardItem.date) {
                return MR.strings().physical_release.localized()
            } else if let digital = movie.digitalRelease, digital.isEqual(date: dashboardItem.date) {
                return MR.strings().digital_release.localized()
            } else if let cinemas = movie.inCinemas, cinemas.isEqual(date: dashboardItem.date) {
                return MR.strings().in_cinemas.localized()
            } else {
                return MR.strings().release_date.localized()
            }
        }
        return ""
    }

    private func formatDate(_ date: Kotlinx_datetimeLocalDate) -> String {
        let components = date.toDateComponents()
        let date = Calendar.current.date(from: components)
        let formatter = DateFormatter()
        formatter.dateFormat = "EEE, MMM d"
        return formatter.string(from: date ?? Date())
    }
}

struct DashboardInstanceDashboardSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "server.rack")
                Text(MR.strings().instances.localized())
                    .font(.headline)
                    .bold()
            }

            ForEach(state.instances, id: \.instance.id) { instanceState in
                Button {
                    navigationManager.openArrDashboard(id: instanceState.instance.id)
                } label: {
                    VStack(alignment: .leading, spacing: 12) {
                        HStack(spacing: 12) {
                            Image(resource: instanceState.instance.type.icon)
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                                .frame(width: 32, height: 32)

                            VStack(alignment: .leading, spacing: 2) {
                                Text(instanceState.instance.label)
                                    .font(.subheadline)
                                    .bold()

                                let completion = instanceState.library.isEmpty ? 0 : instanceState.library.map { $0.statusProgress }.reduce(0, +) / Float(instanceState.library.count)

                                Text("\(instanceState.totalItems) Items • \(instanceState.sizeOnDisk.bytesAsFileSizeString()) • \(Int(completion * 100))% Downloaded")
                                    .font(.system(size: 10))
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            if instanceState.healthItems.contains(where: { $0.type == .error }) {
                                Image(systemName: "exclamationmark.octagon.fill")
                                    .foregroundColor(.red)
                                    .font(.system(size: 14))
                            } else if !instanceState.healthItems.isEmpty {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(.yellow)
                                    .font(.system(size: 14))
                            }
                        }

                        VStack(spacing: 4) {
                            ForEach(instanceState.disks, id: \.path) { disk in
                                HStack(spacing: 8) {
                                    Text(disk.path ?? MR.strings().unknown.localized())
                                        .font(.system(size: 10, weight: .medium))
                                        .foregroundColor(.secondary)
                                        .lineLimit(1)

                                    let usedSpace = disk.totalSpace - disk.freeSpace
                                    Text("\(usedSpace.bytesAsFileSizeString()) / \(disk.totalSpace.bytesAsFileSizeString())")
                                        .font(.system(size: 8))
                                        .foregroundColor(.secondary.opacity(0.7))

                                    Spacer()

                                    Text("\(Int(disk.usedPercentage * 100))% full")
                                        .font(.system(size: 8))
                                        .foregroundColor(disk.usedPercentage > 0.9 ? .red : .secondary)
                                }
                            }
                        }
                    }
                    .padding(12)
                    .background(Color(UIColor.tertiarySystemBackground))
                    .cornerRadius(12)
                }
                .buttonStyle(.plain)
            }
        }
    }
}

struct AddDashboardCardSheet: View {
    @ObservedObject var viewModel: DashboardViewModelS
    @Environment(\.dismiss) var dismiss

    private let columns = [
        GridItem(.adaptive(minimum: 300, maximum: .infinity), spacing: 16)
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                let available = DashboardCards.allCases.filter { card in
                    !viewModel.cards.contains(where: { $0.name == card.name })
                }

                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(available, id: \.self) { card in
                        VStack(alignment: .leading, spacing: 12) {
                            HStack {
                                Text(card.title.localized())
                                    .font(.headline)
                                    .bold()
                                Spacer()
                                Image(systemName: "plus.circle.fill")
                                    .font(.title2)
                                    .foregroundColor(.accentColor)
                            }

                            let mockSuccess = CombinedDashboardStateCompanion.shared.Mock
                            DashboardCardView(card: card, state: mockSuccess, isEditing: false)
                                .disabled(true)
                                .padding(12)
                                .background(Color(UIColor.systemBackground).opacity(0.5))
                                .cornerRadius(12)
                        }
                        .padding()
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(16)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            viewModel.addCard(card: card)
                        }
                    }
                }
                .padding()
            }
            .navigationTitle(MR.strings().add_dashboard_cards.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(MR.strings().close.localized()) {
                        dismiss()
                    }
                }
            }
        }
    }
}

struct DashboardPendingRequestsSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onRequestClick: ((MediaRequestPackage) -> Void)? = nil
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var selectedFilter: RequestState = .pending

    private var filteredRequests: [MediaRequestPackage] {
        if selectedFilter == .all {
            return state.allRequests
        } else {
            return state.allRequests.filter { $0.request.matchesFilter(state: selectedFilter) }
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "tray")
                Text(MR.strings().requests.localized())
                    .font(.headline)
                    .bold()
            }

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(RequestState.allCases, id: \.self) { filterState in
                        let isSelected = selectedFilter == filterState
                        Button(action: { selectedFilter = filterState }) {
                            Text(filterState.resource.localized())
                                .font(.subheadline)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(isSelected ? Color.accentColor : Color(.secondarySystemBackground))
                                .foregroundColor(isSelected ? .white : .primary)
                                .clipShape(Capsule())
                                .overlay(
                                    Capsule()
                                        .stroke(Color.primary.opacity(0.1), lineWidth: isSelected ? 0 : 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }

            if filteredRequests.isEmpty {
                Text(MR.strings().no_requests_found.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(filteredRequests, id: \.request.id) { mediaPackage in
                            CompactRequestCard(mediaPackage: mediaPackage) {
                                if let onRequestClick = onRequestClick {
                                    onRequestClick(mediaPackage)
                                } else {
                                    navigationManager.goToSeerrDetailsOnDashboard(
                                        tmdbId: mediaPackage.request.media.tmdbId,
                                        requestType: mediaPackage.request.type
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

struct CompactRequestCard: View {
    let mediaPackage: MediaRequestPackage
    let onClick: () -> Void

    private var request: MediaRequest { mediaPackage.request }
    private var details: RequestMediaDetails? { mediaPackage.details }

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 8) {
                HStack(alignment: .top, spacing: 8) {
                    if let posterUrl = details?.fullPosterPath, let url = URL(string: posterUrl) {
                        AsyncImage(url: url) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color(.systemGray4)
                        }
                        .frame(width: 48, height: 72)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    } else {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color(.systemGray4))
                            .frame(width: 48, height: 72)
                    }

                    VStack(alignment: .leading, spacing: 2) {
                        HStack(spacing: 4) {
                            if let year = details?.displayDate?.year {
                                Text(String(year))
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                            RequestTypeChip(type: request.type)
                        }

                        Text(details?.displayTitle ?? MR.strings().unknown.localized())
                            .font(.subheadline.bold())
                            .lineLimit(2)

                        SeerrStatusChip(request: request)
                    }
                }

                HStack(spacing: 6) {
                    if let avatarUrl = URL(string: request.requestedBy.avatar) {
                        AsyncImage(url: avatarUrl) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Image(systemName: "person.circle.fill")
                                .foregroundColor(.secondary)
                        }
                        .frame(width: 20, height: 20)
                        .clipShape(Circle())
                    }

                    Text(request.requestedBy.displayName)
                        .font(.caption)
                        .fontWeight(.medium)
                        .lineLimit(1)
                }
            }
            .padding(12)
            .frame(width: 260, alignment: .leading)
            .background(Color(UIColor.tertiarySystemBackground))
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }
}

struct DashboardPendingIssuesSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onIssueClick: ((MediaIssuePackage) -> Void)? = nil
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var selectedFilter: IssueState = .open

    private var filteredIssues: [MediaIssuePackage] {
        if selectedFilter == .all {
            return state.allIssues
        } else {
            return state.allIssues.filter { $0.issue.matchesFilter(state: selectedFilter) }
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "ladybug")
                Text(MR.strings().issues.localized())
                    .font(.headline)
                    .bold()
            }

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(IssueState.allCases, id: \.self) { filterState in
                        let isSelected = selectedFilter == filterState
                        Button(action: { selectedFilter = filterState }) {
                            Text(filterState.resource.localized())
                                .font(.subheadline)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(isSelected ? Color.accentColor : Color(.secondarySystemBackground))
                                .foregroundColor(isSelected ? .white : .primary)
                                .clipShape(Capsule())
                                .overlay(
                                    Capsule()
                                        .stroke(Color.primary.opacity(0.1), lineWidth: isSelected ? 0 : 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }

            if filteredIssues.isEmpty {
                Text(MR.strings().no_issues_found.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(filteredIssues, id: \.issue.id) { issuePackage in
                            CompactIssueCard(issuePackage: issuePackage) {
                                if let onIssueClick = onIssueClick {
                                    onIssueClick(issuePackage)
                                } else if let media = issuePackage.issue.media {
                                    navigationManager.goToSeerrDetailsOnDashboard(
                                        tmdbId: media.tmdbId,
                                        requestType: media.mediaType
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

struct CompactIssueCard: View {
    let issuePackage: MediaIssuePackage
    let onClick: () -> Void

    private var issue: Issue { issuePackage.issue }
    private var details: RequestMediaDetails? { issuePackage.details }

    var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 8) {
                HStack(alignment: .top, spacing: 8) {
                    if let posterUrl = details?.fullPosterPath, let url = URL(string: posterUrl) {
                        AsyncImage(url: url) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color(.systemGray4)
                        }
                        .frame(width: 48, height: 72)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    } else {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color(.systemGray4))
                            .frame(width: 48, height: 72)
                    }

                    VStack(alignment: .leading, spacing: 2) {
                        HStack(spacing: 4) {
                            if let mediaType = issue.media?.mediaType {
                                RequestTypeChip(type: mediaType)
                            }
                            if let year = details?.displayDate?.year {
                                Text(String(year))
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                            if issue.media?.mediaType == .tv {
                                let seasonLabel = issue.problemSeason == 0 ? nil : "S\(issue.problemSeason)"
                                let episodeLabel = issue.problemEpisode == 0 ? nil : "E\(issue.problemEpisode)"
                                if let season = seasonLabel, let episode = episodeLabel {
                                    Text("\(season) • \(episode)")
                                        .font(.caption2.bold())
                                        .foregroundColor(.accentColor)
                                } else if let season = seasonLabel {
                                    Text(season)
                                        .font(.caption2.bold())
                                        .foregroundColor(.accentColor)
                                }
                            }
                        }

                        Text(details?.displayTitle ?? MR.strings().unknown.localized())
                            .font(.subheadline.bold())
                            .lineLimit(2)

                        SeerrIssueStatusChip(issue: issue)
                    }
                }

                if let createdBy = issue.createdBy {
                    HStack(spacing: 6) {
                        if let avatarUrl = URL(string: createdBy.avatar) {
                            AsyncImage(url: avatarUrl) { image in
                                image.resizable().aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Image(systemName: "person.circle.fill")
                                    .foregroundColor(.secondary)
                            }
                            .frame(width: 20, height: 20)
                            .clipShape(Circle())
                        }

                        Text(createdBy.displayName)
                            .font(.caption)
                            .fontWeight(.medium)
                            .lineLimit(1)
                    }
                }
            }
            .padding(12)
            .frame(width: 260, alignment: .leading)
            .background(Color(UIColor.tertiarySystemBackground))
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }
}

private func navigateCalendarItemOnDashboard(item: CalendarItem, navigationManager: NavigationManager) {
    if let episode = item as? Episode {
        let seriesId = episode.series?.id?.int64Value ?? episode.seriesId
        navigationManager.goToDetailsOnDashboard(arrId: seriesId, instanceType: .sonarr)
    } else if let group = item as? EpisodeGroup {
        let seriesId = group.first.series?.id?.int64Value ?? group.first.seriesId
        navigationManager.goToDetailsOnDashboard(arrId: seriesId, instanceType: .sonarr)
    } else if let album = item as? ArrAlbum {
        navigationManager.goToDetailsOnDashboard(arrId: album.artistId, instanceType: .lidarr)
    } else if let movie = item as? ArrMovie {
        if let movieId = movie.id?.int64Value {
            navigationManager.goToDetailsOnDashboard(arrId: movieId, instanceType: .radarr)
        }
    } else if let audiobook = item as? Audiobook {
        if let audiobookId = audiobook.id?.int64Value {
            navigationManager.goToDetailsOnDashboard(arrId: audiobookId, instanceType: .listenarr)
        }
    } else if let book = item as? Book {
        if let authorId = book.authorId?.int64Value {
            navigationManager.goToDetailsOnDashboard(arrId: authorId, instanceType: .bookshelf)
        }
    }
}

struct HealthNoticesSheet: View {
    let instances: [ArrInstanceDashboardState]
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    let healthInstances = instances.filter { !$0.healthItems.isEmpty }
                    if healthInstances.isEmpty {
                        Text(MR.strings().no_issues.localized())
                            .foregroundColor(.secondary)
                            .frame(maxWidth: .infinity, alignment: .center)
                            .padding(.vertical)
                    } else {
                        ForEach(healthInstances, id: \.instance.id) { instanceState in
                            VStack(alignment: .leading, spacing: 8) {
                                HStack(spacing: 8) {
                                    Image(resource: instanceState.instance.type.icon)
                                        .resizable()
                                        .aspectRatio(contentMode: .fit)
                                        .frame(width: 20, height: 20)
                                    Text(instanceState.instance.label)
                                        .font(.headline)
                                        .bold()
                                }

                                ForEach(instanceState.healthItems.indices, id: \.self) { index in
                                    let health = instanceState.healthItems[index]
                                    VStack(alignment: .leading, spacing: 4) {
                                        if let message = health.message {
                                            Text(message)
                                                .font(.subheadline)
                                                .fontWeight(.medium)
                                        }
                                        if let source = health.source {
                                            Text(source)
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                        }
                                    }
                                    .padding(12)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(health.type == .error ? Color.red.opacity(0.15) : Color(UIColor.secondarySystemBackground))
                                    .cornerRadius(10)
                                }
                            }
                        }
                    }
                }
                .padding(16)
            }
            .navigationTitle(MR.strings().health.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(MR.strings().close.localized()) { dismiss() }
                }
            }
        }
    }
}

struct IdentifiableDiscoverResult: Identifiable {
    let result: DiscoverResult
    var id: String { "\(result.mediaType.name)_\(result.id)" }
}

struct DashboardDiscoverSpotlightSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onMediaClick: ((Int64, RequestType) -> Void)? = nil
    var onRequestClick: ((DiscoverResult) -> Void)? = nil

    @State private var selectedIndex: Int = 0
    private let timer = Timer.publish(every: 10, on: .main, in: .common).autoconnect()

    var body: some View {
        let spotlightItems = state.spotlightMedia

        VStack(alignment: .leading, spacing: 12) {
            if isEditing || state.seerrInstances.isEmpty || spotlightItems.isEmpty {
                HStack(spacing: 8) {
                    Image(resource: InstanceType.seerr.icon)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 20, height: 20)
                    Text(MR.strings().dashboard_discover_spotlight.localized())
                        .font(.headline)
                        .bold()
                }
            }

            if state.seerrInstances.isEmpty || spotlightItems.isEmpty {
                Text(MR.strings().no_type_instances_message.formatted(args: [InstanceType.seerr.name]))
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 16)
            } else {
                let currentIndex = min(max(0, selectedIndex), spotlightItems.count - 1)
                let currentItem = spotlightItems[currentIndex]

                VStack(spacing: 0) {
                    // Pager & Backdrop
                    ZStack(alignment: .bottomLeading) {
                        TabView(selection: $selectedIndex) {
                            ForEach(spotlightItems.indices, id: \.self) { index in
                                let item = spotlightItems[index]
                                ZStack {
                                    if let backdrop = item.fullBackdropPath ?? item.fullPosterPath, let url = URL(string: backdrop) {
                                        AsyncImage(url: url) { phase in
                                            switch phase {
                                            case .success(let img):
                                                img.resizable().aspectRatio(contentMode: .fill)
                                            case .empty:
                                                ProgressView()
                                            default:
                                                ZStack {
                                                    Color(.secondarySystemBackground)
                                                    Image(systemName: item.mediaType == .tv ? "tv" : "film")
                                                        .font(.system(size: 40))
                                                        .foregroundColor(.secondary.opacity(0.4))
                                                }
                                            }
                                        }
                                    } else {
                                        ZStack {
                                            Color(.secondarySystemBackground)
                                            Image(systemName: item.mediaType == .tv ? "tv" : "film")
                                                .font(.system(size: 40))
                                                .foregroundColor(.secondary.opacity(0.4))
                                        }
                                    }
                                }
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .clipped()
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    if !isEditing {
                                        onMediaClick?(item.id, item.mediaType)
                                    }
                                }
                                .tag(index)
                            }
                        }
                        .tabViewStyle(.page(indexDisplayMode: .never))
                        .aspectRatio(16.0 / 9.0, contentMode: .fit)
                        .frame(maxWidth: .infinity)
                        .cornerRadius(12)

                        // Gradient Scrim
                        LinearGradient(
                            colors: [.clear, .black.opacity(0.4), .black.opacity(0.85)],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                        .cornerRadius(12)
                        .allowsHitTesting(false)

                        // Top overlays
                        VStack {
                            HStack(spacing: 6) {
                                Text(MR.strings().dashboard_discover_spotlight.localized())
                                    .font(.system(size: 10, weight: .bold))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color.accentColor.opacity(0.9))
                                    .foregroundColor(.white)
                                    .clipShape(Capsule())

                                if let status = currentItem.mediaInfo?.status {
                                    StatusBadge(status: status)
                                }
                                Spacer()
                            }
                            .padding(10)
                            Spacer()
                        }
                        .allowsHitTesting(false)

                        // Bottom info overlay
                        VStack(alignment: .leading, spacing: 4) {
                            HStack(spacing: 8) {
                                RequestTypeChip(type: currentItem.mediaType, solid: true)

                                if let rating = currentItem.contentRating, !rating.isEmpty {
                                    Text(rating)
                                        .font(.system(size: 11, weight: .bold))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 2)
                                        .background(Color.black.opacity(0.6))
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 4)
                                                .stroke(Color.white.opacity(0.3), lineWidth: 1)
                                        )
                                        .foregroundColor(.white)
                                        .cornerRadius(4)
                                }

                                let dateStr = currentItem.releaseDate ?? currentItem.firstAirDate
                                if let d = dateStr, d.count >= 4 {
                                    Text(String(d.prefix(4)))
                                        .font(.system(size: 11, weight: .semibold))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 2)
                                        .background(Color.black.opacity(0.6))
                                        .foregroundColor(.white)
                                        .cornerRadius(4)
                                }

                                if currentItem.voteAverage > 0 {
                                    HStack(spacing: 2) {
                                        Image(systemName: "star.fill")
                                            .font(.system(size: 10))
                                            .foregroundColor(.yellow)
                                        Text(String(format: "%.1f", currentItem.voteAverage))
                                            .font(.system(size: 11, weight: .bold))
                                            .foregroundColor(.white)
                                    }
                                }
                            }

                            Text(currentItem.title ?? currentItem.name ?? MR.strings().unknown.localized())
                                .font(.title3.bold())
                                .foregroundColor(.white)
                                .lineLimit(1)

                            if let studio = currentItem.studioOrNetwork, !studio.isEmpty {
                                Text(studio)
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.85))
                                    .lineLimit(1)
                            }
                        }
                        .padding(12)
                        .allowsHitTesting(false)
                        .animation(.easeInOut(duration: 0.3), value: selectedIndex)
                    }

                    // Content & Actions
                    VStack(alignment: .leading, spacing: 12) {
                        if let overview = currentItem.overview, !overview.isEmpty {
                            Text(overview)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                                .lineLimit(3)
                        }

                        HStack(spacing: 8) {
                            Button(action: {
                                if !isEditing {
                                    onMediaClick?(currentItem.id, currentItem.mediaType)
                                }
                            }) {
                                HStack(spacing: 6) {
                                    Image(systemName: "info.circle")
                                    Text(MR.strings().details.localized())
                                }
                                .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.bordered)

                            let mediaStatus = state.resolveMediaStatus(item: currentItem)
                            let isAvailable = mediaStatus == .available
                            let isPending = mediaStatus == .pending
                            let isProcessing = mediaStatus == .processing
                            let isTv = currentItem.mediaType == .tv

                            let buttonTitle: String = {
                                if isAvailable {
                                    return isTv ? MR.strings().request_more.localized() : MR.strings().available.localized()
                                } else if isPending {
                                    return MR.strings().pending.localized()
                                } else if isProcessing {
                                    return MR.strings().processing.localized()
                                } else if mediaStatus == .partiallyAvailable {
                                    return MR.strings().request_more.localized()
                                } else {
                                    return MR.strings().request.localized()
                                }
                            }()

                            let buttonIcon: String = {
                                if isAvailable && !isTv { return "checkmark" }
                                if isPending || isProcessing { return "clock" }
                                return "plus"
                            }()

                            let isTonal = (isAvailable && !isTv) || isPending || isProcessing

                            if isTonal {
                                Button(action: {
                                    if !isEditing {
                                        if let onRequest = onRequestClick {
                                            onRequest(currentItem)
                                        } else {
                                            onMediaClick?(currentItem.id, currentItem.mediaType)
                                        }
                                    }
                                }) {
                                    HStack(spacing: 6) {
                                        Image(systemName: buttonIcon)
                                        Text(buttonTitle)
                                    }
                                    .frame(maxWidth: .infinity)
                                }
                                .buttonStyle(.bordered)
                            } else {
                                Button(action: {
                                    if !isEditing {
                                        if let onRequest = onRequestClick {
                                            onRequest(currentItem)
                                        } else {
                                            onMediaClick?(currentItem.id, currentItem.mediaType)
                                        }
                                    }
                                }) {
                                    HStack(spacing: 6) {
                                        Image(systemName: buttonIcon)
                                        Text(buttonTitle)
                                    }
                                    .frame(maxWidth: .infinity)
                                }
                                .buttonStyle(.borderedProminent)
                            }
                        }
                    }
                    .padding(.top, 12)
                }
                .onReceive(timer) { _ in
                    if !isEditing && spotlightItems.count > 1 {
                        withAnimation {
                            selectedIndex = (selectedIndex + 1) % spotlightItems.count
                        }
                    }
                }
            }
        }
    }
}

struct DashboardDiscoverQuickPickSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onShuffleClick: (() -> Void)? = nil
    var onMediaClick: ((Int64, RequestType) -> Void)? = nil
    var onRequestClick: ((DiscoverResult) -> Void)? = nil

    @State private var rotationDegrees: Double = 0

    var body: some View {
        let currentItem = state.quickPickItem ?? state.quickPickMedia.first

        VStack(alignment: .leading, spacing: 12) {
            if isEditing || state.seerrInstances.isEmpty {
                HStack(spacing: 8) {
                    Image(resource: InstanceType.seerr.icon)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 20, height: 20)
                    Text(MR.strings().dashboard_discover_quick_pick.localized())
                        .font(.headline)
                        .bold()
                }
            }

            if state.seerrInstances.isEmpty {
                Text(MR.strings().no_type_instances_message.formatted(args: [InstanceType.seerr.name]))
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 16)
            } else if let item = currentItem {
                VStack(spacing: 12) {
                    HStack(alignment: .top, spacing: 12) {
                        GenericPosterItem(posterUrl: item.fullPosterPath)
                            .frame(width: 90, height: 135)

                        VStack(alignment: .leading, spacing: 6) {
                            Text(item.title ?? item.name ?? MR.strings().unknown.localized())
                                .font(.headline.bold())
                                .lineLimit(2)

                            HStack(spacing: 6) {
                                RequestTypeChip(type: item.mediaType, solid: true)

                                let dateStr = item.releaseDate ?? item.firstAirDate
                                if let d = dateStr, d.count >= 4 {
                                    Text(String(d.prefix(4)))
                                        .font(.system(size: 11, weight: .semibold))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 2)
                                        .background(Color(UIColor.tertiarySystemBackground))
                                        .cornerRadius(4)
                                }

                                if item.voteAverage > 0 {
                                    HStack(spacing: 2) {
                                        Image(systemName: "star.fill")
                                            .font(.system(size: 10))
                                            .foregroundColor(.yellow)
                                        Text(String(format: "%.1f", item.voteAverage))
                                            .font(.system(size: 11, weight: .bold))
                                    }
                                }
                            }

                            if let overview = item.overview, !overview.isEmpty {
                                Text(overview)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                    .lineLimit(3)
                            }
                        }
                    }

                    // Actions
                    HStack(spacing: 8) {
                        Button(action: {
                            if !isEditing {
                                withAnimation(.easeInOut(duration: 0.4)) {
                                    rotationDegrees += 360
                                }
                                onShuffleClick?()
                            }
                        }) {
                            Image(systemName: "die.face.5.fill")
                                .font(.system(size: 16))
                                .rotationEffect(.degrees(rotationDegrees))
                                .frame(width: 36, height: 36)
                        }
                        .buttonStyle(.bordered)
                        .disabled(state.quickPickMedia.isEmpty)

                        Button(action: {
                            if !isEditing {
                                onMediaClick?(item.id, item.mediaType)
                            }
                        }) {
                            Text(MR.strings().details.localized())
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)

                        let mediaStatus = state.resolveMediaStatus(item: item)
                        let isAvailable = mediaStatus == .available
                        let isPending = mediaStatus == .pending
                        let isProcessing = mediaStatus == .processing
                        let isTv = item.mediaType == .tv

                        let buttonTitle: String = {
                            if isAvailable {
                                return isTv ? MR.strings().request_more.localized() : MR.strings().available.localized()
                            } else if isPending {
                                return MR.strings().pending.localized()
                            } else if isProcessing {
                                return MR.strings().processing.localized()
                            } else if mediaStatus == .partiallyAvailable {
                                return MR.strings().request_more.localized()
                            } else {
                                return MR.strings().request.localized()
                            }
                        }()

                        let buttonIcon: String = {
                            if isAvailable && !isTv { return "checkmark" }
                            if isPending || isProcessing { return "clock" }
                            return "plus"
                        }()

                        let isTonal = (isAvailable && !isTv) || isPending || isProcessing

                        if isTonal {
                            Button(action: {
                                if !isEditing {
                                    if let onRequest = onRequestClick {
                                        onRequest(item)
                                    } else {
                                        onMediaClick?(item.id, item.mediaType)
                                    }
                                }
                            }) {
                                HStack(spacing: 4) {
                                    Image(systemName: buttonIcon)
                                    Text(buttonTitle)
                                }
                                .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.bordered)
                        } else {
                            Button(action: {
                                if !isEditing {
                                    if let onRequest = onRequestClick {
                                        onRequest(item)
                                    } else {
                                        onMediaClick?(item.id, item.mediaType)
                                    }
                                }
                            }) {
                                HStack(spacing: 4) {
                                    Image(systemName: buttonIcon)
                                    Text(buttonTitle)
                                }
                                .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.borderedProminent)
                        }
                    }
                }
            } else {
                Text(MR.strings().no_media_found.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 16)
            }
        }
    }
}

struct DashboardDiscoverFeedSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var visibleCategories: [DiscoverCategory] = []
    var onMediaClick: ((Int64, RequestType) -> Void)? = nil

    @State private var selectedCategory: DiscoverCategory? = nil

    private var activeCategories: [DiscoverCategory] {
        visibleCategories.isEmpty ? DiscoverCategory.allCases : visibleCategories
    }

    private var currentSelectedCategory: DiscoverCategory {
        if let selected = selectedCategory, activeCategories.contains(selected) {
            return selected
        }
        return activeCategories.first ?? .trending
    }

    private var categories: [(DiscoverCategory, String, String)] {
        return activeCategories.map { category in
            let title = category.title.localized()
            let icon: String
            switch category {
            case .trending: icon = "chart.line.uptrend.xyaxis"
            case .popularMovies: icon = "film"
            case .popularSeries: icon = "tv"
            case .upcomingMovies, .upcomingSeries: icon = "calendar"
            default: icon = "calendar"
            }
            return (category, title, icon)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if isEditing || state.seerrInstances.isEmpty {
                HStack(spacing: 8) {
                    Image(resource: InstanceType.seerr.icon)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 20, height: 20)
                    Text(MR.strings().dashboard_discover_feed.localized())
                        .font(.headline)
                        .bold()
                }
            }

            if state.seerrInstances.isEmpty {
                Text(MR.strings().no_type_instances_message.formatted(args: [InstanceType.seerr.name]))
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 16)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(categories, id: \.0) { category, title, icon in
                            let isSelected = currentSelectedCategory == category
                            Button(action: {
                                withAnimation { selectedCategory = category }
                            }) {
                                HStack(spacing: 6) {
                                    Image(systemName: icon)
                                    Text(title)
                                }
                                .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(isSelected ? Color.accentColor : Color(UIColor.tertiarySystemBackground))
                                .foregroundColor(isSelected ? .white : .primary)
                                .clipShape(Capsule())
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }

                let items = state.getDiscoverFeedItems(category: currentSelectedCategory)

                if items.isEmpty {
                    Text(MR.strings().no_media_found.localized())
                        .foregroundColor(.secondary)
                        .frame(maxWidth: .infinity, alignment: .center)
                        .padding(.vertical, 24)
                } else {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(items, id: \.id) { item in
                                DiscoverPosterItem(item: item, onItemClick: { it in
                                    if !isEditing {
                                        onMediaClick?(it.id, it.mediaType)
                                    }
                                })
                                .frame(width: 110)
                            }
                        }
                    }
                    .animation(.easeInOut(duration: 0.25), value: selectedCategory)
                }
            }
        }
    }
}

