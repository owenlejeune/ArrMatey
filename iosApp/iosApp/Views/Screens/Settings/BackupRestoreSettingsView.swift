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
    @State private var showGenerateQrSheet: Bool = false
    @State private var showScanQrSheet: Bool = false
    @State private var isQrImport: Bool = false
    @State private var exportData: String = ""
    @State private var importData: String = ""
    @State private var showExportSuccess: Bool = false
    @State private var showImportSuccess: Bool = false
    @State private var showOnboarding: Bool = false

    var body: some View {
        Form {
            Section {
                Button {
                    showExportSheet = true
                } label: {
                    Label(MR.strings().backup.localized(), systemImage: "square.and.arrow.up")
                }

                Button {
                    isQrImport = false
                    showFileImporter = true
                } label: {
                    Label(MR.strings().import_data.localized(), systemImage: "square.and.arrow.down")
                }

                Button {
                    showGenerateQrSheet = true
                } label: {
                    Label(MR.strings().generate_qr_code.localized(), systemImage: "qrcode")
                }

                Button {
                    showScanQrSheet = true
                } label: {
                    Label(MR.strings().scan_qr_code.localized(), systemImage: "qrcode.viewfinder")
                }
            } header: {
                Text(MR.strings().backup_restore.localized())
            }

            Section {
                Button {
                    showOnboarding = true
                } label: {
                    Label(MR.strings().dev_settings_launch_onboarding.localized(), systemImage: "paperplane")
                }
            } header: {
                Text(MR.strings().onboarding.localized())
            }
        }
        .navigationTitle(MR.strings().backup_restore.localized())
        .fullScreenCover(isPresented: $showOnboarding) {
            OnboardingView {
                showOnboarding = false
            }
        }
        .sheet(isPresented: $showExportSheet) {
            ExportSheet(viewModel: backupViewModel, isPresented: $showExportSheet) { data in
                self.exportData = data
                self.showFileExporter = true
            }
        }
        .sheet(isPresented: $showGenerateQrSheet) {
            GenerateQrCodeSheet(viewModel: backupViewModel, isPresented: $showGenerateQrSheet)
        }
        .sheet(isPresented: $showScanQrSheet) {
            ScanQrCodeSheet(isPresented: $showScanQrSheet) { code in
                backupViewModel.resetImportState()
                self.isQrImport = true
                self.importData = code
            }
        }
        .fileExporter(isPresented: $showFileExporter, document: BackupFile(data: exportData), contentType: .json, defaultFilename: backupViewModel.getBackupFilename()) { result in
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
                        backupViewModel.resetImportState()
                        self.isQrImport = false
                        self.importData = string
                    }
                }
            case .failure(let error):
                print("Import failed: \(error.localizedDescription)")
            }
        }
        .sheet(isPresented: Binding(get: { !importData.isEmpty }, set: { if !$0 { importData = "" } }), onDismiss: {
            backupViewModel.resetImportState()
            self.isQrImport = false
        }) {
            ImportSheet(viewModel: backupViewModel, isPresented: Binding(get: { !importData.isEmpty }, set: { if !$0 { importData = "" } }), encryptedData: importData, isQrImport: isQrImport) {
                self.importData = ""
                self.isQrImport = false
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
