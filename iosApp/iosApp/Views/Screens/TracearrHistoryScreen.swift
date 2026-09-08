import Shared
import SwiftUI

struct TracearrHistoryScreen: View {
    @StateObject private var viewModel = TracearrHistoryViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        Group {
            if viewModel.state is TracearrHistoryStateNoInstance {
                NoInstanceView(type: .tracearr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let success = viewModel.state as? TracearrHistoryStateSuccess {
                if success.items.isEmpty {
                    VStack(spacing: 8) {
                        Text(MR.strings().no_history.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 12) {
                            ForEach(success.items, id: \.id) { historyItem in
                                TracearrHistoryCardView(item: historyItem)
                                    .contentShape(Rectangle())
                                    .onTapGesture {
                                        viewModel.setSelectedHistoryStream(historyItem)
                                    }
                                    .onAppear {
                                        if historyItem.id == success.items.last?.id && success.hasMore && !success.isLoadingMore {
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
            } else if viewModel.state is TracearrHistoryStateLoading || viewModel.state is TracearrHistoryStateInitial {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = viewModel.state as? TracearrHistoryStateError {
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
        .navigationTitle(MR.strings().history.localized())
        .navigationBarTitleDisplayMode(.inline)
    }
}
