//
//  ArrArtistForm.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-01.
//

import Shared
import SwiftUI

struct AddArtistForm: View {
    let artist: Arrtist
    let addItemStatus: OperationStatus
    let qualityProfiles: [QualityProfile]
    let rootFolders: [RootFolder]
    let tags: [Tag]
    let onAddItem: (Arrtist, Bool) -> Void
    let onDismiss: () -> Void
    
    @State private var monitor: ArtistMonitorType = .all
    @State private var monitorNew: ArtistMonitorType = .none
    @State private var selectedQualityProfileId: Int32? = nil
    @State private var selectedRootFolderId: Int32? = nil
    @State private var selectedTags: Set<Int> = Set()
    @State private var searchOnAdd: Bool = false
    
    private let selectedStatuses: [ArtistMonitorType] = [.all, .none, .future]
    
    private var selectedRootFolderPath: String? {
        rootFolders.first { $0.id == selectedRootFolderId }?.path
    }
    
    private var isLoading: Bool {
        addItemStatus is OperationStatusInProgress
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
                        selectedRootFolderId = rootFolders[0].id
                    }
                }
        }
    }
    
    @ViewBuilder
    private var content: some View {
        Form {
            Section {
                Picker(MR.strings().monitor.localized(), selection: $monitor) {
                    ForEach(selectedStatuses, id: \.self) { status in
                        Text(status.resource.localized()).tag(status)
                    }
                }
                
                if selectedQualityProfileId != nil {
                    Picker(MR.strings().quality_profile.localized(), selection: $selectedQualityProfileId) {
                        ForEach(qualityProfiles, id: \.self) { qualityProfile in
                            if let name = qualityProfile.name {
                                Text(name)
                                    .tag(qualityProfile.id)
                            }
                        }
                    }
                }
                
                Picker(MR.strings().monitor_new_albums.localized(), selection: $monitorNew) {
                    ForEach(selectedStatuses, id: \.self) { status in
                        Text(status.resource.localized()).tag(status)
                    }
                }
                
                if tags.count > 0 {
                    NavigationLink {
                        TagSelectionView(tags: tags, selectedTags: $selectedTags)
                    } label: {
                        LabeledContent(
                            MR.strings().tags.localized(),
                            value: MR.plurals().tag_count.localized(selectedTags.count)
                        )
                    }
                }
                
                
                Toggle(MR.strings().search_on_add_label.localized(), isOn: $searchOnAdd)
            }
            
            Section {
                if selectedRootFolderId != nil {
                    Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolderId) {
                        ForEach(rootFolders, id: \.self) { rootFolder in
                            Text("\(rootFolder.path) (\(rootFolder.freeSpaceString))")
                                .tag(rootFolder.id)
                        }
                    }
                }
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
                    if let profileId = selectedQualityProfileId, let path = selectedRootFolderPath {
                        let newArtist = artist.doCopyForCreation(
                            monitor: monitor,
                            monitorNew: monitorNew,
                            qualityProfileId: profileId,
                            rootFolderPath: path,
                            tags: Array(selectedTags.map { $0.asKotlinInt })
                        )
                        onAddItem(newArtist, searchOnAdd)
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
