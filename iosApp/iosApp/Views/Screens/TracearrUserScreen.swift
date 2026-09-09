import Shared
import SwiftUI

struct TracearrUserScreen: View {
    let userRef: String

    @StateObject private var viewModel: TracearrUserViewModelS
    @EnvironmentObject private var navigationManager: NavigationManager
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    private var isLargeScreen: Bool { horizontalSizeClass == .regular }

    init(userRef: String) {
        self.userRef = userRef
        _viewModel = StateObject(wrappedValue: TracearrUserViewModelS(userRef: userRef))
    }

    var body: some View {
        Group {
            if viewModel.state is TracearrUserState.NoInstance {
                NoInstanceView(type: .tracearr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TracearrUserState.Success {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        if let detail = success.userDetail {
                            userInfoCard(detail: detail)
                        }

                        if let stats = success.userStats {
                            userStatsCard(stats: stats)
                        }

                        recentSessionsHeader

                        if success.history.isEmpty {
                            VStack(spacing: 8) {
                                Text(MR.strings().no_history.localized())
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 24)
                        } else if isLargeScreen {
                            TracearrHistoryTableView(
                                items: success.history,
                                hasMore: success.hasMoreHistory,
                                isLoadingMore: success.isLoadingMoreHistory,
                                onLoadMore: {
                                    viewModel.loadMoreHistory()
                                },
                                onClickItem: { historyItem in
                                    viewModel.setSelectedHistoryStream(historyItem)
                                }
                            )
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(success.history, id: \.id) { historyItem in
                                    TracearrHistoryCardView(item: historyItem)
                                        .contentShape(Rectangle())
                                        .onTapGesture {
                                            viewModel.setSelectedHistoryStream(historyItem)
                                        }
                                        .onAppear {
                                            if historyItem.id == success.history.last?.id && success.hasMoreHistory && !success.isLoadingMoreHistory {
                                                viewModel.loadMoreHistory()
                                            }
                                        }
                                }

                                if success.isLoadingMoreHistory {
                                    ProgressView()
                                        .padding(16)
                                }
                            }
                        }
                    }
                    .padding(16)
                }
                .refreshable {
                    viewModel.refresh()
                }
            } else if viewModel.state is TracearrUserState.Loading || viewModel.state is TracearrUserState.Initial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TracearrUserState.Error {
                VStack(spacing: 12) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 48))
                        .foregroundColor(.red)
                    Text(error.message)
                        .foregroundColor(.secondary)
                    Button(MR.strings().retry.localized()) {
                        viewModel.refresh()
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
            TracearrStreamDetailsSheet(
                session: session,
                onNavigateToDetails: { mediaType, tmdbId in
                    if let tmdbId = tmdbId {
                        navigationManager.goToDetails(
                            tmdbId: tmdbId,
                            requestType: mediaType?.requestType
                        )
                    }
                }
            )
        }
        .navigationTitle(navigationTitleText)
        .navigationBarTitleDisplayMode(.inline)
    }

    private var navigationTitleText: String {
        if let success = viewModel.state as? TracearrUserState.Success,
           let name = success.userDetail?.username,
           !name.isEmpty {
            return name
        }
        return MR.strings().user_details.localized()
    }

    // MARK: - User Info Card
    @ViewBuilder
    private func userInfoCard(detail: TracearrUserDetail) -> some View {
        let username = (detail.username?.isEmpty == false) ? detail.username! : MR.strings().unknown.localized()

        VStack(alignment: .leading, spacing: 12) {
            Text(MR.strings().user_info.localized())
                .font(.headline)
                .bold()

            HStack(spacing: 16) {
                Circle()
                    .fill(Color(UIColor.tertiarySystemBackground))
                    .frame(width: 60, height: 60)
                    .overlay(
                        Text(String(username.prefix(1)).uppercased())
                            .font(.title2.bold())
                            .foregroundColor(.primary)
                    )

                VStack(alignment: .leading, spacing: 4) {
                    Text(username)
                        .font(.title3.bold())

                    if let email = detail.email, !email.isEmpty {
                        Text(email)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
            }

            if !detail.accounts.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text(MR.strings().accounts.localized())
                        .font(.caption.bold())
                        .foregroundColor(.secondary)

                    FlowLayout(spacing: 8) {
                        ForEach(detail.accounts, id: \.serverId) { account in
                            let serverName = account.serverType?.name ?? "Server"
                            let userLabel = account.username ?? account.serverUserId ?? account.externalUserId ?? ""
                            let label = !userLabel.isEmpty ? "\(serverName): \(userLabel)" : serverName
                            let serverColor = Color.serverColor(type: account.serverType, name: account.username)

                            HStack(spacing: 4) {
                                Image(systemName: "server.rack")
                                    .font(.caption2)
                                    .foregroundColor(serverColor)
                                Text(label)
                                    .font(.caption.bold())
                                    .foregroundColor(serverColor)
                            }
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(serverColor.opacity(0.12))
                            .overlay(
                                RoundedRectangle(cornerRadius: 6)
                                    .stroke(serverColor.opacity(0.4), lineWidth: 1)
                            )
                            .cornerRadius(6)
                        }
                    }
                }
            }
        }
        .padding(16)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }

    // MARK: - User Stats Card
    @ViewBuilder
    private func userStatsCard(stats: TracearrUserStats) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(MR.strings().statistics.localized())
                .font(.headline)
                .bold()

            let windows = stats.windows
            let allTime = windows?.allTime
            let last30 = windows?.last30
            let last7 = windows?.last7

            let columns = isLargeScreen ? Array(repeating: GridItem(.flexible(), spacing: 12), count: 4) : [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)]

            LazyVGrid(columns: columns, spacing: 12) {
                CountStatItem(
                    iconName: "play.fill",
                    label: MR.strings().plays.localized(),
                    count: Int(allTime?.plays ?? 0),
                    containerColor: Color(UIColor.tertiarySystemBackground)
                )

                CompactStatCard(
                    iconName: "clock",
                    label: MR.strings().watch_time.localized(),
                    value: formatWatchTime(ms: allTime?.watchTimeMs ?? 0),
                    containerColor: Color(UIColor.tertiarySystemBackground)
                )

                if let l30 = last30 {
                    CompactStatCard(
                        iconName: "play.fill",
                        label: "Last 30 Days",
                        value: "\(l30.plays)",
                        containerColor: Color(UIColor.tertiarySystemBackground)
                    )
                }

                if let l7 = last7 {
                    CompactStatCard(
                        iconName: "play.fill",
                        label: "Last 7 Days",
                        value: "\(l7.plays)",
                        containerColor: Color(UIColor.tertiarySystemBackground)
                    )
                }
            }

            if !stats.topGenres.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text(MR.strings().top_genres.localized())
                        .font(.caption.bold())
                        .foregroundColor(.secondary)

                    FlowLayout(spacing: 8) {
                        ForEach(Array(stats.topGenres.enumerated()), id: \.offset) { _, genreStat in
                            if let genre = genreStat.genre {
                                Text("\(genre) (\(genreStat.plays))")
                                    .font(.caption)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 4)
                                    .background(Color(UIColor.tertiarySystemBackground))
                                    .clipShape(Capsule())
                            }
                        }
                    }
                }
            }
        }
        .padding(16)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }

    @ViewBuilder
    private var recentSessionsHeader: some View {
        HStack(spacing: 8) {
            Image(systemName: "clock.arrow.circlepath")
                .font(.title3)
                .foregroundColor(.accentColor)
            Text(MR.strings().recent_sessions.localized())
                .font(.title3.bold())
            Spacer()
        }
    }
}
