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
    let preferences: InstancePreferences
    let onUpdatePreferences: (InstancePreferences) -> Void
    let onAddItem: (SearchAudiobook, Bool) -> Void
    let onDismiss: () -> Void
    let instances: [Instance]
    let selectedInstance: Instance?
    let onInstanceSelected: (Instance) -> Void
    
    @State private var monitored: Bool
    @State private var selectedQualityProfileId: Int32?
    @State private var selectedRootFolderId: Int32?
    @State private var selectedRelativePath: String
    @State private var searchOnAdd: Bool
    
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
        preferences: InstancePreferences,
        onUpdatePreferences: @escaping (InstancePreferences) -> Void,
        onAddItem: @escaping (SearchAudiobook, Bool) -> Void,
        onDismiss: @escaping () -> Void,
        instances: [Instance] = [],
        selectedInstance: Instance? = nil,
        onInstanceSelected: @escaping (Instance) -> Void = { _ in }
    ) {
        self.audiobook = audiobook
        self.addItemStatus = addItemStatus
        self.qualityProfiles = qualityProfiles
        self.rootFolders = rootFolders
        self.relativePath = relativePath
        self.preferences = preferences
        self.onUpdatePreferences = onUpdatePreferences
        self.onAddItem = onAddItem
        self.onDismiss = onDismiss
        self.instances = instances
        self.selectedInstance = selectedInstance
        self.onInstanceSelected = onInstanceSelected
        
        self._monitored = State(initialValue: preferences.addAudiobookMonitored)
        self._searchOnAdd = State(initialValue: preferences.addSearchOnAdd)
        
        let qp = qualityProfiles.first(where: { $0.id == preferences.addQualityProfileId?.int32Value }) ?? qualityProfiles.first
        self._selectedQualityProfileId = State(initialValue: qp?.id)
        
        let rf = rootFolders.first(where: { $0.path == preferences.addRootFolderPath }) ?? rootFolders.first(where: { $0.isDefault }) ?? rootFolders.first
        self._selectedRootFolderId = State(initialValue: rf?.id)
        
        self._selectedRelativePath = State(initialValue: relativePath)
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
            if instances.count > 1, let selectedInstance = selectedInstance {
                Section {
                    Picker(MR.strings().instances.localized(), selection: Binding(
                        get: { selectedInstance },
                        set: { onInstanceSelected($0) }
                    )) {
                        ForEach(instances, id: \.id) { instance in
                            Text(instance.label).tag(instance)
                        }
                    }
                }
            }
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
                        onUpdatePreferences(
                            preferences.doCopyWithAudiobookAddDefaults(
                                monitored: monitored,
                                qualityProfileId: selectedQualityProfileId.asKotlinInt,
                                rootFolderPath: path,
                                searchOnAdd: searchOnAdd
                            )
                        )
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
