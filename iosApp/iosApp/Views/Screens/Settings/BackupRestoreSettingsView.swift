//
//  BackupRestoreSettingsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-08-31.
//

import SwiftUI
import Shared
import UniformTypeIdentifiers

struct BackupRestoreSettingsView: View {
    @StateObject private var backupViewModel = BackupViewModelS()

    @State private var showExportSheet: Bool = false
    @State private var showFileExporter: Bool = false
    @State private var showFileImporter: Bool = false
    @State private var exportData: String = ""
    @State private var importData: String = ""
    @State private var showExportSuccess: Bool = false
    @State private var showImportSuccess: Bool = false

    var body: some View {
        Form {
            Section {
                Button {
                    showExportSheet = true
                } label: {
                    Label(MR.strings().backup.localized(), systemImage: "square.and.arrow.up")
                }

                Button {
                    showFileImporter = true
                } label: {
                    Label(MR.strings().import_data.localized(), systemImage: "square.and.arrow.down")
                }
            } header: {
                Text(MR.strings().backup_restore.localized())
            }
        }
        .navigationTitle(MR.strings().backup_restore.localized())
        .sheet(isPresented: $showExportSheet) {
            ExportSheet(viewModel: backupViewModel, isPresented: $showExportSheet) { data in
                self.exportData = data
                self.showFileExporter = true
            }
        }
        .fileExporter(isPresented: $showFileExporter, document: BackupFile(data: exportData), contentType: .json, defaultFilename: "\(TimeExtensionsKt.nowTimestamp())_ArrMatey_Backup.json") { result in
            switch result {
            case .success:
                self.exportData = ""
                self.showExportSuccess = true
            case .failure(let error):
                print("Export failed: \(error.localizedDescription)")
            }
        }
        .fileImporter(isPresented: $showFileImporter, allowedContentTypes: [.json]) { result in
            switch result {
            case .success(let url):
                if url.startAccessingSecurityScopedResource() {
                    defer { url.stopAccessingSecurityScopedResource() }
                    if let data = try? Data(contentsOf: url), let string = String(data: data, encoding: .utf8) {
                        self.importData = string
                        // Need a way to trigger ImportSheet from here.
                        // In the original it was in SettingsScreen.
                    }
                }
            case .failure(let error):
                print("Import failed: \(error.localizedDescription)")
            }
        }
        .sheet(isPresented: Binding(get: { !importData.isEmpty }, set: { if !$0 { importData = "" } })) {
            ImportSheet(viewModel: backupViewModel, isPresented: Binding(get: { !importData.isEmpty }, set: { if !$0 { importData = "" } }), encryptedData: importData) {
                self.importData = ""
                self.showImportSuccess = true
            }
        }
        .alert(MR.strings().success.localized(), isPresented: $showExportSuccess) {
            Button(MR.strings().ok.localized(), role: .cancel) { }
        } message: {
            Text(MR.strings().export_ready.localized())
        }
        .alert(MR.strings().success.localized(), isPresented: $showImportSuccess) {
            Button(MR.strings().ok.localized(), role: .cancel) { }
        } message: {
            Text(MR.strings().import_complete.localized())
        }
    }
}
