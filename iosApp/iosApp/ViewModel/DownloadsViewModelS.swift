//
//  DownloadsViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-21.
//

import Shared
import SwiftUI

@MainActor
class DownloadsViewModelS: ObservableObject {
    private let viewModel: DownloadsViewModel
    
    @Published private(set) var queueState: DownloadsState = DownloadsStateInitial()
    
    init() {
        self.viewModel = KoinBridge.shared.getDownloadsViewModel()
        
        viewModel.queueState.observeAsync { self.queueState = $0 }
    }
}
