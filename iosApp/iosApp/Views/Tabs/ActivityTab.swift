//
//  ActivityTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-21.
//

import SwiftUI
import Shared

struct ActivityTab: View {
    var body: some View {
        ActivityTabContent()
    }
}

struct ActivityTabContent: View {

    @StateObject private var viewModel = ActivityQueueViewModelS()
    @StateObject private var moreViewModel = MoreScreenViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager

    @State private var selectedItem: IdentifiableQueueItem? = nil
    @State private var showActivityCustomizationSheet = false
    @State private var showManualImportSheet = false

    private var titleText: String {
        switch viewModel.uiState.selectedTab {
        case .activity:
            guard !viewModel.queueItems.isEmpty else { return MR.strings().activity.localized() }
            return "\(MR.strings().activity.localized()) (\(viewModel.queueItems.count))"
        case .history:
            guard !viewModel.historyItems.isEmpty else { return MR.strings().history.localized() }
            return "\(MR.strings().history.localized()) (\(viewModel.historyItems.count))"
        case .downloaded:
            guard !viewModel.downloadedItems.isEmpty else { return MR.strings().recently_downloaded.localized() }
            return "\(MR.strings().recently_downloaded.localized()) (\(viewModel.downloadedItems.count))"
        case .blocklist:
            guard !viewModel.blocklistItems.isEmpty else { return MR.strings().blocklist.localized() }
            return "\(MR.strings().blocklist.localized()) (\(viewModel.blocklistItems.count))"
        case .missing:
            guard !viewModel.missingItems.isEmpty else { return MR.strings().missing.localized() }
            return "\(MR.strings().missing.localized()) (\(viewModel.missingItems.count))"
        case .cutoffUnmet:
            guard !viewModel.cutoffUnmetItems.isEmpty else { return MR.strings().cutoff_unmet.localized() }
            return "\(MR.strings().cutoff_unmet.localized()) (\(viewModel.cutoffUnmetItems.count))"
        }
    }

    private let segments: [ActivityTabSegment] = [
        .activity,
        .history,
        .downloaded,
        .blocklist,
        .missing,
        .cutoffUnmet
    ]

    private var topTabsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(segments, id: \.self) { segment in
                    let isSelected = viewModel.uiState.selectedTab == segment
                    tabPill(segment: segment, isSelected: isSelected)
                        .onTapGesture {
                            viewModel.setSelectedTab(segment)
                        }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 6)
        }
        .frame(height: 48)
        .background(Color(UIColor.systemBackground))
    }

    @ViewBuilder
    private func tabPill(segment: ActivityTabSegment, isSelected: Bool) -> some View {
        HStack(spacing: 6) {
            Text(segmentLabel(segment))
                .font(.subheadline.weight(isSelected ? .semibold : .regular))
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(isSelected ? Color.themePrimary : Color(UIColor.secondarySystemFill))
        .foregroundColor(isSelected ? Color.white : Color.primary)
        .clipShape(Capsule())
        .contentShape(Capsule())
    }

    private func segmentLabel(_ segment: ActivityTabSegment) -> String {
        switch segment {
        case .activity:
            return MR.strings().activity.localized()
        case .history:
            return MR.strings().history.localized()
        case .downloaded:
            return MR.strings().recently_downloaded.localized()
        case .blocklist:
            return MR.strings().blocklist.localized()
        case .missing:
            return MR.strings().missing.localized()
        case .cutoffUnmet:
            return MR.strings().cutoff_unmet.localized()
        default:
            return segment.resource.localized()
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            topTabsRow

            tabContent
        }
        .navigationTitle(titleText)
        .navigationBarTitleDisplayMode(.inline)
        .searchable(
            text: Binding(
                get: { viewModel.uiState.searchQuery },
                set: { viewModel.setSearchQuery($0) }
            ),
            prompt: MR.strings().search.localized()
        )
        .toolbar {
            if navigationManager.shouldShowDrawerButton(for: TabItemStandard.activity.key) {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        navigationManager.showLauncher = true
                    } label: {
                        Image(systemName: "line.3.horizontal")
                    }
                }
            }

            ToolbarItem(placement: .primaryAction) {
                ActivityFilterMenu(
                    selectedTab: viewModel.uiState.selectedTab,
                    sortBy: Binding(
                        get: { viewModel.uiState.sortBy },
                        set: { viewModel.setSortBy($0) }
                    ),
                    sortOrder: Binding(
                        get: { viewModel.uiState.sortOrder },
                        set: { viewModel.setSortOrder($0) }
                    ),
                    instanceId: Binding(
                        get: { viewModel.uiState.instanceId?.int64Value },
                        set: { viewModel.setInstanceId($0) }
                    ),
                    historyInstanceId: Binding(
                        get: { viewModel.uiState.historyInstanceId?.int64Value },
                        set: { viewModel.setHistoryInstanceId($0) }
                    ),
                    historyStateFilter: Binding(
                        get: { viewModel.uiState.historyStateFilter },
                        set: { viewModel.setHistoryStateFilter($0) }
                    ),
                    downloadedInstanceId: Binding(
                        get: { viewModel.uiState.downloadedInstanceId?.int64Value },
                        set: { viewModel.setDownloadedInstanceId($0) }
                    ),
                    blocklistInstanceId: Binding(
                        get: { viewModel.uiState.blocklistInstanceId?.int64Value },
                        set: { viewModel.setBlocklistInstanceId($0) }
                    ),
                    missingInstanceId: Binding(
                        get: { viewModel.uiState.missingInstanceId?.int64Value },
                        set: { viewModel.setMissingInstanceId($0) }
                    ),
                    cutoffInstanceId: Binding(
                        get: { viewModel.uiState.cutoffInstanceId?.int64Value },
                        set: { viewModel.setCutoffInstanceId($0) }
                    ),
                    instances: viewModel.instances,
                    onOpenViewCustomization: {
                        showActivityCustomizationSheet = true
                    }
                )
            }
        }
        .sheet(isPresented: $showActivityCustomizationSheet) {
            ActivityCardCustomizationSheet(viewModel: moreViewModel)
        }
        .sheet(isPresented: $showManualImportSheet) {
            let supportedInstances = viewModel.instances.filter {
                $0.supportsManualImport
            }
            let initialId = (viewModel.uiState.missingInstanceId?.int64Value
                ?? viewModel.uiState.cutoffInstanceId?.int64Value
                ?? viewModel.uiState.instanceId?.int64Value)
                .flatMap { id in supportedInstances.contains(where: { $0.id == id }) ? id : nil }

            DirectoryManualImportSheet(
                viewModel: viewModel,
                instances: supportedInstances,
                initialInstanceId: initialId,
                onDismiss: { showManualImportSheet = false },
                onSuccess: { viewModel.refresh() }
            )
        }
        .sheet(item: $selectedItem) { wrapper in
            QueueItemInfoSheet(item: wrapper.item, deleteInProgress: viewModel.removeInProgress, onDelete: { remove, block, skip in
                viewModel.removeQueueItem(wrapper.item, remove, block, skip)
            })
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
        }
        .refreshable {
            viewModel.refresh()
        }
        .onChange(of: viewModel.removeSuccesss) { _, newValue in
            if newValue {
                selectedItem = nil
                viewModel.refresh()
            }
        }
    }

    @ViewBuilder
    private var tabContent: some View {
        switch viewModel.uiState.selectedTab {
        case .activity:
            queueItemContent
        case .history:
            historyContent
        case .downloaded:
            downloadedContent
        case .blocklist:
            blocklistContent
        case .missing:
            wantedContent(
                items: viewModel.missingItems,
                isLoading: viewModel.isMissingLoading,
                hasLoaded: viewModel.hasMissingLoaded,
                selectedInstanceId: viewModel.uiState.missingInstanceId?.int64Value,
                onRefresh: { viewModel.refreshMissing() }
            )
        case .cutoffUnmet:
            wantedContent(
                items: viewModel.cutoffUnmetItems,
                isLoading: viewModel.isCutoffLoading,
                hasLoaded: viewModel.hasCutoffLoaded,
                selectedInstanceId: viewModel.uiState.cutoffInstanceId?.int64Value,
                onRefresh: { viewModel.refreshCutoff() }
            )
        default:
            queueItemContent
        }
    }

    @ViewBuilder
    private var queueItemContent: some View {
        if viewModel.queueItems.isEmpty {
            ContentUnavailableView(
                MR.strings().no_activity.localized(),
                systemImage: "square.and.arrow.down.fill"
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            List {
                ForEach(viewModel.queueItems, id: \.id) { item in
                    ActivityQueueItem(
                        item: item,
                        useFullColorCards: viewModel.useColoredCards,
                        onClick: {
                            selectedItem = IdentifiableQueueItem(item: item)
                        }
                    )
                    .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button(role: .destructive) {
                            selectedItem = IdentifiableQueueItem(item: item)
                        } label: {
                            Label(MR.strings().delete.localized(), systemImage: "trash.fill")
                        }
                    }
                }
            }
            .listStyle(.plain)
        }
    }

    @ViewBuilder
    private var historyContent: some View {
        if viewModel.historyItems.isEmpty {
            ContentUnavailableView(
                MR.strings().no_history.localized(),
                systemImage: "clock.arrow.circlepath"
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ScrollViewReader { proxy in
                List {
                    ForEach(Array(viewModel.historyItems.enumerated()), id: \.offset) { index, item in
                        HistoryItemView(item: item)
                            .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                            .listRowSeparator(.hidden)
                            .listRowBackground(Color.clear)
                            .id(index)
                    }
                }
                .listStyle(.plain)
                .onChange(of: viewModel.uiState.historyStateFilter) { _, _ in
                    var transaction = Transaction()
                    transaction.disablesAnimations = true
                    withTransaction(transaction) {
                        proxy.scrollTo(0, anchor: .top)
                    }
                }
                .onChange(of: viewModel.uiState.historyInstanceId) { _, _ in
                    var transaction = Transaction()
                    transaction.disablesAnimations = true
                    withTransaction(transaction) {
                        proxy.scrollTo(0, anchor: .top)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var downloadedContent: some View {
        if viewModel.downloadedItems.isEmpty {
            ContentUnavailableView(
                MR.strings().no_downloaded_media.localized(),
                systemImage: "arrow.down.circle"
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ScrollViewReader { proxy in
                List {
                    ForEach(Array(viewModel.downloadedItems.enumerated()), id: \.offset) { index, item in
                        DownloadedMediaItemView(item: item)
                            .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                            .listRowSeparator(.hidden)
                            .listRowBackground(Color.clear)
                            .id(index)
                    }
                }
                .listStyle(.plain)
                .onChange(of: viewModel.uiState.downloadedInstanceId) { _, _ in
                    var transaction = Transaction()
                    transaction.disablesAnimations = true
                    withTransaction(transaction) {
                        proxy.scrollTo(0, anchor: .top)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var blocklistContent: some View {
        if viewModel.blocklistItems.isEmpty {
            ContentUnavailableView(
                MR.strings().no_blocklist.localized(),
                systemImage: "hand.raised.fill"
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ScrollViewReader { proxy in
                List {
                    ForEach(Array(viewModel.blocklistItems.enumerated()), id: \.offset) { index, item in
                        BlocklistItemView(item: item, onDelete: {
                            viewModel.deleteBlocklistItem(item)
                        })
                        .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                        .listRowSeparator(.hidden)
                        .listRowBackground(Color.clear)
                        .id(index)
                    }
                }
                .listStyle(.plain)
                .onChange(of: viewModel.uiState.blocklistInstanceId) { _, _ in
                    var transaction = Transaction()
                    transaction.disablesAnimations = true
                    withTransaction(transaction) {
                        proxy.scrollTo(0, anchor: .top)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func wantedContent(
        items: [WantedMediaItem],
        isLoading: Bool,
        hasLoaded: Bool,
        selectedInstanceId: Int64?,
        onRefresh: @escaping () -> Void
    ) -> some View {
        let selectedInstance = viewModel.instances.first(where: { $0.id == selectedInstanceId })
        let isManualImportEnabled = selectedInstance?.type != .kapowarr && selectedInstance?.type != .listenarr

        if !hasLoaded && isLoading && !viewModel.instances.isEmpty {
            ProgressView()
                .progressViewStyle(.circular)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if items.isEmpty {
            ContentUnavailableView(
                MR.strings().no_wanted_items.localized(),
                systemImage: "magnifyingglass"
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            List {
                Section {
                    HStack(spacing: 12) {
                        Button {
                            viewModel.searchAllMissing()
                        } label: {
                            Label(MR.strings().search_all.localized(), systemImage: "magnifyingglass")
                                .font(.system(size: 14, weight: .semibold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 8)
                        }
                        .buttonStyle(.borderedProminent)

                        Button {
                            showManualImportSheet = true
                        } label: {
                            Label(MR.strings().manual_import.localized(), systemImage: "folder.badge.plus")
                                .font(.system(size: 14, weight: .semibold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 8)
                        }
                        .buttonStyle(.bordered)
                        .disabled(!isManualImportEnabled)
                    }
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                }

                Section {
                    ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                        WantedMediaItemView(item: item)
                            .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                            .listRowSeparator(.hidden)
                            .listRowBackground(Color.clear)
                            .id(index)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                onNavigateToInteractiveSearch(item: item)
                            }
                            .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                Button {
                                    viewModel.executeAutomaticSearch(item: item)
                                } label: {
                                    Label(MR.strings().search.localized(), systemImage: "magnifyingglass")
                                }
                                .tint(.blue)

                                Button {
                                    onNavigateToInteractiveSearch(item: item)
                                } label: {
                                    Label(MR.strings().interactive.localized(), systemImage: "person.fill")
                                }
                                .tint(.indigo)
                            }
                            .contextMenu {
                                Button {
                                    viewModel.executeAutomaticSearch(item: item)
                                } label: {
                                    Label(MR.strings().search.localized(), systemImage: "magnifyingglass")
                                }

                                Button {
                                    onNavigateToInteractiveSearch(item: item)
                                } label: {
                                    Label(MR.strings().interactive.localized(), systemImage: "person.fill")
                                }
                            }
                    }
                }
            }
            .listStyle(.plain)
        }
    }

    private func onNavigateToInteractiveSearch(item: WantedMediaItem) {
        guard let id = item.id?.int64Value, let instanceType = item.instanceType else { return }
        let instanceId = item.instanceId?.int64Value
        switch instanceType {
        case .radarr:
            navigationManager.push(MediaRoute.movieRelease(movieId: id, instanceId: instanceId))
        case .sonarr:
            navigationManager.push(MediaRoute.seriesReleases(seriesId: nil, seasonNumber: nil, episodeId: id, instanceId: instanceId))
        case .lidarr:
            navigationManager.push(MediaRoute.albumReleases(albumId: id, artistId: nil, instanceId: instanceId))
        case .bookshelf, .chaptarr:
            navigationManager.push(MediaRoute.bookReleases(bookId: id, instanceId: instanceId))
        case .listenarr:
            navigationManager.push(MediaRoute.audiobookReleases(id: id, query: item.title ?? "", instanceId: instanceId))
        case .kapowarr:
            navigationManager.push(MediaRoute.comicReleases(mediaId: id, isVolume: true, instanceId: instanceId))
        default:
            break
        }
    }
}

