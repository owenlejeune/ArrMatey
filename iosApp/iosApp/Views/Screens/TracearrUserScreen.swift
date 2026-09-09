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
            if viewModel.state is TracearrUserStateNoInstance {
                NoInstanceView(type: .tracearr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TracearrUserStateSuccess {
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
            } else if viewModel.state is TracearrUserStateLoading || viewModel.state is TracearrUserStateInitial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TracearrUserStateError {
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
        if let success = viewModel.state as? TracearrUserStateSuccess,
           let name = success.userDetail?.effectiveUsername,
           !name.isEmpty {
            return name
        }
        return MR.strings().user_details.localized()
    }

    // MARK: - User Info Card
    @ViewBuilder
    private func userInfoCard(detail: TracearrUserDetail) -> some View {
        let username = detail.effectiveUsername.isEmpty ? MR.strings().user.localized() : detail.effectiveUsername
        let avatarUrl = detail.effectiveAvatarUrl

        VStack(alignment: .leading, spacing: 12) {
            Text(MR.strings().user_info.localized())
                .font(.headline)
                .bold()

            HStack(spacing: 16) {
                if let avatarStr = avatarUrl, !avatarStr.isEmpty {
                    TracearrImage(urlString: avatarStr) {
                        Circle().fill(Color.orange)
                    }
                    .aspectRatio(contentMode: .fill)
                    .frame(width: 60, height: 60)
                    .clipShape(Circle())
                } else {
                    Circle()
                        .fill(Color.orange)
                        .frame(width: 60, height: 60)
                        .overlay(
                            Text(String(username.prefix(1)).uppercased())
                                .font(.title2.bold())
                                .foregroundColor(.white)
                        )
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text(username)
                        .font(.title3.bold())

                    if let email = detail.email, !email.isEmpty {
                        Text(email)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }

                    if let trustScore = detail.effectiveTrustScore {
                        HStack(spacing: 4) {
                            Image(systemName: "checkmark.shield.fill")
                                .font(.caption)
                            Text("\(trustScore.intValue) · Trusted")
                                .font(.caption.bold())
                        }
                        .foregroundColor(Color(hex: 0x19D2E7))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color(hex: 0x19D2E7).opacity(0.15))
                        .clipShape(Capsule())
                    }
                }
            }

            let createdAt = detail.effectiveCreatedAt
            let lastActive = detail.effectiveLastActivityAt

            if createdAt != nil || lastActive != nil {
                HStack {
                    if let created = createdAt {
                        HStack(spacing: 4) {
                            Image(systemName: "calendar")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("\(MR.strings().joined.localized()): \(created)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }

                    Spacer()

                    if let active = lastActive {
                        HStack(spacing: 4) {
                            Image(systemName: "clock")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("\(MR.strings().last_activity.localized()): \(active)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding(.top, 4)
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

            FlowLayout(spacing: 8) {
                userStatCard(
                    iconName: "play.fill",
                    value: "\(allTime?.plays.int64Value ?? 0)",
                    label: MR.strings().plays.localized()
                )

                userStatCard(
                    iconName: "clock",
                    value: formatWatchTime(ms: allTime?.watchTimeMs.int64Value ?? 0),
                    label: MR.strings().watch_time.localized()
                )

                if let l30 = last30 {
                    userStatCard(
                        iconName: "play.fill",
                        value: "\(l30.plays.int64Value)",
                        label: "Last 30 Days"
                    )
                }

                if let l7 = last7 {
                    userStatCard(
                        iconName: "play.fill",
                        value: "\(l7.plays.int64Value)",
                        label: "Last 7 Days"
                    )
                }
            }

            if !stats.topGenres.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text(MR.strings().top_genres.localized())
                        .font(.caption.bold())
                        .foregroundColor(.secondary)

                    FlowLayout(spacing: 8) {
                        ForEach(stats.topGenres, id: \.genre) { genreStat in
                            if let genre = genreStat.genre {
                                Text("\(genre) (\(genreStat.plays.int64Value))")
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
    private func userStatCard(iconName: String, value: String, label: String) -> some View {
        HStack(spacing: 12) {
            RoundedRectangle(cornerRadius: 8)
                .fill(Color(hex: 0x19D2E7).opacity(0.12))
                .frame(width: 36, height: 36)
                .overlay(
                    Image(systemName: iconName)
                        .font(.system(size: 16))
                        .foregroundColor(Color(hex: 0x19D2E7))
                )

            VStack(alignment: .leading, spacing: 2) {
                Text(value)
                    .font(.headline.bold())
                    .lineLimit(1)
                Text(label)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(1)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color(UIColor.tertiarySystemBackground))
        .cornerRadius(10)
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

    private func formatWatchTime(ms: Int64) -> String {
        if ms <= 0 { return "0m" }
        let totalSeconds = ms / 1000
        let totalMinutes = totalSeconds / 60
        let days = totalMinutes / (24 * 60)
        let hours = (totalMinutes % (24 * 60)) / 60
        let minutes = totalMinutes % 60

        if days > 0 {
            return "\(days)d \(hours)h"
        } else if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }
}
