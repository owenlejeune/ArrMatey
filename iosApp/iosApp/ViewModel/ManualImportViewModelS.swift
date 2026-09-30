//
//  ManualImportViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class ManualImportViewModelS: ObservableObject {
    private let viewModel: ManualImportViewModel

    @Published private(set) var isLoading: Bool = true
    @Published private(set) var isWorking: Bool = false
    @Published private(set) var error: String? = nil
    @Published private(set) var files: [ManualImportFile] = []
    @Published private(set) var selectedIds: Set<String> = []
    @Published private(set) var importSuccess: Bool = false

    init(item: QueueItem) {
        self.viewModel = KoinBridge.shared.getManualImportViewModel(item: item)
        startObserving()
    }

    private func startObserving() {
        viewModel.isLoading.observeAsync(on: self) { owner, isLoading in
            owner.isLoading = isLoading.boolValue
        }
        viewModel.isWorking.observeAsync(on: self) { owner, isWorking in
            owner.isWorking = isWorking.boolValue
        }
        viewModel.error.observeAsync(on: self, to: \.error)
        viewModel.files.observeAsync(on: self, to: \.files)
        viewModel.selectedIds.observeAsync(on: self, to: \.selectedIds)
        viewModel.importSuccess.observeAsync(on: self) { owner, importSuccess in
            owner.importSuccess = importSuccess.boolValue
        }
    }

    func loadFiles() {
        viewModel.loadFiles()
    }

    func toggleFileSelected(_ file: ManualImportFile) {
        viewModel.toggleFileSelected(file: file)
    }

    func isSelected(_ file: ManualImportFile) -> Bool {
        selectedIds.contains(file.stableId)
    }

    func importFiles(onSuccess: (() -> Void)? = nil) {
        viewModel.importFiles(onSuccess: onSuccess)
    }

    func clearError() {
        viewModel.clearError()
    }
}
