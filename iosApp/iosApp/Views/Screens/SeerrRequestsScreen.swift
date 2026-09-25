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

    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack {
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
                                selectedFilter: viewModel.selectedFilter,
                                onFilterSelected: { viewModel.setFilter($0) },
                                onApprove: { viewModel.approveRequest($0) },
                                onApproveWithDetails: { id, profileId, rootFolder, lang, seasons in
                                    viewModel.approveRequest(id, profileId: profileId, rootFolder: rootFolder, languageProfileId: lang, seasons: seasons)
                                },
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
                                selectedFilter: viewModel.selectedIssueFilter,
                                onFilterSelected: { viewModel.setIssueFilter($0) },
                                onLoadMore: { viewModel.loadNextIssuesPage() },
                                onRetry: { viewModel.retryIssues() },
                                onClearError: { viewModel.clearIssuesError() },
                                onRefresh: { viewModel.refresh() }
                            )
                        }
                    }
                }
            }
            toastOverlay
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
        .onReceive(viewModel.$requestActionStatus) { status in
            if let success = status as? OperationStatusSuccess {
                let msg: String
                if success.message == "Request approved" {
                    msg = MR.strings().request_approved.localized()
                } else if success.message == "Request declined" {
                    msg = MR.strings().request_declined.localized()
                } else if let message = success.message, !message.isEmpty {
                    msg = message
                } else {
                    msg = MR.strings().success.localized()
                }
                withAnimation {
                    toastMessage = msg
                }
                viewModel.resetRequestActionStatus()
            } else if let error = status as? OperationStatusError {
                withAnimation {
                    toastMessage = error.message
                }
                viewModel.resetRequestActionStatus()
            }
        }
    }

    @ViewBuilder
    private var toastOverlay: some View {
        if let message = toastMessage {
            VStack {
                Spacer()
                Text(message)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(Color.black.opacity(0.75))
                    .cornerRadius(20)
                    .padding(.bottom, 24)
            }
            .transition(.opacity)
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                    withAnimation {
                        toastMessage = nil
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var requestsTabLabel: some View {
        let count = viewModel.pendingRequestsCount
        if count > 0 {
            Text("\(MR.strings().requests.localized()) (\(count))")
        } else {
            Text(MR.strings().requests.localized())
        }
    }

    @ViewBuilder
    private var issuesTabLabel: some View {
        let count = viewModel.openIssuesCount
        if count > 0 {
            Text("\(MR.strings().issues.localized()) (\(count))")
        } else {
            Text(MR.strings().issues.localized())
        }
    }
}

// MARK: - Requests Content

struct RequestsContentView: View {
    let pagedData: PagedData<MediaRequestPackage>
    let userState: SeerrUser?
    let operationsState: RequestOperationsState
    let selectedFilter: RequestState
    let onFilterSelected: (RequestState) -> Void
    let onApprove: (Int64) -> Void
    var onApproveWithDetails: ((Int64, Int64?, String?, Int64?, [Int32]?) -> Void)? = nil
    let onDecline: (Int64) -> Void
    let onEdit: (Int64) -> Void
    let onDelete: (Int64) -> Void
    let onRemoveFromService: (MediaRequest) -> Void
    let onNavigateToDetails: (Int64, RequestType) -> Void
    let onLoadMore: () -> Void
    let onRetry: () -> Void
    let onClearError: () -> Void

    @State private var selectedPackageForSheet: MediaRequestPackage? = nil

    var body: some View {
        VStack(spacing: 0) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(RequestState.allCases, id: \.self) { state in
                        let isSelected = selectedFilter == state
                        Button(action: { onFilterSelected(state) }) {
                            Text(state.resource.localized())
                                .font(.subheadline)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(isSelected ? Color.accentColor : Color(.secondarySystemBackground))
                                .foregroundColor(isSelected ? .white : .primary)
                                .clipShape(Capsule())
                                .overlay(
                                    Capsule()
                                        .stroke(Color.primary.opacity(0.1), lineWidth: isSelected ? 0 : 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }

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
                    selectedFilter: selectedFilter,
                    hasMore: pagedData.hasMore,
                    isLoadingMore: pagedData.isLoadingMore,
                    loadMoreFailed: pagedData.loadMoreFailed,
                    userState: userState,
                    operationsState: operationsState,
                    onApprove: onApprove,
                    onDecline: onDecline,
                    onEdit: onEdit,
                    onDelete: onDelete,
                    onRemoveFromService: onRemoveFromService,
                    onNavigateToDetails: onNavigateToDetails,
                    onLoadMore: onLoadMore,
                    onViewRequest: { selectedPackageForSheet = $0 }
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
        .sheet(item: Binding(
            get: { selectedPackageForSheet.map { IdentifiableRequestPackage(package: $0) } },
            set: { selectedPackageForSheet = $0?.package }
        )) { wrapper in
            if let details = wrapper.package.details {
                SeerrViewRequestSheet(
                    details: details,
                    request: wrapper.package.request,
                    serviceDetails: wrapper.package.serviceDetails,
                    onDismissRequest: { selectedPackageForSheet = nil },
                    onApproveRequest: { requestId, profileId, rootFolder, languageProfileId, seasons in
                        if let onApproveWithDetails {
                            onApproveWithDetails(requestId, profileId, rootFolder, languageProfileId, seasons)
                        } else {
                            onApprove(requestId)
                        }
                        selectedPackageForSheet = nil
                    },
                    onDeclineRequest: { requestId in
                        onDecline(requestId)
                        selectedPackageForSheet = nil
                    },
                    onViewMedia: { tmdbId, type in
                        selectedPackageForSheet = nil
                        onNavigateToDetails(tmdbId, type)
                    }
                )
            }
        }
    }
}

// MARK: - Issues Content

struct IssuesContentView: View {
    let pagedData: PagedData<MediaIssuePackage>
    let selectedFilter: IssueState
    let onFilterSelected: (IssueState) -> Void
    let onLoadMore: () -> Void
    let onRetry: () -> Void
    let onClearError: () -> Void
    let onRefresh: () -> Void

    @State private var selectedIssue: MediaIssuePackage? = nil

    var body: some View {
        VStack(spacing: 0) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(IssueState.allCases, id: \.self) { state in
                        let isSelected = selectedFilter == state
                        Button(action: { onFilterSelected(state) }) {
                            Text(state.resource.localized())
                                .font(.subheadline)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(isSelected ? Color.accentColor : Color(.secondarySystemBackground))
                                .foregroundColor(isSelected ? .white : .primary)
                                .clipShape(Capsule())
                                .overlay(
                                    Capsule()
                                        .stroke(Color.primary.opacity(0.1), lineWidth: isSelected ? 0 : 1)
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }

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
                        selectedFilter: selectedFilter,
                        hasMore: pagedData.hasMore,
                        isLoadingMore: pagedData.isLoadingMore,
                        loadMoreFailed: pagedData.loadMoreFailed,
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
        }
        .sheet(item: Binding(
            get: { selectedIssue.map { IdentifiableIssue(package: $0) } },
            set: { selectedIssue = $0?.package }
        )) { wrapper in
            SeerrIssueDetailsSheet(
                issuePackage: wrapper.package,
                onDismiss: { selectedIssue = nil },
                onIssueClosed: {
                    selectedIssue = nil
                    onRefresh()
                }
            )
        }
    }
}

// MARK: - Requests List

private struct RequestsListView: View {
    let items: [MediaRequestPackage]
    let selectedFilter: RequestState
    let hasMore: Bool
    let isLoadingMore: Bool
    var loadMoreFailed: Bool = false
    let userState: SeerrUser?
    let operationsState: RequestOperationsState
    let onApprove: (Int64) -> Void
    let onDecline: (Int64) -> Void
    let onEdit: (Int64) -> Void
    let onDelete: (Int64) -> Void
    let onRemoveFromService: (MediaRequest) -> Void
    let onNavigateToDetails: (Int64, RequestType) -> Void
    let onLoadMore: () -> Void
    var onViewRequest: ((MediaRequestPackage) -> Void)? = nil

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 12) {
                    Color.clear
                        .frame(height: 0)
                        .id("requestsTop")

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
                        },
                        onViewRequest: { onViewRequest?(rPackage) }
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
                    } else if loadMoreFailed {
                        Button(action: onLoadMore) {
                            Image(systemName: "arrow.clockwise")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.accentColor)
                                .padding(8)
                        }
                        .buttonStyle(.plain)
                        .padding(16)
                    }
                }
                .padding(16)
            }
            .id(selectedFilter)
            .onChange(of: selectedFilter) { _ in
                proxy.scrollTo("requestsTop", anchor: .top)
            }
        }
    }
}

// MARK: - Issues List

private struct IssuesListView: View {
    let items: [MediaIssuePackage]
    let selectedFilter: IssueState
    let hasMore: Bool
    let isLoadingMore: Bool
    var loadMoreFailed: Bool = false
    let onLoadMore: () -> Void
    let onSelectIssue: (MediaIssuePackage) -> Void

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 12) {
                    Color.clear
                        .frame(height: 0)
                        .id("issuesTop")

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
                    } else if loadMoreFailed {
                        Button(action: onLoadMore) {
                            Image(systemName: "arrow.clockwise")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(.accentColor)
                                .padding(8)
                        }
                        .buttonStyle(.plain)
                        .padding(16)
                    }
                }
                .padding(16)
            }
            .id(selectedFilter)
            .onChange(of: selectedFilter) { _ in
                proxy.scrollTo("issuesTop", anchor: .top)
            }
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

struct IdentifiableRequestPackage: Identifiable {
    let package: MediaRequestPackage
    var id: Int64 { package.request.id }
}

struct IdentifiableIssue: Identifiable {
    let package: MediaIssuePackage
    var id: Int64 { package.issue.id }
}

struct SeerrSheetView: View {
    @ObservedObject var viewModel: RequestsViewModelS
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("", selection: Binding(
                    get: { viewModel.selectedTab == .requests ? 0 : 1 },
                    set: { viewModel.setSelectedTab($0 == 0 ? .requests : .issues) }
                )) {
                    Text(MR.strings().requests.localized()).tag(0)
                    Text(MR.strings().issues.localized()).tag(1)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)

                if viewModel.selectedTab == .requests {
                    RequestsContentView(
                        pagedData: viewModel.requestsState,
                        userState: viewModel.userState,
                        operationsState: viewModel.operationsState,
                        selectedFilter: viewModel.selectedFilter,
                        onFilterSelected: { viewModel.setFilter($0) },
                        onApprove: { viewModel.approveRequest($0) },
                        onApproveWithDetails: { id, profileId, rootFolder, lang, seasons in
                            viewModel.approveRequest(id, profileId: profileId, rootFolder: rootFolder, languageProfileId: lang, seasons: seasons)
                        },
                        onDecline: { viewModel.declineRequest($0) },
                        onEdit: { _ in },
                        onDelete: { viewModel.cancelRequest($0) },
                        onRemoveFromService: { viewModel.deleteMediaFile($0) },
                        onNavigateToDetails: { tmdbId, type in
                            dismiss()
                            navigationManager.goToSeerrDetailsOnDashboard(tmdbId: tmdbId, requestType: type)
                        },
                        onLoadMore: { viewModel.loadNextRequestsPage() },
                        onRetry: { viewModel.retryRequests() },
                        onClearError: { viewModel.clearRequestsError() }
                    )
                } else {
                    IssuesContentView(
                        pagedData: viewModel.issuesState,
                        selectedFilter: viewModel.selectedIssueFilter,
                        onFilterSelected: { viewModel.setIssueFilter($0) },
                        onLoadMore: { viewModel.loadNextIssuesPage() },
                        onRetry: { viewModel.retryIssues() },
                        onClearError: { viewModel.clearIssuesError() },
                        onRefresh: { viewModel.refresh() }
                    )
                }
            }
            .navigationTitle(MR.strings().seerr.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(MR.strings().close.localized()) {
                        dismiss()
                    }
                }
            }
        }
    }
}
