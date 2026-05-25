//
//  AddAudiobookForm.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import SwiftUI
import Shared

struct AddAudiobookForm: View {
    let audiobook: SearchAudiobook
    let addItemStatus: OperationStatus
    let qualityProfiles: [QualityProfile]
    let rootFolders: [RootFolder]
    let relativePath: String
    let onAddItem: (SearchAudiobook, Bool) -> Void
    let onDismiss: () -> Void
    
    @State private var monitored: Bool = true
    @State private var selectedQualityProfileId: Int32? = nil
    @State private var selectedRootFolderId: Int32? = nil
    @State private var selectedRelativePath: String
    @State private var searchOnAdd: Bool = false
    
    private var selectedRootFolderPath: String? {
        rootFolders.first { $0.id == selectedRootFolderId }?.path
    }
    
    private var isLoading: Bool {
        addItemStatus is OperationStatusInProgress
    }
    
    init(
        audiobook: SearchAudiobook,
        addItemStatus: OperationStatus,
        qualityProfiles: [QualityProfile],
        rootFolders: [RootFolder],
        relativePath: String,
        onAddItem: @escaping (SearchAudiobook, Bool) -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.audiobook = audiobook
        self.addItemStatus = addItemStatus
        self.qualityProfiles = qualityProfiles
        self.rootFolders = rootFolders
        self.relativePath = relativePath
        self.onAddItem = onAddItem
        self.onDismiss = onDismiss
        
        _selectedRelativePath = State(initialValue: relativePath)
    }
    
    var body: some View {
        NavigationStack {
            content
                .toolbar {
                    toolbarButtons
                }
                .onChange(of: qualityProfiles, initial: true) {
                    if !qualityProfiles.isEmpty && selectedQualityProfileId == nil {
                        selectedQualityProfileId = qualityProfiles[0].id
                    }
                }
                .onChange(of: rootFolders, initial: true) {
                    if !rootFolders.isEmpty && selectedRootFolderId == nil {
                        selectedRootFolderId = rootFolders.first(where: { $0.isDefault })?.id ?? rootFolders[0].id
                    }
                }
        }
    }
    
    @ViewBuilder
    private var content: some View {
        Form {
            Section {
                Toggle(MR.strings().monitored.localized(), isOn: $monitored)
                
                if selectedQualityProfileId != nil {
                    Picker(MR.strings().quality_profile.localized(), selection: $selectedQualityProfileId) {
                        ForEach(qualityProfiles, id: \.self) { qualityProfile in
                            if let name = qualityProfile.name {
                                Text(name).tag(qualityProfile.id)
                            }
                        }
                    }
                }
                
                Toggle(MR.strings().search_on_add_label.localized(), isOn: $searchOnAdd)
            }
            
            Section {
                if selectedRootFolderId != nil {
                    Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolderId) {
                        ForEach(rootFolders, id: \.self) { rootFolder in
                            Text("\(rootFolder.path)\(rootFolder.isDefault ? " (\(MR.strings().default_label.localized()))" : "")")
                                .tag(rootFolder.id)
                        }
                    }
                }
                
                TextField(MR.strings().relative_path.localized(), text: $selectedRelativePath)
            }
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarButtons: some ToolbarContent {
        ToolbarItem(placement: .cancellationAction) {
            Button {
                onDismiss()
            } label: {
                Label(MR.strings().cancel.localized(), systemImage: "xmark")
            }
            .tint(.primary)
        }
        
        ToolbarItem(placement: .primaryAction) {
            Button {
                Task {
                    if let path = selectedRootFolderPath {
                        let newAudiobook = audiobook.doCopyForCreation(
                            monitored: monitored,
                            qualityProfileId: selectedQualityProfileId ?? 0,
                            rootFolderPath: path,
                            relativePath: selectedRelativePath
                        )
                        onAddItem(newAudiobook, searchOnAdd)
                    }
                }
            } label: {
                if (isLoading) {
                    ProgressView().tint(nil)
                } else {
                    Label(MR.strings().save.localized(), systemImage: "checkmark")
                }
            }
            .disabled(isLoading)
        }
    }
}
