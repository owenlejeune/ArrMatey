import Shared
import SwiftUI

struct TautulliUsersScreen: View {
    @StateObject private var viewModel = TautulliUsersViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        Group {
            if viewModel.state is TautulliUsersState.NoInstance {
                NoInstanceView(type: .tautulli)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TautulliUsersState.Success {
                VStack(spacing: 0) {
                    searchBar

                    if success.filteredUsers.isEmpty {
                        VStack(spacing: 8) {
                            Text(MR.strings().no_results_found.localized())
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        ScrollView {
                            LazyVStack(spacing: 12) {
                                ForEach(success.filteredUsers, id: \.user.userId) { item in
                                    TautulliUserCardView(item: item)
                                }
                            }
                            .padding(16)
                        }
                        .refreshable {
                            viewModel.refresh()
                        }
                    }
                }
            } else if viewModel.state is TautulliUsersState.Loading || viewModel.state is TautulliUsersState.Initial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TautulliUsersState.Error {
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
        .navigationTitle(MR.strings().users.localized())
        .navigationBarTitleDisplayMode(.inline)
    }

    private var searchBar: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.secondary)
            TextField(MR.strings().search.localized(), text: Binding(
                get: { viewModel.searchQuery },
                set: { viewModel.updateSearchQuery($0) }
            ))
            if !viewModel.searchQuery.isEmpty {
                Button {
                    viewModel.updateSearchQuery("")
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(8)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(10)
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
}

struct TautulliUserCardView: View {
    let item: TautulliUserItem

    private static let tautulliOrange = Color(hex: 0xe5a00d)

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 12) {
                if let avatarStr = item.avatarUrl, let url = URL(string: avatarStr) {
                    ArrAsyncImage(url: url) { image in
                        image
                            .resizable()
                            .scaledToFill()
                    } placeholder: {
                        Circle().fill(Color.orange)
                    }
                    .frame(width: 44, height: 44)
                    .clipShape(Circle())
                } else {
                    Circle()
                        .fill(Color.orange)
                        .frame(width: 44, height: 44)
                        .overlay(
                            Text(String(item.user.displayName.prefix(1)).uppercased())
                                .font(.headline.bold())
                                .foregroundColor(.white)
                        )
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(item.user.displayName)
                        .font(.headline)
                        .lineLimit(1)

                    let sub = subtitleText
                    if !sub.isEmpty {
                        Text(sub)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }

                Spacer()
            }

            if let stats = item.stats, ((stats.totalPlays?.int64Value ?? 0) > 0 || (stats.totalTime?.int64Value ?? 0) > 0) {
                HStack(spacing: 8) {
                    if let plays = stats.totalPlays?.int32Value, plays > 0 {
                        HStack(spacing: 4) {
                            Image(systemName: "play.fill")
                                .font(.system(size: 10))
                            Text(MR.plurals().plays_count.localized(plays))
                                .font(.caption2.bold())
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Self.tautulliOrange.opacity(0.15))
                        .foregroundColor(Self.tautulliOrange)
                        .clipShape(Capsule())
                    }

                    if let timeSeconds = stats.totalTime?.int64Value, timeSeconds > 0 {
                        HStack(spacing: 4) {
                            Image(systemName: "clock.fill")
                                .font(.system(size: 10))
                            Text(formatDuration(timeSeconds))
                                .font(.caption2.bold())
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Self.tautulliOrange.opacity(0.15))
                        .foregroundColor(Self.tautulliOrange)
                        .clipShape(Capsule())
                    }
                }
            }

            if !item.sharedLibraryNames.isEmpty {
                FlowLayout(spacing: 6) {
                    HStack(spacing: 4) {
                        Image(systemName: "server.rack")
                            .font(.system(size: 10))
                        Text(item.serverName)
                            .font(.caption2.bold())
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Self.tautulliOrange.opacity(0.15))
                    .foregroundColor(Self.tautulliOrange)
                    .clipShape(Capsule())

                    ForEach(item.sharedLibraryNames, id: \.self) { lib in
                        HStack(spacing: 4) {
                            Image(systemName: "film")
                                .font(.system(size: 10))
                            Text(lib)
                                .font(.caption2)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(UIColor.tertiarySystemBackground))
                        .foregroundColor(.secondary)
                        .clipShape(Capsule())
                    }
                }
            }
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }

    private var subtitleText: String {
        let u = item.user
        if let friendly = u.friendlyName, let username = u.username, !friendly.isEmpty, !username.isEmpty, friendly != username {
            return username
        }
        if let email = u.email, !email.isEmpty {
            return email
        }
        return ""
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
