//
//  DashboardTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-06-11.
//

import Shared
import SwiftUI

struct DashboardTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.dashboardPath) {
                DashboardTabContent()
            }
        case .launcher:
            DashboardTabContent()
        }
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
                    showInstanceIndicatorShadow: discoverViewModel.searchShowInstanceIndicatorShadow,
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
        .toolbar {
            if !viewModel.isEditing {
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
                        onHealthClick: { showHealthSheet = true },
                        onSeerrRequestsStatClick: {
                            requestsViewModel.setSelectedTab(.requests)
                            showSeerrSheet = true
                        },
                        onSeerrIssuesStatClick: {
                            requestsViewModel.setSelectedTab(.issues)
                            showSeerrSheet = true
                        }
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
        .navigationDestination(for: SettingsRoute.self) { route in
            if case .arrDashboard(let id) = route {
                ArrInstanceDashboard(id: id)
            }
        }
        .navigationDestination(for: MediaRoute.self) { route in
            MediaRouteDestination(route: route)
        }
        .navigationDestination(for: SeerrRoute.self) { route in
            SeerrRouteDestination(route: route)
        }
    }

    private func handleCardClick(_ card: DashboardCards) {
        switch card {
        case .arrOverview: navigationManager.openSettings()
        case .seerrOverview, .pendingRequests, .pendingIssues: navigationManager.openRequestsTab()
        case .prowlarrOverview: navigationManager.openProwlarrTab()
        case .bazarrOverview: navigationManager.openBazarrTab()
        case .downloadClients: navigationManager.openDownloadsTab()
        case .activityQueue: navigationManager.openActivityTab()
        case .onToday, .upcomingReleases: navigationManager.openScheduleTab()
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
    var onHealthClick: (() -> Void)? = nil
    var onSeerrRequestsStatClick: (() -> Void)? = nil
    var onSeerrIssuesStatClick: (() -> Void)? = nil
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
                onHealthClick: onHealthClick,
                onSeerrRequestsStatClick: onSeerrRequestsStatClick,
                onSeerrIssuesStatClick: onSeerrIssuesStatClick
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
    var onHealthClick: (() -> Void)? = nil
    var onSeerrRequestsStatClick: (() -> Void)? = nil
    var onSeerrIssuesStatClick: (() -> Void)? = nil

    var body: some View {
        Group {
            switch card {
            case .arrOverview: DashboardOverviewSection(state: state, isEditing: isEditing, onHealthClick: onHealthClick)
            case .seerrOverview: DashboardSeerrSection(state: state, isEditing: isEditing, onRequestClick: onSeerrRequestsStatClick, onIssueClick: onSeerrIssuesStatClick)
            case .pendingRequests: DashboardPendingRequestsSection(state: state, isEditing: isEditing, onRequestClick: onRequestClick)
            case .pendingIssues: DashboardPendingIssuesSection(state: state, isEditing: isEditing, onIssueClick: onIssueClick)
            case .prowlarrOverview: DashboardProwlarrSection(state: state, isEditing: isEditing)
            case .network: DashboardNetworkSection(state: state)
            case .recentlyAdded: DashboardRecentlyAddedSection(state: state)
            case .downloadClients: DashboardDownloadClientsSection(state: state, isEditing: isEditing)
            case .activityQueue: DashboardActivityQueueSection(state: state, isEditing: isEditing, onItemClick: onActivityClick)
            case .onToday: DashboardTodaySection(state: state, isEditing: isEditing)
            case .upcomingReleases: DashboardUpcomingSection(state: state, isEditing: isEditing)
            case .bazarrOverview: DashboardBazarrSection(state: state, isEditing: isEditing)
            case .instanceDashboard: DashboardInstanceDashboardSection(state: state, isEditing: isEditing)
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
                StatCard(icon: "tray", label: MR.strings().requests.localized(), value: "\(totalRequests)", color: .purple, onClick: isEditing ? nil : onRequestClick)
                StatCard(icon: "ladybug", label: MR.strings().issues.localized(), value: "\(totalIssues)", color: totalIssues > 0 ? .red : .secondary, onClick: isEditing ? nil : onIssueClick)
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

struct DashboardNetworkSection: View {
    let state: CombinedDashboardStateSuccess

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: "wifi")
                    .font(.system(size: 20))
                Text(MR.strings().network_status.localized())
                    .font(.headline)
                    .bold()

                Spacer()

                if let ssid = state.networkStatus?.ssid {
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
            }
            .padding(12)
            .background(Color(UIColor.tertiarySystemBackground))
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
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
            return MR.strings().release_date.localized()
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
        NavigationView {
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
                        .onTapGesture {
                            viewModel.addCard(card: card)
                        }
                    }
                }
                .padding()
            }
            .navigationTitle(MR.strings().add_dashboard_cards.localized())
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

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "tray")
                Text(MR.strings().requests.localized())
                    .font(.headline)
                    .bold()
            }

            let pendingRequests = state.pendingRequests
            if pendingRequests.isEmpty {
                Text(MR.strings().no_requests_found.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(pendingRequests, id: \.request.id) { mediaPackage in
                            CompactRequestCard(mediaPackage: mediaPackage) {
                                if let onRequestClick = onRequestClick {
                                    onRequestClick(mediaPackage)
                                } else {
                                    navigationManager.goToSeerrDetails(
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

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "ladybug")
                Text(MR.strings().issues.localized())
                    .font(.headline)
                    .bold()
            }

            let openIssues = state.openIssues
            if openIssues.isEmpty {
                Text(MR.strings().no_issues_found.localized())
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(openIssues, id: \.issue.id) { issuePackage in
                            CompactIssueCard(issuePackage: issuePackage) {
                                if let onIssueClick = onIssueClick {
                                    onIssueClick(issuePackage)
                                } else if let media = issuePackage.issue.media {
                                    navigationManager.goToSeerrDetails(
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
                            if let year = details?.displayDate?.year {
                                Text(String(year))
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                            if let mediaType = issue.media?.mediaType {
                                RequestTypeChip(type: mediaType)
                            }
                        }

                        Text(details?.displayTitle ?? MR.strings().unknown.localized())
                            .font(.subheadline.bold())
                            .lineLimit(2)

                        SeerrIssueStatusChip(issue: issue)
                    }
                }

                if issue.media?.mediaType == .tv {
                    let seasonLabel = issue.problemSeason == 0 ? MR.strings().all.localized() : "\(issue.problemSeason)"
                    let episodeLabel = issue.problemEpisode == 0 ? MR.strings().all.localized() : "\(issue.problemEpisode)"
                    Text("S\(seasonLabel) E\(episodeLabel)")
                        .font(.caption2.bold())
                        .foregroundColor(.accentColor)
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
        if let seriesId = episode.series?.id {
            navigationManager.goToDetailsOnDashboard(arrId: seriesId.int64Value, instanceType: .sonarr)
        }
    } else if let group = item as? EpisodeGroup {
        if let seriesId = group.first.series?.id {
            navigationManager.goToDetailsOnDashboard(arrId: seriesId.int64Value, instanceType: .sonarr)
        }
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
