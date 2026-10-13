import Shared
import SwiftUI

struct TautulliHomeScreen: View {
    @StateObject private var viewModel = MonitoringViewModelS()
    @StateObject private var instancesViewModel = InstancesViewModelS(types: [.tracearr, .tautulli])
    @StateObject private var globalPreferences = PreferencesViewModel()
    @EnvironmentObject private var navigationManager: NavigationManager
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    private var isLargeScreen: Bool { horizontalSizeClass == .regular }

    var body: some View {
        Group {
            if viewModel.state is MonitoringStateNoInstance {
                NoInstanceView(type: .tautulli)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? MonitoringStateSuccess {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        if let stats = success.stats {
                            TautulliDashboardStatsView(
                                stats: stats,
                                isExpanded: isLargeScreen,
                                onNavigateToHistory: { navigationManager.go(to: TracearrRoute.history) },
                                onNavigateToUsers: { navigationManager.go(to: TracearrRoute.users) },
                                onNavigateToStats: { navigationManager.go(to: TracearrRoute.stats) }
                            )
                        }

                        nowPlayingHeader(count: success.streams.count)

                        if success.streams.isEmpty {
                            VStack(spacing: 12) {
                                Image(systemName: "tv")
                                    .font(.system(size: 48))
                                    .foregroundColor(.secondary)
                                Text(MR.strings().no_active_streams.localized())
                                    .foregroundColor(.secondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.top, 30)
                        } else {
                            LazyVStack(spacing: 16) {
                                ForEach(success.streams, id: \.id) { session in
                                    PlaybackStreamCardView(
                                        session: session,
                                        onTerminate: {
                                            viewModel.setSelectedStream(session)
                                        }
                                    )
                                    .contentShape(Rectangle())
                                    .onTapGesture {
                                        viewModel.setSelectedStream(session)
                                    }
                                }
                            }
                        }

                        historyHeader

                        let historyItems = Array(success.history.prefix(5))
                        if historyItems.isEmpty {
                            VStack(spacing: 8) {
                                Text(MR.strings().no_history.localized())
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 20)
                        } else {
                            LazyVStack(spacing: 16) {
                                ForEach(historyItems, id: \.id) { historyItem in
                                    PlaybackHistoryCardView(item: historyItem)
                                        .contentShape(Rectangle())
                                        .onTapGesture {
                                            viewModel.setSelectedHistoryStream(historyItem)
                                        }
                                }
                            }
                        }
                    }
                    .padding(16)
                }
                .refreshable {
                    viewModel.refresh()
                }
            } else if viewModel.state is MonitoringStateLoading || viewModel.state is MonitoringStateInitial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? MonitoringStateError {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundColor(.red)
                    Text(error.message)
                        .foregroundColor(.secondary)
                    Button(MR.strings().retry.localized()) {
                        viewModel.loadStreams()
                    }
                    .buttonStyle(.borderedProminent)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .sheet(item: Binding(
            get: { viewModel.selectedSession },
            set: { if $0 == nil { viewModel.clearSelected() } }
        )) { session in
            PlaybackStreamDetailsSheet(
                session: session,
                onTerminateStream: { message in
                    viewModel.terminateStream(session, message: message)
                },
                onNavigateToDetails: { mediaType, tmdbId in
                    if let tmdbId = tmdbId {
                        navigationManager.goToDetails(tmdbId: tmdbId, requestType: mediaType?.requestType)
                    }
                },
                onNavigateToUser: { _ in
                    navigationManager.go(to: TracearrRoute.users)
                }
            )
        }
        .navigationTitle(MR.strings().tautulli.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if !instancesViewModel.instancesState.instances.isEmpty && (!globalPreferences.hideInstanceSwitcher || instancesViewModel.instancesState.instances.count > 1) {
                ToolbarItem(placement: .topBarLeading) {
                    InstancePickerMenu(
                        instances: instancesViewModel.instancesState.instances,
                        selectedInstanceId: instancesViewModel.instancesState.selectedInstance?.id,
                        onChangeInstance: { instancesViewModel.setInstanceActive($0) },
                        onAddNewInstance: { navigationManager.goToNewInstance(of: instancesViewModel.instancesState.selectedInstance?.type ?? .tautulli) }
                    )
                    .menuIndicator(.hidden)
                }
            } else if navigationManager.shouldShowDrawerButton(for: TabItemStandard.monitoring.key) {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        navigationManager.showLauncher = true
                    } label: {
                        Image(systemName: "line.3.horizontal")
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func nowPlayingHeader(count: Int) -> some View {
        HStack(spacing: 8) {
            Image(systemName: "tv")
                .font(.title3)
                .foregroundColor(.accentColor)
            Text(MR.strings().now_playing.localized())
                .font(.title3.bold())
            Text(MR.plurals().streams.localized(Int32(count)))
                .font(.caption.bold())
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(Color(UIColor.secondarySystemBackground))
                .clipShape(Capsule())
            Spacer()
        }
    }

    @ViewBuilder
    private var historyHeader: some View {
        HStack(spacing: 8) {
            Image(systemName: "clock.arrow.circlepath")
                .font(.title3)
                .foregroundColor(.accentColor)
            Text(MR.strings().history.localized())
                .font(.title3.bold())
            Spacer()
            Button(MR.strings().all.localized()) {
                navigationManager.go(to: TracearrRoute.history)
            }
            .font(.subheadline)
        }
    }
}

struct TautulliDashboardStatsView: View {
    let stats: PlaybackSummaryStats
    var isExpanded: Bool = false
    var onNavigateToHistory: (() -> Void)? = nil
    var onNavigateToUsers: (() -> Void)? = nil
    var onNavigateToStats: (() -> Void)? = nil

    private static let tautulliOrange = Color(hex: 0xe5a00d)
    private static let tautulliDark = Color(hex: 0x8a5b00)

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            let columns = isExpanded
                ? [GridItem(.adaptive(minimum: 140, maximum: .infinity), spacing: 12)]
                : [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)]

            LazyVGrid(columns: columns, spacing: 12) {
                CompactStatCard(
                    iconName: "tv.fill",
                    label: MR.strings().active_streams.localized(),
                    value: "\(stats.activeStreamsCount)",
                    containerColor: stats.activeStreamsCount > 0 ? Self.tautulliOrange : Color(UIColor.secondarySystemBackground),
                    contentColor: stats.activeStreamsCount > 0 ? .black : .primary,
                    onClick: onNavigateToHistory
                )

                CompactStatCard(
                    iconName: "play.fill",
                    label: MR.strings().total_plays.localized(),
                    value: "\(stats.todayTotalPlays)",
                    containerColor: Self.tautulliDark.opacity(0.2),
                    contentColor: .primary,
                    onClick: onNavigateToStats
                )

                CompactStatCard(
                    iconName: "clock.fill",
                    label: MR.strings().watch_time.localized(),
                    value: formatDuration(stats.todayTotalDurationSeconds),
                    containerColor: Color(UIColor.secondarySystemBackground),
                    contentColor: .primary,
                    onClick: onNavigateToStats
                )

                CompactStatCard(
                    iconName: "person.2.fill",
                    label: MR.strings().users.localized(),
                    value: "\(stats.activeUsersCount)",
                    containerColor: Color(UIColor.secondarySystemBackground),
                    contentColor: .primary,
                    onClick: onNavigateToUsers
                )
            }
        }
    }

    private func formatDuration(_ seconds: Int64) -> String {
        if seconds <= 0 { return "0h" }
        let hours = seconds / 3600
        let days = hours / 24
        if days > 0 {
            return "\(days)d \(hours % 24)h"
        }
        return "\(hours)h"
    }
}
