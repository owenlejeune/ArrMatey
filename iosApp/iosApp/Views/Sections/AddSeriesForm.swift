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
    let preferences: InstancePreferences
    let onUpdatePreferences: (InstancePreferences) -> Void
    let onAddItem: (ArrMedia, Bool) -> Void
    let onDismiss: () -> Void
    let instances: [Instance]
    let selectedInstance: Instance?
    let onInstanceSelected: (Instance) -> Void
    
    
    @State private var monitorType: SeriesMonitorType
    @State private var selectedQualityProfileId: Int32?
    @State private var selectedSeriesType: SeriesType
    @State private var useSeasonFolders: Bool
    @State private var selectedRootFolderId: Int32?
    @State private var selectedTags: Set<Int> = Set()
    @State private var searchOnAdd: Bool
    
    init(
        series: ArrSeries,
        addItemStatus: OperationStatus,
        qualityProfiles: [QualityProfile],
        rootFolders: [RootFolder],
        tags: [Tag],
        preferences: InstancePreferences,
        onUpdatePreferences: @escaping (InstancePreferences) -> Void,
        onAddItem: @escaping (ArrMedia, Bool) -> Void,
        onDismiss: @escaping () -> Void,
        instances: [Instance] = [],
        selectedInstance: Instance? = nil,
        onInstanceSelected: @escaping (Instance) -> Void = { _ in }
    ) {
        self.series = series
        self.addItemStatus = addItemStatus
        self.qualityProfiles = qualityProfiles
        self.rootFolders = rootFolders
        self.tags = tags
        self.preferences = preferences
        self.onUpdatePreferences = onUpdatePreferences
        self.onAddItem = onAddItem
        self.onDismiss = onDismiss
        self.instances = instances
        self.selectedInstance = selectedInstance
        self.onInstanceSelected = onInstanceSelected
        
        self._monitorType = State(initialValue: preferences.addSeriesMonitor)
        self._selectedSeriesType = State(initialValue: preferences.addSeriesType)
        self._useSeasonFolders = State(initialValue: preferences.addSeriesSeasonFolder)
        self._searchOnAdd = State(initialValue: preferences.addSearchOnAdd)
        
        let qp = qualityProfiles.first(where: { $0.id == preferences.addQualityProfileId?.int32Value }) ?? qualityProfiles.first
        self._selectedQualityProfileId = State(initialValue: qp?.id)
        
        let rf = rootFolders.first(where: { $0.path == preferences.addRootFolderPath }) ?? rootFolders.first
        self._selectedRootFolderId = State(initialValue: rf?.id)
    }
    
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
                        onUpdatePreferences(
                            preferences.doCopyWithSeriesAddDefaults(
                                monitor: monitorType,
                                type: selectedSeriesType,
                                seasonFolder: useSeasonFolders,
                                qualityProfileId: Int32(profileId).asKotlinInt,
                                rootFolderPath: path,
                                searchOnAdd: searchOnAdd
                            )
                        )
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
