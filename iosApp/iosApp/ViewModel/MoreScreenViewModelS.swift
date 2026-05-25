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
    @Published private(set) var customWebpages: [CustomWebpage] = []
    @Published private(set) var connectionStatuses: [KotlinLong:OperationStatus] = [:]
    @Published private(set) var useServiceNavLogos: Bool = false
    @Published private(set) var hideInstanceSwitcher: Bool = false
    
    init() {
        self.viewModel = KoinBridge.shared.getMoreScreenViewModel()
        
        viewModel.instances.observeAsync(on: self, to: \.instances)
        viewModel.downloadClients.observeAsync(on: self, to: \.downloadClients)
        viewModel.customWebpages.observeAsync(on: self, to: \.customWebpages)
        viewModel.testingStatus.observeAsync(on: self, to: \.connectionStatuses)
        viewModel.useServiceNavLogos.observeAsync(on: self) { owner, useLogos in
            owner.useServiceNavLogos = useLogos.boolValue
        }
        viewModel.hideInstanceSwitcher.observeAsync(on: self) { owner, hide in
            owner.hideInstanceSwitcher = hide.boolValue
        }
    }
    
    func toggleUseServiceNavLogos() {
        viewModel.toggleUseServiceNavLogos()
    }
    
    func toggleInstanceSwitcher() {
        viewModel.toggleInstanceSwitcher()
    }
}
