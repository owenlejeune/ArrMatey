//
//  RequestsViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class RequestsViewModelS: ObservableObject {
    private let viewModel: RequestsViewModel

    @Published private(set) var requestsState = PagedData<MediaRequestPackage>()
    @Published private(set) var issuesState = PagedData<MediaIssuePackage>()
    @Published private(set) var operationsState: RequestOperationsState = RequestOperationsState()
    @Published private(set) var userState: SeerrUser? = nil
    @Published private(set) var selectedTab: SeerrTab = .requests
    @Published private(set) var requestActionStatus: OperationStatus = OperationStatusIdle()

    init() {
        self.viewModel = KoinBridge.shared.getRequestsViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.requestsState.observeAsync(on: self, to: \.requestsState)
        viewModel.issuesState.observeAsync(on: self, to: \.issuesState)
        viewModel.operationsState.observeAsync(on: self, to: \.operationsState)
        viewModel.userState.observeAsync(on: self, to: \.userState)
        viewModel.selectedTab.observeAsync(on: self, to: \.selectedTab)
        viewModel.requestActionStatus.observeAsync(on: self, to: \.requestActionStatus)
    }

    func resetRequestActionStatus() {
        viewModel.resetRequestActionStatus()
    }

    func setSelectedTab(_ tab: SeerrTab) {
        viewModel.setSelectedTab(tab: tab)
    }

    func refresh() {
        viewModel.refresh()
    }

    func loadNextRequestsPage() {
        viewModel.loadNextRequestsPage()
    }

    func loadNextIssuesPage() {
        viewModel.loadNextIssuesPage()
    }

    func retryRequests() {
        viewModel.retryRequests()
    }

    func retryIssues() {
        viewModel.retryIssues()
    }

    func clearRequestsError() {
        viewModel.clearRequestsError()
    }

    func clearIssuesError() {
        viewModel.clearIssuesError()
    }

    func approveRequest(
        _ requestId: Int64,
        profileId: Int64? = nil,
        rootFolder: String? = nil,
        languageProfileId: Int64? = nil,
        seasons: [Int32]? = nil
    ) {
        let seasonsKotlin = seasons?.map { KotlinInt(value: $0) }
        viewModel.approveRequest(
            requestId: requestId,
            profileId: profileId?.asKotlinLong,
            rootFolder: rootFolder,
            languageProfileId: languageProfileId?.asKotlinLong,
            seasons: seasonsKotlin
        )
    }

    func declineRequest(_ requestId: Int64) {
        viewModel.declineRequest(requestId: requestId)
    }

    func cancelRequest(_ requestId: Int64) {
        viewModel.cancelRequest(requestId: requestId)
    }

    func deleteMediaFile(_ request: MediaRequest) {
        viewModel.deleteMediaFile(request: request)
    }
}
