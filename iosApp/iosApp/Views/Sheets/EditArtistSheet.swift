//
//  EditArtistSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-01.
//

import SwiftUI
import Shared

struct EditArtistSheet: View {
    let item: Arrtist
    let qualityProfiles: [QualityProfile]
    let rootFolders: [RootFolder]
    let tags: [Tag]
    let editInProgress: Bool
    let onEditItem: (Arrtist, Bool) -> Void
    
    @State private var monitored: Bool
    @State private var monitorNewAlbums: ArtistMonitorType
    @State private var qualityProfileId: Int32
    @State private var rootFolder: String?
    @State private var selectedTags: Set<Int>
    
    @State private var moveFiles: Bool = false
    
    private var canMove: Bool {
        rootFolder != item.rootFolderPath
    }
    
    private let statusOptions: [ArtistMonitorType] = [.all, .none, .future]
    
    init(item: Arrtist, qualityProfiles: [QualityProfile], rootFolders: [RootFolder], tags: [Tag], editInProgress: Bool, onEditItem: @escaping (Arrtist, Bool) -> Void) {
        self.item = item
        self.qualityProfiles = qualityProfiles
        self.rootFolders = rootFolders
        self.tags = tags
        self.editInProgress = editInProgress
        self.onEditItem = onEditItem
    
        self.monitored = item.monitored
        self.monitorNewAlbums = item.monitorNewItems
        self.qualityProfileId = item.qualityProfileId
        self.rootFolder = item.rootFolderPath
        self.selectedTags = Set(item.tags.map(\.intValue))
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Toggle(MR.strings().monitored.localized(), isOn: $monitored)
                    
                    Picker(MR.strings().quality_profile.localized(), selection: $qualityProfileId) {
                        ForEach(qualityProfiles, id: \.id) { qp in
                            Text(qp.name ?? "").tag(qp.id)
                        }
                    }
                    
                    Picker(MR.strings().monitor_new_albums.localized(), selection: $monitorNewAlbums) {
                        ForEach(statusOptions, id: \.self) { status in
                            Text(status.name).tag(status)
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
                }
                
                Section {
                    if rootFolders.count > 1 {
                        Picker(MR.strings().root_folder.localized(), selection: $rootFolder) {
                            ForEach(rootFolders, id: \.id) { folder in
                                Text("\(folder.path) (\(folder.freeSpace.bytesAsFileSizeString()))")
                                    .tag(folder.path)
                            }
                        }
                        if canMove {
                            Toggle(MR.strings().move_files.localized(), isOn: $moveFiles)
                        }
                    }
                } footer: {
                    if canMove {
                        Text(MR.strings().move_files_description.localized())
                    }
                }
            }
            .toolbarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        let newArtist = item.doCopyForEdit(
                            monitored: monitored,
                            monitorNew: monitorNewAlbums,
                            qualityProfileId: qualityProfileId,
                            rootFolderPath: rootFolder,
                            tags: Array(selectedTags.map { $0.asKotlinInt })
                        )
                        onEditItem(newArtist, moveFiles && canMove)
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
                }
            }
        }
    }
    
}
