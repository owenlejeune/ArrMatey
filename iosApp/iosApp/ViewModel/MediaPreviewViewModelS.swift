//
//  MediaPreviewViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class MediaPreviewViewModelS: ObservableObject {
    private let viewModel: MediaPreviewViewModel
    
    @Published private(set) var qualityProfiles: [QualityProfile] = []
    @Published private(set) var rootFolders: [RootFolder] = []
    @Published private(set) var tags: [Tag] = []
    @Published private(set) var addItemStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var lastAddedItemId: Int64? = nil
    
    init(type: InstanceType) {
        self.viewModel = KoinBridge.shared.getMediaPreviewViewModel(type: type)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.qualityProfiles.observeAsync { self.qualityProfiles = $0 }
        viewModel.rootFolders.observeAsync { self.rootFolders = $0 }
        viewModel.tags.observeAsync { self.tags = $0 }
        viewModel.addItemStatus.observeAsync { self.addItemStatus = $0 }
        viewModel.lastAddedItemId.observeAsync { self.lastAddedItemId = $0?.int64Value }
    }
    
    func addItem(_ item: ArrMedia, _ searchOnAdd: Bool) {
        viewModel.addItem(item: item, searchOnAdd: searchOnAdd)
    }
    
}
