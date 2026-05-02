//
//  AuthorFilesViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import Shared
import SwiftUI

@MainActor
class AuthorFilesViewModelS: ObservableObject {
    private let viewModel: AuthorFilesViewModel
    
    @Published private(set) var uiState: AuthorFilesState = AuthorFilesState()
    
    init(authorId: Int64) {
        self.viewModel = KoinBridge.shared.getAuthorFilesViewModel(authorId: authorId)
        
        viewModel.uiState.observeAsync { self.uiState = $0 }
    }
    
    func refreshHistory() {
        viewModel.refreshHistory()
    }
}
