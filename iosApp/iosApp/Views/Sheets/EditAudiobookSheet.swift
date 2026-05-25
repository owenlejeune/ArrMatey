//
//  EditAudiobookSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import SwiftUI
import Shared

struct EditAudiobookSheet: View {
    let item: Audiobook
    let qualityProfiles: [QualityProfile]
    let rootFolders: [RootFolder]
    let editInProgress: Bool
    let onEditItem: (Audiobook) -> Void
    
    @Environment(\.dismiss) var dismiss
    
    @State private var monitored: Bool
    @State private var selectedQualityProfileId: Int32
    @State private var selectedRootFolderPath: String
    @State private var relativePath: String
    
    init(item: Audiobook, qualityProfiles: [QualityProfile], rootFolders: [RootFolder], editInProgress: Bool, onEditItem: @escaping (Audiobook) -> Void) {
        self.item = item
        self.qualityProfiles = qualityProfiles
        self.rootFolders = rootFolders
        self.editInProgress = editInProgress
        self.onEditItem = onEditItem
        
        self._monitored = State(initialValue: item.monitored)
        self._selectedQualityProfileId = State(initialValue: Int32(item.qualityProfileId))
        
        let folder = rootFolders.first(where: { item.path?.hasPrefix($0.path) == true }) ?? rootFolders.first!
        self._selectedRootFolderPath = State(initialValue: folder.path)
        self._relativePath = State(initialValue: item.path?.replacingOccurrences(of: folder.path, with: "").trimmingCharacters(in: .init(charactersIn: "/")) ?? "")
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Toggle(MR.strings().monitored.localized(), isOn: $monitored)
                    
                    Picker(MR.strings().quality_profile.localized(), selection: $selectedQualityProfileId) {
                        ForEach(qualityProfiles, id: \.id) { qp in
                            Text(qp.name ?? "").tag(qp.id)
                        }
                    }
                }
                
                Section {
                    if rootFolders.count > 1 {
                        Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolderPath) {
                            ForEach(rootFolders, id: \.id) { folder in
                                Text("\(folder.path) (\(folder.freeSpace.bytesAsFileSizeString()))").tag(folder.path)
                            }
                        }
                    }
                    
                    TextField(MR.strings().relative_path.localized(), text: $relativePath)
                }
            }
            .toolbarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button {
                        dismiss()
                    } label: {
                        Label(MR.strings().close.localized(), systemImage: "xmark")
                            .foregroundStyle(.white)
                    }
                }
                
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        let updatedItem = item.doCopyForEdit(
                            monitored: monitored,
                            qualityProfileId: selectedQualityProfileId,
                            rootFolderPath: selectedRootFolderPath,
                            relativePath: relativePath
                        )
                        onEditItem(updatedItem)
                    } label: {
                        if editInProgress {
                            ProgressView()
                                .progressViewStyle(.circular)
                        } else {
                            Label(MR.strings().save.localized(), systemImage: "checkmark")
                                .foregroundStyle(.white)
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.primary)
                    .disabled(editInProgress)
                }
            }
        }
    }
}
