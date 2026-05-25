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
    
    func approveRequest(_ requestId: Int64) {
        viewModel.approveRequest(requestId: requestId)
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
