import Shared
import SwiftUI

struct TracearrUsersScreen: View {
    @StateObject private var viewModel = TracearrUsersViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        Group {
            if viewModel.state is TracearrUsersState.NoInstance {
                NoInstanceView(type: .tracearr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TracearrUsersState.Success {
                if success.filteredUsers.isEmpty {
                    VStack(spacing: 8) {
                        Text(success.searchQuery.isEmpty ? MR.strings().no_history.localized() : MR.strings().no_results_found.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(success.filteredUsers, id: \.id) { userDetail in
                                TracearrUserCardView(
                                    detail: userDetail,
                                    stats: success.userStatsMap[userDetail.id]
                                )
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    navigationManager.go(to: TracearrRoute.user(userDetail.id))
                                }
                                .onAppear {
                                    if userDetail.id == success.users.last?.id && success.hasMore && !success.isLoadingMore {
                                        viewModel.loadMore()
                                    }
                                }
                            }

                            if success.isLoadingMore {
                                ProgressView()
                                    .padding(16)
                            }
                        }
                        .padding(16)
                    }
                    .refreshable {
                        viewModel.refresh()
                    }
                }
            } else if viewModel.state is TracearrUsersState.Loading || viewModel.state is TracearrUsersState.Initial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TracearrUsersState.Error {
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
        .searchable(text: $viewModel.searchQuery, prompt: MR.strings().search.localized())
        .navigationTitle(MR.strings().users.localized())
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct TracearrUserCardView: View {
    let detail: TracearrUserDetail
    let stats: TracearrUserStats?

    var body: some View {
        let username = (detail.username?.isEmpty == false) ? detail.username! : MR.strings().unknown.localized()

        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 16) {
                Circle()
                    .fill(Color(UIColor.tertiarySystemBackground))
                    .frame(width: 48, height: 48)
                    .overlay(
                        Text(String(username.prefix(1)).uppercased())
                            .font(.title3.bold())
                            .foregroundColor(.primary)
                    )

                VStack(alignment: .leading, spacing: 2) {
                    Text(username)
                        .font(.headline)
                        .bold()

                    if let email = detail.email, !email.isEmpty {
                        Text(email)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()
            }

            if let stats = stats, let allTime = stats.windows?.allTime {
                HStack(spacing: 12) {
                    HStack(spacing: 4) {
                        Image(systemName: "play.fill")
                            .font(.caption2)
                        Text(MR.plurals().plays_count.localized(Int32(allTime.plays)))
                            .font(.caption2.bold())
                    }
                    .foregroundColor(Color(hex: 0x00b4d8))
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(hex: 0x00b4d8).opacity(0.12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 6)
                            .stroke(Color(hex: 0x00b4d8).opacity(0.5), lineWidth: 1)
                    )
                    .cornerRadius(6)

                    HStack(spacing: 4) {
                        Image(systemName: "clock")
                            .font(.caption2)
                        Text(formatWatchTime(ms: allTime.watchTimeMs))
                            .font(.caption2.bold())
                    }
                    .foregroundColor(Color(hex: 0x00b4d8))
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(hex: 0x00b4d8).opacity(0.12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 6)
                            .stroke(Color(hex: 0x00b4d8).opacity(0.5), lineWidth: 1)
                    )
                    .cornerRadius(6)
                }
            }

            if let stats = stats, !stats.topGenres.isEmpty {
                FlowLayout(spacing: 4) {
                    ForEach(Array(stats.topGenres.enumerated()), id: \.offset) { _, genre in
                        let genreText = genre.genre ?? MR.strings().unknown.localized()
                        Text("\(genreText) (\(genre.plays))")
                            .font(.caption2)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(Color(UIColor.tertiarySystemBackground))
                            .clipShape(Capsule())
                    }
                }
            }

            if !detail.accounts.isEmpty {
                FlowLayout(spacing: 8) {
                    ForEach(detail.accounts, id: \.serverId) { account in
                        let serverName = account.serverType?.name ?? "Server"
                        let userLabel = account.username ?? account.serverUserId ?? account.externalUserId ?? ""
                        let label = !userLabel.isEmpty ? "\(serverName): \(userLabel)" : serverName
                        let serverColor = Color.serverColor(type: account.serverType, name: account.username)

                        HStack(spacing: 4) {
                            Image(systemName: "server.rack")
                                .font(.caption2)
                            Text(label)
                                .font(.caption.bold())
                                .foregroundColor(serverColor)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
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
        .padding(16)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
}
