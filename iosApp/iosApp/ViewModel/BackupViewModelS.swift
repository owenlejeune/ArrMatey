//
//  BackupViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class BackupViewModelS: ObservableObject {
    private let viewModel: BackupViewModel
    
    @Published var exportState = ExportUiState()
    @Published var importState = ImportUiState()
    
    init() {
        self.viewModel = KoinBridge.shared.getBackupViewModel()
        
        viewModel.exportUiState.observeAsync(on: self, to: \.exportState)
        viewModel.importUiState.observeAsync(on: self, to: \.importState)
    }
    
    func toggleInstanceSelection(id: Int64) {
        viewModel.toggleInstanceSelection(id: id)
    }
    
    func toggleDownloadClientSelection(id: Int64) {
        viewModel.toggleDownloadClientSelection(id: id)
    }
    
    func setExportPassword(password: String) {
        viewModel.setExportPassword(password: password)
    }
    
    func toggleIncludePreferences() {
        viewModel.toggleIncludePreferences()
    }
    
    func toggleIncludeTabPreferences() {
        viewModel.toggleIncludeTabPreferences()
    }
    
    func toggleIncludeUiPreferences() {
        viewModel.toggleIncludeUiPreferences()
    }
    
    func exportData(onExportReady: @escaping (String) -> Void) {
        viewModel.exportData(onExportReady: onExportReady)
    }
    
    func setImportPassword(password: String) {
        viewModel.setImportPassword(password: password)
    }
    
    func prepareImport(encryptedData: String) {
        viewModel.prepareImport(encryptedData: encryptedData)
    }
    
    func toggleImportInstanceSelection(index: Int32) {
        viewModel.toggleImportInstanceSelection(index: index)
    }
    
    func toggleImportDownloadClientSelection(index: Int32) {
        viewModel.toggleImportDownloadClientSelection(index: index)
    }
    
    func toggleImportTabPreferences() {
        viewModel.toggleImportTabPreferences()
    }
    
    func toggleImportUiPreferences() {
        viewModel.toggleImportUiPreferences()
    }
    
    func executeImport(onComplete: @escaping () -> Void) {
        viewModel.executeImport(onComplete: onComplete)
    }
}
