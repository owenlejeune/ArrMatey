//
//  AddSeriesForm.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-31.
//

import Shared
import SwiftUI

struct AddSeriesForm: View {
    let series: ArrSeries
    let addItemStatus: OperationStatus
    let qualityProfiles: [QualityProfile]
    let rootFolders: [RootFolder]
    let tags: [Tag]
    let onAddItem: (ArrMedia, Bool) -> Void
    let onDismiss: () -> Void
    
    
    @State private var monitorType: SeriesMonitorType = .all
    @State private var selectedQualityProfileId: Int32? = nil
    @State private var selectedSeriesType: SeriesType = .standard
    @State private var useSeasonFolders: Bool = true
    @State private var selectedRootFolderId: Int32? = nil
    @State private var selectedTags: Set<Int> = Set()
    @State private var searchOnAdd: Bool = false
    
    private let selectableMonitorTypes: [SeriesMonitorType] = SeriesMonitorType.allCases.filter {
        $0 != .unknown && $0 != .latestSeason && $0 != .skip
    }
    
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
                Picker(MR.strings().monitor.localized(), selection: $monitorType) {
                    ForEach(selectableMonitorTypes, id: \.self) { type in
                        Text(type.resource.localized()).tag(type)
                    }
                }
                
                Toggle(MR.strings().season_folders.localized(), isOn: $useSeasonFolders)
            }
            
            Section {
                Picker(MR.strings().quality_profile.localized(), selection: $selectedQualityProfileId) {
                    ForEach(qualityProfiles, id: \.self) { qualityProfile in
                        if let name = qualityProfile.name {
                            Text(name).tag(qualityProfile.id)
                        }
                    }
                }
                
                Picker(MR.strings().series_type.localized(), selection: $selectedSeriesType) {
                    ForEach(SeriesType.allCases, id: \.self) { seriesType in
                        Text(seriesType.resource.localized()).tag(seriesType)
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
                Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolderId) {
                    ForEach(rootFolders, id: \.self) { rootFolder in
                        Text("\(rootFolder.path) (\(rootFolder.freeSpaceString))")
                            .tag(rootFolder.id)
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
                        let newSeries = series.doCopyForCreation(
                            monitor: monitorType,
                            qualityProfileId: profileId,
                            seriesType: selectedSeriesType,
                            seasonFolder: useSeasonFolders,
                            rootFolderPath: path,
                            tags: Array(selectedTags.map { $0.asKotlinInt })
                        )
                        onAddItem(newSeries, searchOnAdd)
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
