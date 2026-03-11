//
//  MoreScreenViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class MoreScreenViewModelS: ObservableObject {
    private let viewModel: MoreScreenViewModel
    
    @Published private(set) var instances: [Instance] = []
    @Published private(set) var downloadClients: [DownloadClient] = []
    @Published private(set) var connectionStatuses: [KotlinLong:OperationStatus] = [:]
    @Published private(set) var useServiceNavLogos: Bool = false
    
    init() {
        self.viewModel = KoinBridge.shared.getMoreScreenViewModel()
        
        viewModel.instances.observeAsync { self.instances = $0 }
        viewModel.downloadClients.observeAsync { self.downloadClients = $0 }
        viewModel.testingStatus.observeAsync { self.connectionStatuses = $0 }
        viewModel.useServiceNavLogos.observeAsync { self.useServiceNavLogos = $0.boolValue }
    }
    
    func toggleUseServiceNavLogos() {
        viewModel.toggleUseServiceNavLogos()
    }
}
