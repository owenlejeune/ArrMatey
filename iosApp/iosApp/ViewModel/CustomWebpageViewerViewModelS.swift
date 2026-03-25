//
//  CustomWebpageViewerViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-23.
//

import Shared
import SwiftUI

@MainActor
class CustomWebpageViewerViewModelS: ObservableObject {
    private let viewModel: CustomWebpageViewerViewModel
    
    @Published private(set) var webpage: CustomWebpage? = nil
    
    init(webpageId: Int64) {
        self.viewModel = KoinBridge.shared.getCustomWebpageViewerViewModel(webpageId: webpageId)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.webpage.observeAsync { self.webpage = $0 }
    }
}
