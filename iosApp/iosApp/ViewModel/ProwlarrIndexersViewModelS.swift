//
//  ProwlarrIndexersViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-21.
//

import Shared
import SwiftUI

@MainActor
class ProwlarrIndexersViewModelS: ObservableObject {
    private let viewModel: ProwlarrIndexersViewModel
    
    @Published private(set) var indexers: IndexersState = IndexersStateInitial()
    
    init() {
        self.viewModel = KoinBridge.shared.getProwlarrIndexersViewModel()
        
        viewModel.indexers.observeAsync { self.indexers = $0 }
    }
}
