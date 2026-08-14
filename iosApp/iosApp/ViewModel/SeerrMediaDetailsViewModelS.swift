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
    @Published var isReportIssueSheetVisible: Bool = false
    @Published var isViewRequestSheetVisible: Bool = false
    @Published var isRequestSheetVisible: Bool = false
    @Published private(set) var currentUser: SeerrUser? = nil
    @Published private(set) var users: [SeerrUser] = []
    @Published private(set) var serviceDetails: ServiceDetails? = nil
    @Published private(set) var personCredits: PersonCredits? = nil

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
        viewModel.isRequestSheetVisible.observeAsync(on: self) { owner, visible in
            owner.isRequestSheetVisible = visible.boolValue
        }
        viewModel.currentUser.observeAsync(on: self, to: \.currentUser)
        viewModel.users.observeAsync(on: self, to: \.users)
        viewModel.serviceDetails.observeAsync(on: self, to: \.serviceDetails)
        viewModel.personCredits.observeAsync(on: self, to: \.personCredits)
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

    func showRequestSheet() {
        viewModel.showRequestSheet()
    }

    func hideRequestSheet() {
        viewModel.hideRequestSheet()
    }
    
    func declineRequest(_ requestId: Int64) {
        viewModel.declineRequest(requestId: requestId)
    }

    func submitRequest(
        profileId: Int64?,
        rootFolder: String?,
        languageProfileId: Int64?,
        seasons: [KotlinInt]?,
        userId: Int64?
    ) {
        viewModel.submitRequest(
            profileId: profileId.map { KotlinLong(value: $0) },
            rootFolder: rootFolder,
            languageProfileId: languageProfileId.map { KotlinLong(value: $0) },
            seasons: seasons,
            is4k: false,
            userId: userId.map { KotlinLong(value: $0) }
        )
    }
}
