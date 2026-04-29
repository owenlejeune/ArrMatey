//
//  IssueDetailsViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class IssueDetailsViewModelS: ObservableObject {
    private let viewModel: IssueDetailsViewModel
    
    @Published private(set) var uiState: IssueDetailsUiState
    
    init(issuePackage: MediaIssuePackage) {
        let vm = KoinBridge.shared.getIssueDetailsViewModel(issuePackage: issuePackage)
        self.viewModel = vm
        self.uiState = IssueDetailsUiState(issuePackage: issuePackage)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync {
            self.uiState = $0
            print("comments: \($0.issuePackage.issue.comments.count)")
        }
    }
    
    func submitIssueComment(_ comment: String) {
        viewModel.submitIssueComment(comment: comment)
    }
    
    func closeIssue(_ issueId: Int64) {
        viewModel.closeIssue(issueId: issueId)
    }
}
