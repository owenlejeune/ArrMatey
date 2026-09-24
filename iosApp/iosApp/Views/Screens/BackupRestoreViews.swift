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
                Section(header: Text(MR.strings().password.localized()), footer: Text(MR.strings().export_password_prompt.localized())) {
                    SecureField(MR.strings().password.localized(), text: Binding(
                        get: { viewModel.exportState.password },
                        set: { viewModel.setExportPassword(password: $0) }
                    ))
                }
                
                Section(header: Text(MR.strings().onboarding_preferences_title.localized())) {
                    Toggle(isOn: Binding(
                        get: { viewModel.exportState.includeInstancePreferences },
                        set: { _ in viewModel.toggleIncludePreferences() }
                    )) {
                        Label(MR.strings().include_preferences.localized(), systemImage: "gearshape")
                    }
                    
                    Toggle(isOn: Binding(
                        get: { viewModel.exportState.includeTabPreferences },
                        set: { _ in viewModel.toggleIncludeTabPreferences() }
                    )) {
                        Label(MR.strings().navigation_bar_configuration.localized(), systemImage: "sidebar.left")
                    }
                    
                    Toggle(isOn: Binding(
                        get: { viewModel.exportState.includeUiPreferences },
                        set: { _ in viewModel.toggleIncludeUiPreferences() }
                    )) {
                        Label(MR.strings().user_interface.localized(), systemImage: "paintbrush")
                    }

                    Toggle(isOn: Binding(
                        get: { viewModel.exportState.includeIntegrationsPreferences },
                        set: { _ in viewModel.toggleIncludeIntegrationsPreferences() }
                    )) {
                        Label(MR.strings().integrations.localized(), systemImage: "link")
                    }
                }
                
                if !viewModel.exportState.instances.isEmpty {
                    Section(header: HStack {
                        Label(MR.strings().instances.localized(), systemImage: "server.rack")
                        Spacer()
                        Text("\(viewModel.exportState.selectedInstanceIds.count)/\(viewModel.exportState.instances.count)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }) {
                        ForEach(viewModel.exportState.instances, id: \.id) { instance in
                            Toggle(instance.label, isOn: Binding(
                                get: { viewModel.exportState.selectedInstanceIds.contains(instance.id.asKotlinLong) },
                                set: { _ in viewModel.toggleInstanceSelection(id: instance.id) }
                            ))
                        }
                    }
                }
                
                if !viewModel.exportState.downloadClients.isEmpty {
                    Section(header: HStack {
                        Label(MR.strings().download_clients.localized(), systemImage: "arrow.down.circle")
                        Spacer()
                        Text("\(viewModel.exportState.selectedDownloadClientIds.count)/\(viewModel.exportState.downloadClients.count)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }) {
                        ForEach(viewModel.exportState.downloadClients, id: \.id) { client in
                            Toggle(client.label, isOn: Binding(
                                get: { viewModel.exportState.selectedDownloadClientIds.contains(client.id.asKotlinLong) },
                                set: { _ in viewModel.toggleDownloadClientSelection(id: client.id) }
                            ))
                        }
                    }
                }

                if !viewModel.exportState.customWebpages.isEmpty {
                    Section(header: HStack {
                        Label(MR.strings().custom_webpages.localized(), systemImage: "globe")
                        Spacer()
                        Text("\(viewModel.exportState.selectedCustomWebpageIds.count)/\(viewModel.exportState.customWebpages.count)")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }) {
                        ForEach(viewModel.exportState.customWebpages, id: \.id) { webpage in
                            Toggle(webpage.name, isOn: Binding(
                                get: { viewModel.exportState.selectedCustomWebpageIds.contains(webpage.id.asKotlinLong) },
                                set: { _ in viewModel.toggleCustomWebpageSelection(id: webpage.id) }
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
                    .disabled(
                        viewModel.exportState.password.isEmpty ||
                        (
                            viewModel.exportState.selectedInstanceIds.isEmpty &&
                            viewModel.exportState.selectedDownloadClientIds.isEmpty &&
                            viewModel.exportState.selectedCustomWebpageIds.isEmpty &&
                            !viewModel.exportState.includeTabPreferences &&
                            !viewModel.exportState.includeUiPreferences &&
                            !viewModel.exportState.includeIntegrationsPreferences
                        )
                    )
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
                    Section(header: Text(MR.strings().password.localized()), footer: Text(MR.strings().import_password_prompt.localized())) {
                        SecureField(MR.strings().password.localized(), text: Binding(
                            get: { viewModel.importState.password },
                            set: { viewModel.setImportPassword(password: $0) }
                        ))
                        
                        if let error = viewModel.importState.error {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                    }
                } else if let backup = viewModel.importState.decryptedBackup {
                    let hasAnyPreferences = backup.globalPreferences != nil &&
                        (backup.globalPreferences?.tabPreferences != nil ||
                         backup.globalPreferences?.hasUiPreferences == true ||
                         backup.globalPreferences?.hasIntegrationsPreferences == true)
                    
                    if hasAnyPreferences {
                        Section(header: Label(MR.strings().onboarding_preferences_title.localized(), systemImage: "slider.horizontal.3")) {
                            if backup.globalPreferences?.tabPreferences != nil {
                                Toggle(isOn: Binding(
                                    get: { viewModel.importState.importTabPreferences },
                                    set: { _ in viewModel.toggleImportTabPreferences() }
                                )) {
                                    Label(MR.strings().navigation_bar_configuration.localized(), systemImage: "sidebar.left")
                                }
                            }
                            
                            if backup.globalPreferences?.hasUiPreferences == true {
                                Toggle(isOn: Binding(
                                    get: { viewModel.importState.importUiPreferences },
                                    set: { _ in viewModel.toggleImportUiPreferences() }
                                )) {
                                    Label(MR.strings().user_interface.localized(), systemImage: "paintbrush")
                                }
                            }

                            if backup.globalPreferences?.hasIntegrationsPreferences == true {
                                Toggle(isOn: Binding(
                                    get: { viewModel.importState.importIntegrationsPreferences },
                                    set: { _ in viewModel.toggleImportIntegrationsPreferences() }
                                )) {
                                    Label(MR.strings().integrations.localized(), systemImage: "link")
                                }
                            }
                        }
                    }

                    if !backup.instances.isEmpty {
                        Section(header: HStack {
                            Label(MR.strings().instances.localized(), systemImage: "server.rack")
                            Spacer()
                            Text("\(viewModel.importState.selectedInstanceIndices.count)/\(backup.instances.count)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }) {
                            ForEach(Array(backup.instances.enumerated()), id: \.offset) { index, instance in
                                Toggle(instance.label, isOn: Binding(
                                    get: { viewModel.importState.selectedInstanceIndices.contains(Int32(index).asKotlinInt) },
                                    set: { _ in viewModel.toggleImportInstanceSelection(index: Int32(index)) }
                                ))
                            }
                        }
                    }
                    
                    if !backup.downloadClients.isEmpty {
                        Section(header: HStack {
                            Label(MR.strings().download_clients.localized(), systemImage: "arrow.down.circle")
                            Spacer()
                            Text("\(viewModel.importState.selectedDownloadClientIndices.count)/\(backup.downloadClients.count)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }) {
                            ForEach(Array(backup.downloadClients.enumerated()), id: \.offset) { index, client in
                                Toggle(client.label, isOn: Binding(
                                    get: { viewModel.importState.selectedDownloadClientIndices.contains(Int32(index).asKotlinInt) },
                                    set: { _ in viewModel.toggleImportDownloadClientSelection(index: Int32(index)) }
                                ))
                            }
                        }
                    }

                    if !backup.customWebpages.isEmpty {
                        Section(header: HStack {
                            Label(MR.strings().custom_webpages.localized(), systemImage: "globe")
                            Spacer()
                            Text("\(viewModel.importState.selectedCustomWebpageIndices.count)/\(backup.customWebpages.count)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }) {
                            ForEach(Array(backup.customWebpages.enumerated()), id: \.offset) { index, webpage in
                                Toggle(webpage.name, isOn: Binding(
                                    get: { viewModel.importState.selectedCustomWebpageIndices.contains(Int32(index).asKotlinInt) },
                                    set: { _ in viewModel.toggleImportCustomWebpageSelection(index: Int32(index)) }
                                ))
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
                        let hasGlobalPrefToImport =
                            (viewModel.importState.decryptedBackup?.globalPreferences?.tabPreferences != nil && viewModel.importState.importTabPreferences) ||
                            (viewModel.importState.decryptedBackup?.globalPreferences?.hasUiPreferences == true && viewModel.importState.importUiPreferences) ||
                            (viewModel.importState.decryptedBackup?.globalPreferences?.hasIntegrationsPreferences == true && viewModel.importState.importIntegrationsPreferences)

                        Button(MR.strings().import_data.localized()) {
                            viewModel.executeImport {
                                onComplete()
                                isPresented = false
                            }
                        }
                        .disabled(
                            viewModel.importState.selectedInstanceIndices.isEmpty &&
                            viewModel.importState.selectedDownloadClientIndices.isEmpty &&
                            viewModel.importState.selectedCustomWebpageIndices.isEmpty &&
                            !hasGlobalPrefToImport
                        )
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
