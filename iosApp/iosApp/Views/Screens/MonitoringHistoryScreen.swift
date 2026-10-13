import Shared
import SwiftUI

struct MonitoringHistoryScreen: View {
    @StateObject private var viewModel = MonitoringHistoryViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    private var isLargeScreen: Bool { horizontalSizeClass == .regular }

    var body: some View {
        Group {
            if viewModel.state is MonitoringHistoryStateNoInstance {
                NoInstanceView(type: .tautulli)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? MonitoringHistoryStateSuccess {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text(MR.strings().history.localized())
                            .font(.title2.bold())

                        if success.items.isEmpty {
                            VStack(spacing: 8) {
                                Text(MR.strings().no_history.localized())
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 24)
                        } else {
                            LazyVStack(spacing: 12) {
                                ForEach(success.items, id: \.id) { historyItem in
                                    PlaybackHistoryCardView(item: historyItem)
                                        .contentShape(Rectangle())
                                        .onTapGesture {
                                            viewModel.setSelectedHistoryStream(historyItem)
                                        }
                                        .onAppear {
                                            if historyItem.id == success.items.last?.id && success.hasMore {
                                                viewModel.loadMore()
                                            }
                                        }
                                }

                                if success.isLoadingMore {
                                    ProgressView()
                                        .frame(maxWidth: .infinity)
                                        .padding()
                                }
                            }
                        }
                    }
                    .padding(16)
                }
                .refreshable {
                    viewModel.refresh()
                }
            } else if viewModel.state is MonitoringHistoryStateLoading || viewModel.state is MonitoringHistoryStateInitial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? MonitoringHistoryStateError {
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
            PlaybackStreamDetailsSheet(
                session: session,
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
        .navigationTitle(MR.strings().history.localized())
        .navigationBarTitleDisplayMode(.inline)
    }
}
