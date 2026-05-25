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
        viewModel.uiState.observeAsync(on: self, to: \.uiState)
        viewModel.buttonState.observeAsync(on: self, to: \.buttonState)
        viewModel.selectedInstance.observeAsync(on: self, to: \.selectedInstance)
        viewModel.isReportIssueSheetVisible.observeAsync(on: self) { owner, visible in
            owner.isReportIssueSheetVisible = visible.boolValue
        }
        viewModel.isViewRequestSheetVisible.observeAsync(on: self) { owner, visible in
            owner.isViewRequestSheetVisible = visible.boolValue
        }
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
