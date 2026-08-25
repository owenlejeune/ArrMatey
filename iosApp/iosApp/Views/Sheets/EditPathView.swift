//
//  EditPathView.swift
//  iosApp
//
//

import SwiftUI
import Shared

struct EditPathView: View {
    let item: ArrMedia
    let rootFolders: [RootFolder]
    let onEdit: (ArrMedia, Bool) -> Void
    @Environment(\.dismiss) private var dismiss
    
    @State private var selectedRootFolder: RootFolder
    @State private var moveFiles = false
    
    private let initialRootFolder: RootFolder?
    
    private var isPathChanged: Bool {
        selectedRootFolder.path != initialRootFolder?.path
    }
    
    init(item: ArrMedia, rootFolders: [RootFolder], onEdit: @escaping (ArrMedia, Bool) -> Void) {
        self.item = item
        self.rootFolders = rootFolders
        self.onEdit = onEdit
        
        let initialRF = item.findCurrentRoot(rootFolders: rootFolders)
        let effectiveRF = initialRF ?? rootFolders.first
        
        self.initialRootFolder = initialRF
        self._selectedRootFolder = State(initialValue: effectiveRF ?? rootFolders.first!)
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section(MR.strings().edit_path.localized()) {
                    Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolder) {
                        ForEach(rootFolders, id: \.id) { folder in
                            Text("\(folder.path) (\(folder.freeSpace.bytesAsFileSizeString()))")
                                .tag(folder)
                        }
                    }
                }
                
                Section {
                    Toggle(MR.strings().move_files.localized(), isOn: $moveFiles)
                        .disabled(!isPathChanged)
                }
            }
            .navigationTitle(MR.strings().edit_path.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(MR.strings().save.localized()) {
                        save()
                    }
                    .disabled(!isPathChanged)
                }
            }
        }
    }
    
    private func save() {
        let rf = selectedRootFolder.path
        let updatedItem = item.withNewRoot(rootFolderPath: rf, currentRootFolderPath: initialRootFolder?.path)
        onEdit(updatedItem, moveFiles)
        dismiss()
    }
}
