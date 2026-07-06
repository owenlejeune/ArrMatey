//
//  AddAuthorForm.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-04.
//

import SwiftUI
import Shared

struct AddAuthorForm: View {
    let author: Author
    let addItemStatus: OperationStatus
    let qualityProfiles: [QualityProfile]
    let rootFolders: [RootFolder]
    let tags: [Tag]
    let preferences: InstancePreferences
    let onUpdatePreferences: (InstancePreferences) -> Void
    let onAddItem: (Author, Bool) -> Void
    let onDismiss: () -> Void
    
    @State private var monitor: AuthorMonitorType
    @State private var monitorNewBooks: AuthorMonitorType
    @State private var selectedQualityProfileId: Int32?
    @State private var selectedRootFolderId: Int32?
    @State private var selectedTags: Set<Int> = Set()
    @State private var searchOnAdd: Bool
    
    init(author: Author, addItemStatus: OperationStatus, qualityProfiles: [QualityProfile], rootFolders: [RootFolder], tags: [Tag], preferences: InstancePreferences, onUpdatePreferences: @escaping (InstancePreferences) -> Void, onAddItem: @escaping (Author, Bool) -> Void, onDismiss: @escaping () -> Void) {
        self.author = author
        self.addItemStatus = addItemStatus
        self.qualityProfiles = qualityProfiles
        self.rootFolders = rootFolders
        self.tags = tags
        self.preferences = preferences
        self.onUpdatePreferences = onUpdatePreferences
        self.onAddItem = onAddItem
        self.onDismiss = onDismiss
        
        self._monitor = State(initialValue: preferences.addAuthorMonitor)
        self._monitorNewBooks = State(initialValue: preferences.addAuthorMonitorNew)
        self._searchOnAdd = State(initialValue: preferences.addSearchOnAdd)
        
        let qp = qualityProfiles.first(where: { $0.id == preferences.addQualityProfileId?.int32Value }) ?? qualityProfiles.first
        self._selectedQualityProfileId = State(initialValue: qp?.id)
        
        let rf = rootFolders.first(where: { $0.path == preferences.addRootFolderPath }) ?? rootFolders.first
        self._selectedRootFolderId = State(initialValue: rf?.id)
    }
    
    private let selectedStatuses: [AuthorMonitorType] = [.all, .none, .future]
    
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
                    ForEach(AuthorMonitorType.allCases, id: \.self) { status in
                        Text(status.resource.localized()).tag(status)
                    }
                }
                
                Picker(MR.strings().monitor_new_books.localized(), selection: $monitorNewBooks) {
                    ForEach(selectedStatuses, id: \.self) { status in
                        Text(status.resource.localized()).tag(status)
                    }
                }
                
                if selectedQualityProfileId != nil {
                    Picker(MR.strings().quality_profile.localized(), selection: $selectedQualityProfileId) {
                        ForEach(qualityProfiles, id: \.self) { qualityProfile in
                            if let name = qualityProfile.name {
                                Text(name).tag(qualityProfile.id)
                            }
                        }
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
                        onUpdatePreferences(
                            preferences.doCopy(
                                sortBy: preferences.sortBy,
                                sortOrder: preferences.sortOrder,
                                filterBy: preferences.filterBy,
                                viewType: preferences.viewType,
                                posterElevation: preferences.posterElevation,
                                posterRadius: preferences.posterRadius,
                                showFullDetails: preferences.showFullDetails,
                                showOverlay: preferences.showOverlay,
                                gridDensity: preferences.gridDensity,
                                gridSpacing: preferences.gridSpacing,
                                showBannerBackground: preferences.showBannerBackground,
                                includeOverview: preferences.includeOverview,
                                bannerBlur: preferences.bannerBlur,
                                applyGlobally: preferences.applyGlobally,
                                addQualityProfileId: Int32(profileId).asKotlinInt,
                                addRootFolderPath: path,
                                addSearchOnAdd: searchOnAdd,
                                addSeriesMonitor: preferences.addSeriesMonitor,
                                addSeriesType: preferences.addSeriesType,
                                addSeriesSeasonFolder: preferences.addSeriesSeasonFolder,
                                addMovieMonitored: preferences.addMovieMonitored,
                                addMovieMinimumAvailability: preferences.addMovieMinimumAvailability,
                                addArtistMonitor: preferences.addArtistMonitor,
                                addArtistMonitorNew: preferences.addArtistMonitorNew,
                                addAuthorMonitor: monitor,
                                addAuthorMonitorNew: monitorNewBooks,
                                addAudiobookMonitored: preferences.addAudiobookMonitored
                            )
                        )
                        let newAuthor = author.doCopyForCreation(
                            monitor: monitor,
                            monitorNew: monitorNewBooks,
                            qualityProfileId: profileId,
                            rootFolderPath: path,
                            tags: Array(selectedTags.map { $0.asKotlinInt })
                        )
                        onAddItem(newAuthor, searchOnAdd)
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
