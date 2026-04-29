//
//  SeerrMediaDetailsViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class SeerrMediaDetailsViewModelS: ObservableObject {
    private let viewModel: SeerrMediaDetailsViewModel
    
    @Published private(set) var uiState: SeerrDetailsState = SeerrDetailsStateInitial()
    @Published private(set) var buttonState: MediaButtonState = MediaButtonState()
    @Published private(set) var selectedInstance: Instance? = nil
    @Published private(set) var isReportIssueSheetVisible: Bool = false
    @Published private(set) var isViewRequestSheetVisible: Bool = false
    
    init(tmdbId: Int64, requestType: RequestType) {
        self.viewModel = KoinBridge.shared.getSeerrMediaDetailsViewModel(tmdbId: tmdbId, mediaType: requestType)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync { self.uiState = $0 }
        viewModel.buttonState.observeAsync { self.buttonState = $0 }
        viewModel.selectedInstance.observeAsync { self.selectedInstance = $0 }
        viewModel.isReportIssueSheetVisible.observeAsync { self.isReportIssueSheetVisible = $0.boolValue }
        viewModel.isViewRequestSheetVisible.observeAsync { self.isViewRequestSheetVisible = $0.boolValue }
    }
    
    func refreshDetails() {
        viewModel.refreshDetails()
    }
    
    func showReportIssueSheet() {
        viewModel.showReportIssueSheet()
    }
    
    func hideReportIssueSheet() {
        viewModel.hideReportIssueSheet()
    }
    
    func showViewRequestSheet() {
        viewModel.showViewRequestSheet()
    }
    
    func hideViewRequestSheet() {
        viewModel.hideViewRequestSheet()
    }
    
    func declineRequest(_ requestId: Int64) {
        viewModel.declineRequest(requestId: requestId)
    }
}
