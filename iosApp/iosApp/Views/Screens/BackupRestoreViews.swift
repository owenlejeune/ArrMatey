//
//  BackupRestoreViews.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import SwiftUI
import Shared
import UniformTypeIdentifiers

struct ExportSheet: View {
    @ObservedObject var viewModel: BackupViewModelS
    @Binding var isPresented: Bool
    var onExport: (String) -> Void
    
    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text(MR.strings().password.localized())) {
                    SecureField(MR.strings().password.localized(), text: Binding(
                        get: { viewModel.exportState.password },
                        set: { viewModel.setExportPassword(password: $0) }
                    ))
                    Text(MR.strings().export_password_prompt.localized())
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Section {
                    Toggle(MR.strings().include_preferences.localized(), isOn: Binding(
                        get: { viewModel.exportState.includeInstancePreferences },
                        set: { _ in viewModel.toggleIncludePreferences() }
                    ))
                    
                    Toggle(MR.strings().navigation_bar_configuration.localized(), isOn: Binding(
                        get: { viewModel.exportState.includeTabPreferences },
                        set: { _ in viewModel.toggleIncludeTabPreferences() }
                    ))
                    
                    Toggle(MR.strings().user_interface.localized(), isOn: Binding(
                        get: { viewModel.exportState.includeUiPreferences },
                        set: { _ in viewModel.toggleIncludeUiPreferences() }
                    ))
                }
                
                Section(header: Text(MR.strings().select_items_to_export.localized())) {
                    if !viewModel.exportState.instances.isEmpty {
                        Text(MR.strings().instances.localized())
                            .font(.caption)
                            .foregroundColor(.themePrimary)
                        
                        ForEach(viewModel.exportState.instances, id: \.id) { instance in
                            Toggle(instance.label, isOn: Binding(
                                get: { viewModel.exportState.selectedInstanceIds.contains(instance.id.asKotlinLong) },
                                set: { _ in viewModel.toggleInstanceSelection(id: instance.id) }
                            ))
                        }
                    }
                    
                    if !viewModel.exportState.downloadClients.isEmpty {
                        Text(MR.strings().download_clients.localized())
                            .font(.caption)
                            .foregroundColor(.themePrimary)
                            .padding(.top, 8)
                        
                        ForEach(viewModel.exportState.downloadClients, id: \.id) { client in
                            Toggle(client.label, isOn: Binding(
                                get: { viewModel.exportState.selectedDownloadClientIds.contains(client.id.asKotlinLong) },
                                set: { _ in viewModel.toggleDownloadClientSelection(id: client.id) }
                            ))
                        }
                    }
                }
            }
            .navigationTitle(MR.strings().export_data.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        isPresented = false
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(MR.strings().save.localized()) {
                        viewModel.exportData { encryptedData in
                            onExport(encryptedData)
                            isPresented = false
                        }
                    }
                    .disabled(viewModel.exportState.password.isEmpty || (viewModel.exportState.selectedInstanceIds.isEmpty && viewModel.exportState.selectedDownloadClientIds.isEmpty))
                }
            }
        }
    }
}

struct ImportSheet: View {
    @ObservedObject var viewModel: BackupViewModelS
    @Binding var isPresented: Bool
    var encryptedData: String
    var onComplete: () -> Void
    
    var body: some View {
        NavigationStack {
            Form {
                if viewModel.importState.decryptedBackup == nil {
                    Section(header: Text(MR.strings().password.localized())) {
                        SecureField(MR.strings().password.localized(), text: Binding(
                            get: { viewModel.importState.password },
                            set: { viewModel.setImportPassword(password: $0) }
                        ))
                        Text(MR.strings().import_password_prompt.localized())
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        if let error = viewModel.importState.error {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                    }
                } else {
                    Section(header: Text(MR.strings().select_items_to_import.localized())) {
                        if let backup = viewModel.importState.decryptedBackup {
                            if !backup.instances.isEmpty {
                                Text(MR.strings().instances.localized())
                                    .font(.caption)
                                    .foregroundColor(.themePrimary)
                                
                                ForEach(Array(backup.instances.enumerated()), id: \.offset) { index, instance in
                                    Toggle(instance.label, isOn: Binding(
                                        get: { viewModel.importState.selectedInstanceIndices.contains(Int32(index).asKotlinInt) },
                                        set: { _ in viewModel.toggleImportInstanceSelection(index: Int32(index)) }
                                    ))
                                }
                            }
                            
                            if !backup.downloadClients.isEmpty {
                                Text(MR.strings().download_clients.localized())
                                    .font(.caption)
                                    .foregroundColor(.themePrimary)
                                    .padding(.top, 8)
                                
                                ForEach(Array(backup.downloadClients.enumerated()), id: \.offset) { index, client in
                                    Toggle(client.label, isOn: Binding(
                                        get: { viewModel.importState.selectedDownloadClientIndices.contains(Int32(index).asKotlinInt) },
                                        set: { _ in viewModel.toggleImportDownloadClientSelection(index: Int32(index)) }
                                    ))
                                }
                            }
                            
                            if backup.globalPreferences != nil {
                                Text(MR.strings().backup_restore.localized())
                                    .font(.caption)
                                    .foregroundColor(.themePrimary)
                                    .padding(.top, 8)
                                
                                if backup.globalPreferences?.tabPreferences != nil {
                                    Toggle(MR.strings().navigation_bar_configuration.localized(), isOn: Binding(
                                        get: { viewModel.importState.importTabPreferences },
                                        set: { _ in viewModel.toggleImportTabPreferences() }
                                    ))
                                }
                                
                                if backup.globalPreferences?.useServiceNavLogos != nil || backup.globalPreferences?.hideInstanceSwitcher != nil {
                                    Toggle(MR.strings().user_interface.localized(), isOn: Binding(
                                        get: { viewModel.importState.importUiPreferences },
                                        set: { _ in viewModel.toggleImportUiPreferences() }
                                    ))
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle(MR.strings().import_data.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        isPresented = false
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    if viewModel.importState.decryptedBackup == nil {
                        Button(MR.strings().ok.localized()) {
                            viewModel.prepareImport(encryptedData: encryptedData)
                        }
                        .disabled(viewModel.importState.password.isEmpty)
                    } else {
                        Button(MR.strings().import_data.localized()) {
                            viewModel.executeImport {
                                onComplete()
                                isPresented = false
                            }
                        }
                        .disabled(viewModel.importState.selectedInstanceIndices.isEmpty && viewModel.importState.selectedDownloadClientIndices.isEmpty)
                    }
                }
            }
        }
    }
}

struct BackupFile: FileDocument {
    static var readableContentTypes: [UTType] { [.json] }
    
    var data: String
    
    init(data: String) {
        self.data = data
    }
    
    init(configuration: ReadConfiguration) throws {
        if let data = configuration.file.regularFileContents {
            self.data = String(data: data, encoding: .utf8) ?? ""
        } else {
            self.data = ""
        }
    }
    
    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        let data = self.data.data(using: .utf8) ?? Data()
        return FileWrapper(regularFileWithContents: data)
    }
}
