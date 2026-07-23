//
//  SeerrRequestsScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrTabContent: View {
    @StateObject private var viewModel = RequestsViewModelS()
    @StateObject private var instancesViewModel = InstancesViewModelS(type: .seerr)
    @EnvironmentObject private var navigationManager: NavigationManager
    
    var body: some View {
        Group {
            if instancesViewModel.instancesState.selectedInstance == nil {
                NoInstanceView(type: .seerr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                VStack(spacing: 0) {
                    Picker("", selection: Binding(
                        get: { viewModel.selectedTab == .requests ? 0 : 1 },
                        set: { viewModel.setSelectedTab($0 == 0 ? .requests : .issues) }
                    )) {
                        requestsTabLabel.tag(0)
                        issuesTabLabel.tag(1)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    
                    if viewModel.selectedTab == .requests {
                        RequestsContentView(
                            pagedData: viewModel.requestsState,
                            userState: viewModel.userState,
                            operationsState: viewModel.operationsState,
                            onApprove: { viewModel.approveRequest($0) },
                            onDecline: { viewModel.declineRequest($0) },
                            onEdit: { _ in },
                            onDelete: { viewModel.cancelRequest($0) },
                            onRemoveFromService: { viewModel.deleteMediaFile($0) },
                            onNavigateToDetails: { tmdbId, type in
                                navigationManager.goToSeerrDetails(tmdbId: tmdbId, requestType: type)
                            },
                            onLoadMore: { viewModel.loadNextRequestsPage() },
                            onRetry: { viewModel.retryRequests() },
                            onClearError: { viewModel.clearRequestsError() }
                        )
                    } else {
                        IssuesContentView(
                            pagedData: viewModel.issuesState,
                            onLoadMore: { viewModel.loadNextIssuesPage() },
                            onRetry: { viewModel.retryIssues() },
                            onClearError: { viewModel.clearIssuesError() }
                        )
                    }
                }
            }
        }
        .navigationTitle(MR.strings().seerr.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    navigationManager.showLauncher = true
                } label: {
                    Image(systemName: "line.3.horizontal")
                }
            }
        }
        .refreshable {
            viewModel.refresh()
        }
    }
    
    private var requestsTabLabel: Text {
        let count = viewModel.requestsState.totalItemCount
        if count > 0 {
            return Text("\(MR.strings().requests.localized()) (\(count))")
        }
        return Text(MR.strings().requests.localized())
    }
    
    private var issuesTabLabel: Text {
        let count = viewModel.issuesState.totalItemCount
        if count > 0 {
            return Text("\(MR.strings().issues.localized()) (\(count))")
        }
        return Text(MR.strings().issues.localized())
    }
}

// MARK: - Requests Content

private struct RequestsContentView: View {
    let pagedData: PagedData<MediaRequestPackage>
    let userState: SeerrUser?
    let operationsState: RequestOperationsState
    let onApprove: (Int64) -> Void
    let onDecline: (Int64) -> Void
    let onEdit: (Int64) -> Void
    let onDelete: (Int64) -> Void
    let onRemoveFromService: (MediaRequest) -> Void
    let onNavigateToDetails: (Int64, RequestType) -> Void
    let onLoadMore: () -> Void
    let onRetry: () -> Void
    let onClearError: () -> Void
    
    var body: some View {
        ZStack {
            if pagedData.isLoading && pagedData.items.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if pagedData.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "tray")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary)
                    Text(MR.strings().no_requests_found.localized())
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                RequestsListView(
                    items: pagedData.items as! [MediaRequestPackage],
                    hasMore: pagedData.hasMore,
                    isLoadingMore: pagedData.isLoadingMore,
                    userState: userState,
                    operationsState: operationsState,
                    onApprove: onApprove,
                    onDecline: onDecline,
                    onEdit: onEdit,
                    onDelete: onDelete,
                    onRemoveFromService: onRemoveFromService,
                    onNavigateToDetails: onNavigateToDetails,
                    onLoadMore: onLoadMore
                )
            }
            
            if let error = pagedData.error {
                VStack {
                    Spacer()
                    ErrorBannerView(
                        error: error,
                        onRetry: onRetry,
                        onDismiss: onClearError
                    )
                    .padding(16)
                }
            }
        }
    }
}

// MARK: - Issues Content

private struct IssuesContentView: View {
    let pagedData: PagedData<MediaIssuePackage>
    let onLoadMore: () -> Void
    let onRetry: () -> Void
    let onClearError: () -> Void
    
    @State private var selectedIssue: MediaIssuePackage? = nil
    
    var body: some View {
        ZStack {
            if pagedData.isLoading && pagedData.items.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if pagedData.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "checkmark.circle")
                        .font(.system(size: 48))
                        .foregroundColor(.secondary)
                    Text(MR.strings().no_issues_found.localized())
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                IssuesListView(
                    items: pagedData.items as! [MediaIssuePackage],
                    hasMore: pagedData.hasMore,
                    isLoadingMore: pagedData.isLoadingMore,
                    onLoadMore: onLoadMore,
                    onSelectIssue: { selectedIssue = $0 }
                )
            }
            
            if let error = pagedData.error {
                VStack {
                    Spacer()
                    ErrorBannerView(
                        error: error,
                        onRetry: onRetry,
                        onDismiss: onClearError
                    )
                    .padding(16)
                }
            }
        }
        .sheet(item: Binding(
            get: { selectedIssue.map { IdentifiableIssue(package: $0) } },
            set: { selectedIssue = $0?.package }
        )) { wrapper in
            SeerrIssueDetailsSheet(
                issuePackage: wrapper.package,
                onDismiss: { selectedIssue = nil }
            )
        }
    }
}

// MARK: - Requests List

private struct RequestsListView: View {
    let items: [MediaRequestPackage]
    let hasMore: Bool
    let isLoadingMore: Bool
    let userState: SeerrUser?
    let operationsState: RequestOperationsState
    let onApprove: (Int64) -> Void
    let onDecline: (Int64) -> Void
    let onEdit: (Int64) -> Void
    let onDelete: (Int64) -> Void
    let onRemoveFromService: (MediaRequest) -> Void
    let onNavigateToDetails: (Int64, RequestType) -> Void
    let onLoadMore: () -> Void
    
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(items, id: \.request.id) { rPackage in
                    SeerrRequestCard(
                        mediaPackage: rPackage,
                        user: userState,
                        operationsState: operationsState,
                        onApprove: { onApprove(rPackage.request.id) },
                        onDecline: { onDecline(rPackage.request.id) },
                        onEdit: { onEdit(rPackage.request.id) },
                        onDelete: { onDelete(rPackage.request.id) },
                        onRemoveFromService: { onRemoveFromService(rPackage.request) },
                        onClick: {
                            onNavigateToDetails(
                                rPackage.request.media.tmdbId,
                                rPackage.request.type
                            )
                        }
                    )
                    .onAppear {
                        if rPackage.request.id == items.last?.request.id && hasMore && !isLoadingMore {
                            onLoadMore()
                        }
                    }
                }
                
                if isLoadingMore {
                    ProgressView()
                        .padding(16)
                }
            }
            .padding(16)
        }
    }
}

// MARK: - Issues List

private struct IssuesListView: View {
    let items: [MediaIssuePackage]
    let hasMore: Bool
    let isLoadingMore: Bool
    let onLoadMore: () -> Void
    let onSelectIssue: (MediaIssuePackage) -> Void
    
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(items, id: \.issue.id) { issuePackage in
                    SeerrIssueCard(
                        issuePackage: issuePackage,
                        onClick: { onSelectIssue(issuePackage) }
                    )
                    .onAppear {
                        if issuePackage.issue.id == items.last?.issue.id && hasMore && !isLoadingMore {
                            onLoadMore()
                        }
                    }
                }
                
                if isLoadingMore {
                    ProgressView()
                        .padding(16)
                }
            }
            .padding(16)
        }
    }
}

// MARK: - Error Banner

private struct ErrorBannerView: View {
    let error: String
    let onRetry: () -> Void
    let onDismiss: () -> Void
    
    var body: some View {
        HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.white)
            Text(error)
                .font(.subheadline)
                .foregroundColor(.white)
                .lineLimit(2)
            Spacer()
            Button(MR.strings().retry.localized()) { onRetry() }
                .font(.subheadline.bold())
                .foregroundColor(.white)
            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .foregroundColor(.white)
            }
        }
        .padding(12)
        .background(Color.red.opacity(0.9))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - Helpers

private struct IdentifiableIssue: Identifiable {
    let package: MediaIssuePackage
    var id: Int64 { package.issue.id }
}
