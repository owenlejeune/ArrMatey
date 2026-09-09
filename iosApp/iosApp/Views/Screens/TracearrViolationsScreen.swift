import Shared
import SwiftUI

struct TracearrViolationsScreen: View {
    @StateObject private var viewModel = TracearrViolationsViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        Group {
            if viewModel.state is TracearrViolationsState.NoInstance {
                NoInstanceView(type: .tracearr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TracearrViolationsState.Success {
                if success.filteredViolations.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: success.searchQuery.isEmpty ? "shield" : "exclamationmark.triangle")
                            .font(.system(size: 48))
                            .foregroundColor(.secondary)
                        Text(success.searchQuery.isEmpty ? MR.strings().no_violations_found.localized() : MR.strings().no_results_found.localized())
                            .font(.headline)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(success.filteredViolations, id: \.id) { violation in
                                TracearrViolationCardView(violation: violation)
                                    .onAppear {
                                        if violation.id == success.violations.last?.id && success.hasMore && !success.isLoadingMore {
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
            } else if viewModel.state is TracearrViolationsState.Loading || viewModel.state is TracearrViolationsState.Initial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TracearrViolationsState.Error {
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
        .navigationTitle(MR.strings().alerts.localized())
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct TracearrViolationCardView: View {
    let violation: TracearrViolation

    private var edgeColor: Color {
        Color.serverColor(type: nil, name: violation.serverName)
    }

    private var severityLower: String {
        violation.severity.name.lowercased()
    }

    private var severityColor: Color {
        switch violation.severity {
        case .high:
            return .red
        case .warning:
            return .orange
        default:
            return Color(hex: 0x00b4d8)
        }
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(edgeColor)
                .frame(width: 4)

            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    HStack(spacing: 8) {
                        Text(severityLower.capitalized)
                            .font(.caption2.bold())
                            .foregroundColor(severityColor)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(severityColor.opacity(0.15))
                            .clipShape(Capsule())

                        if violation.acknowledged {
                            HStack(spacing: 4) {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.caption2)
                                Text(MR.strings().acknowledged.localized())
                                    .font(.caption2.bold())
                            }
                            .foregroundColor(.green)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.green.opacity(0.15))
                            .clipShape(Capsule())
                        }
                    }

                    Spacer()

                    if let user = violation.user {
                        HStack(spacing: 6) {
                            let username = user.username ?? MR.strings().user.localized()
                            let avatarUrl = user.avatarUrl ?? user.thumbUrl

                            if let avatarStr = avatarUrl, !avatarStr.isEmpty {
                                TracearrImage(urlString: avatarStr) {
                                    Circle().fill(Color.orange)
                                }
                                .aspectRatio(contentMode: .fill)
                                .frame(width: 18, height: 18)
                                .clipShape(Circle())
                            } else {
                                Circle()
                                    .fill(Color.orange)
                                    .frame(width: 18, height: 18)
                                    .overlay(
                                        Text(String(username.prefix(1)).uppercased())
                                            .font(.caption2.bold())
                                            .foregroundColor(.white)
                                    )
                            }

                            Text(username)
                                .font(.caption.bold())
                                .lineLimit(1)
                        }
                    }
                }

                Text(violation.rule?.name ?? MR.strings().unknown.localized())
                    .font(.headline)
                    .bold()

                Divider()

                HStack {
                    if let server = violation.serverName {
                        HStack(spacing: 4) {
                            Image(systemName: "server.rack")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(server)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }

                    Spacer()

                    if let dateStr = violation.createdAt {
                        Text(String(dateStr.prefix(10)))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(12)
        }
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
}
