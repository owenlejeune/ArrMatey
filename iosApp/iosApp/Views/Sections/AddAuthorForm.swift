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
    let metadataProfiles: [MetadataProfile]
    let rootFolders: [RootFolder]
    let tags: [Tag]
    let preferences: InstancePreferences
    let onUpdatePreferences: (InstancePreferences) -> Void
    let onAddItem: (Author, Bool) -> Void
    let onDismiss: () -> Void
    let instances: [Instance]
    let selectedInstance: Instance?
    let onInstanceSelected: (Instance) -> Void
    
    // Chaptarr state
    @State private var chaptarrMediaType: BookMediaType
    @State private var selectedAudiobookRootFolderId: Int32?
    @State private var selectedAudiobookQualityProfileId: Int32?
    @State private var selectedAudiobookMetadataProfileId: Int32?
    @State private var audiobookMonitor: AuthorMonitorType
    @State private var audiobookMonitorNew: Bool
    
    @State private var selectedEbookRootFolderId: Int32?
    @State private var selectedEbookQualityProfileId: Int32?
    @State private var selectedEbookMetadataProfileId: Int32?
    @State private var ebookMonitor: AuthorMonitorType
    @State private var ebookMonitorNew: Bool
    
    // Standard Readarr state
    @State private var monitor: AuthorMonitorType
    @State private var monitorNewBooks: AuthorMonitorType
    @State private var selectedQualityProfileId: Int32?
    @State private var selectedRootFolderId: Int32?
    @State private var selectedTags: Set<Int> = Set()
    @State private var searchOnAdd: Bool
    
    init(
        author: Author,
        addItemStatus: OperationStatus,
        qualityProfiles: [QualityProfile],
        metadataProfiles: [MetadataProfile] = [],
        rootFolders: [RootFolder],
        tags: [Tag],
        preferences: InstancePreferences,
        onUpdatePreferences: @escaping (InstancePreferences) -> Void,
        onAddItem: @escaping (Author, Bool) -> Void,
        onDismiss: @escaping () -> Void,
        instances: [Instance] = [],
        selectedInstance: Instance? = nil,
        onInstanceSelected: @escaping (Instance) -> Void = { _ in }
    ) {
        self.author = author
        self.addItemStatus = addItemStatus
        self.qualityProfiles = qualityProfiles
        self.metadataProfiles = metadataProfiles
        self.rootFolders = rootFolders
        self.tags = tags
        self.preferences = preferences
        self.onUpdatePreferences = onUpdatePreferences
        self.onAddItem = onAddItem
        self.onDismiss = onDismiss
        self.instances = instances
        self.selectedInstance = selectedInstance
        self.onInstanceSelected = onInstanceSelected
        
        self._chaptarrMediaType = State(initialValue: preferences.addChaptarrMediaType)
        self._audiobookMonitor = State(initialValue: preferences.addChaptarrAudiobookMonitorExisting)
        self._audiobookMonitorNew = State(initialValue: preferences.addChaptarrAudiobookMonitorFuture)
        self._ebookMonitor = State(initialValue: preferences.addChaptarrEbookMonitorExisting)
        self._ebookMonitorNew = State(initialValue: preferences.addChaptarrEbookMonitorFuture)
        
        let abRf = rootFolders.first(where: { $0.path == preferences.addChaptarrAudiobookRootFolderPath })
            ?? rootFolders.first(where: { $0.path.lowercased().contains("audio") })
            ?? rootFolders.first
        self._selectedAudiobookRootFolderId = State(initialValue: abRf?.id)
        
        let abQp = qualityProfiles.first(where: { $0.id == preferences.addChaptarrAudiobookQualityProfileId?.int32Value })
            ?? qualityProfiles.first(where: { ($0.name ?? "").lowercased().contains("audio") })
            ?? qualityProfiles.first
        self._selectedAudiobookQualityProfileId = State(initialValue: abQp?.id)
        
        let abMp = metadataProfiles.first(where: { $0.id == preferences.addChaptarrAudiobookMetadataProfileId?.int32Value })
            ?? metadataProfiles.first(where: { ($0.name ?? "").lowercased().contains("audio") })
            ?? metadataProfiles.first
        self._selectedAudiobookMetadataProfileId = State(initialValue: abMp?.id)
        
        let ebRf = rootFolders.first(where: { $0.path == preferences.addChaptarrEbookRootFolderPath })
            ?? rootFolders.first(where: { !$0.path.lowercased().contains("audio") })
            ?? rootFolders.first
        self._selectedEbookRootFolderId = State(initialValue: ebRf?.id)
        
        let ebQp = qualityProfiles.first(where: { $0.id == preferences.addChaptarrEbookQualityProfileId?.int32Value })
            ?? qualityProfiles.first(where: { ($0.name ?? "").lowercased().contains("ebook") })
            ?? qualityProfiles.first(where: { !($0.name ?? "").lowercased().contains("audio") })
            ?? qualityProfiles.first
        self._selectedEbookQualityProfileId = State(initialValue: ebQp?.id)
        
        let ebMp = metadataProfiles.first(where: { $0.id == preferences.addChaptarrEbookMetadataProfileId?.int32Value })
            ?? metadataProfiles.first(where: { ($0.name ?? "").lowercased().contains("ebook") })
            ?? metadataProfiles.first(where: { !($0.name ?? "").lowercased().contains("audio") })
            ?? metadataProfiles.first
        self._selectedEbookMetadataProfileId = State(initialValue: ebMp?.id)
        
        self._monitor = State(initialValue: preferences.addAuthorMonitor)
        self._monitorNewBooks = State(initialValue: preferences.addAuthorMonitorNew)
        self._searchOnAdd = State(initialValue: preferences.addSearchOnAdd)
        
        let qp = qualityProfiles.first(where: { $0.id == preferences.addQualityProfileId?.int32Value }) ?? qualityProfiles.first
        self._selectedQualityProfileId = State(initialValue: qp?.id)
        
        let rf = rootFolders.first(where: { $0.path == preferences.addRootFolderPath }) ?? rootFolders.first
        self._selectedRootFolderId = State(initialValue: rf?.id)
    }
    
    private var isChaptarr: Bool {
        (selectedInstance?.type ?? author.instanceType) == .chaptarr
    }
    
    private let chaptarrMonitorOptions: [AuthorMonitorType] = [
        .none, .all, .future, .missing, .existing, .firstBook, .latestBook
    ]
    
    private let selectedStatuses: [AuthorMonitorType] = [.all, .none, .future]
    
    private var selectedRootFolderPath: String? {
        rootFolders.first { $0.id == selectedRootFolderId }?.path
    }
    
    private var selectedAudiobookRootFolderPath: String? {
        rootFolders.first { $0.id == selectedAudiobookRootFolderId }?.path
    }
    
    private var selectedEbookRootFolderPath: String? {
        rootFolders.first { $0.id == selectedEbookRootFolderId }?.path
    }
    
    private var isLoading: Bool {
        addItemStatus is OperationStatusInProgress
    }
    
    private var saveButtonTitle: String {
        if isChaptarr {
            switch chaptarrMediaType {
            case .audiobook: return MR.strings().add_audiobooks.localized()
            case .ebook: return MR.strings().add_ebooks.localized()
            case .both: return MR.strings().add_audiobooks_ebooks.localized()
            default: return MR.strings().save.localized()
            }
        } else {
            return MR.strings().save.localized()
        }
    }
    
    var body: some View {
        NavigationStack {
            content
                .toolbar {
                    toolbarButtons
                }
                .onChange(of: qualityProfiles, initial: true) {
                    if !qualityProfiles.isEmpty {
                        if selectedQualityProfileId == nil {
                            selectedQualityProfileId = qualityProfiles[0].id
                        }
                        if selectedAudiobookQualityProfileId == nil {
                            selectedAudiobookQualityProfileId = qualityProfiles.first(where: { ($0.name ?? "").lowercased().contains("audio") })?.id ?? qualityProfiles[0].id
                        }
                        if selectedEbookQualityProfileId == nil {
                            selectedEbookQualityProfileId = qualityProfiles.first(where: { ($0.name ?? "").lowercased().contains("ebook") || ($0.name ?? "").lowercased().contains("book") })?.id ?? qualityProfiles[0].id
                        }
                    }
                }
                .onChange(of: rootFolders, initial: true) {
                    if !rootFolders.isEmpty {
                        if selectedRootFolderId == nil {
                            selectedRootFolderId = rootFolders[0].id
                        }
                        if selectedAudiobookRootFolderId == nil {
                            selectedAudiobookRootFolderId = rootFolders.first(where: { $0.path.lowercased().contains("audio") })?.id ?? rootFolders[0].id
                        }
                        if selectedEbookRootFolderId == nil {
                            selectedEbookRootFolderId = rootFolders.first(where: { !$0.path.lowercased().contains("audio") })?.id ?? rootFolders[0].id
                        }
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
            
            if isChaptarr {
                chaptarrContent
            } else {
                standardContent
            }
        }
    }
    
    @ViewBuilder
    private var chaptarrContent: some View {
        Group {
            Section {
                Picker("", selection: $chaptarrMediaType) {
                    Text(MR.strings().audiobooks.localized()).tag(BookMediaType.audiobook)
                    Text(MR.strings().both.localized()).tag(BookMediaType.both)
                    Text(MR.strings().ebooks.localized()).tag(BookMediaType.ebook)
                }
                .pickerStyle(.segmented)
                
                Button {
                    onUpdatePreferences(preferences.doCopyWithChaptarrMediaType(mediaType: chaptarrMediaType))
                } label: {
                    Text(MR.strings().set_as_default.localized())
                        .font(.caption)
                }
            }
            
            if chaptarrMediaType == .audiobook || chaptarrMediaType == .both {
                Section(header: Text(MR.strings().audiobooks.localized())) {
                    if selectedAudiobookRootFolderId != nil {
                        Picker(MR.strings().audiobook_root_folder.localized(), selection: $selectedAudiobookRootFolderId) {
                            ForEach(rootFolders, id: \.self) { rootFolder in
                                Text("\(rootFolder.path) (\(rootFolder.freeSpaceString))")
                                    .tag(rootFolder.id as Int32?)
                            }
                        }
                    }
                    
                    Picker(MR.strings().monitor_authors_audiobooks.localized(), selection: $audiobookMonitor) {
                        ForEach(chaptarrMonitorOptions, id: \.self) { status in
                            Text(status.resource.localized()).tag(status)
                        }
                    }
                    
                    Toggle(MR.strings().monitor_new_audiobooks.localized(), isOn: $audiobookMonitorNew)
                    
                    if selectedAudiobookQualityProfileId != nil {
                        Picker(MR.strings().audiobook_quality_profile.localized(), selection: $selectedAudiobookQualityProfileId) {
                            ForEach(qualityProfiles, id: \.self) { qualityProfile in
                                if let name = qualityProfile.name {
                                    Text(name).tag(qualityProfile.id as Int32?)
                                }
                            }
                        }
                    }
                    
                    if !metadataProfiles.isEmpty {
                        Picker(MR.strings().audiobook_metadata_profile.localized(), selection: $selectedAudiobookMetadataProfileId) {
                            ForEach(metadataProfiles, id: \.self) { metadataProfile in
                                if let name = metadataProfile.name {
                                    Text(name).tag(metadataProfile.id as Int32?)
                                }
                            }
                        }
                    }
                }
            }
            
            if tags.count > 0 {
                Section {
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
            
            if chaptarrMediaType == .ebook || chaptarrMediaType == .both {
                Section(header: Text(MR.strings().ebooks.localized())) {
                    if selectedEbookRootFolderId != nil {
                        Picker(MR.strings().ebook_root_folder.localized(), selection: $selectedEbookRootFolderId) {
                            ForEach(rootFolders, id: \.self) { rootFolder in
                                Text("\(rootFolder.path) (\(rootFolder.freeSpaceString))")
                                    .tag(rootFolder.id as Int32?)
                            }
                        }
                    }
                    
                    Picker(MR.strings().monitor_authors_ebooks.localized(), selection: $ebookMonitor) {
                        ForEach(chaptarrMonitorOptions, id: \.self) { status in
                            Text(status.resource.localized()).tag(status)
                        }
                    }
                    
                    Toggle(MR.strings().monitor_new_ebooks.localized(), isOn: $ebookMonitorNew)
                    
                    if selectedEbookQualityProfileId != nil {
                        Picker(MR.strings().ebook_quality_profile.localized(), selection: $selectedEbookQualityProfileId) {
                            ForEach(qualityProfiles, id: \.self) { qualityProfile in
                                if let name = qualityProfile.name {
                                    Text(name).tag(qualityProfile.id as Int32?)
                                }
                            }
                        }
                    }
                    
                    if !metadataProfiles.isEmpty {
                        Picker(MR.strings().ebook_metadata_profile.localized(), selection: $selectedEbookMetadataProfileId) {
                            ForEach(metadataProfiles, id: \.self) { metadataProfile in
                                if let name = metadataProfile.name {
                                    Text(name).tag(metadataProfile.id as Int32?)
                                }
                            }
                        }
                    }
                }
            }
            
            Section {
                Toggle(MR.strings().start_search_for_missing_books.localized(), isOn: $searchOnAdd)
            }
        }
    }
    
    @ViewBuilder
    private var standardContent: some View {
        Group {
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
                                Text(name).tag(qualityProfile.id as Int32?)
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
                                .tag(rootFolder.id as Int32?)
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
                    if isChaptarr {
                        onUpdatePreferences(
                            preferences.doCopyWithChaptarrAddDefaults(
                                mediaType: chaptarrMediaType,
                                audiobookQualityProfileId: selectedAudiobookQualityProfileId?.asKotlinInt,
                                audiobookMetadataProfileId: selectedAudiobookMetadataProfileId?.asKotlinInt,
                                audiobookRootFolderPath: selectedAudiobookRootFolderPath,
                                audiobookMonitorExisting: audiobookMonitor,
                                audiobookMonitorFuture: audiobookMonitorNew,
                                ebookQualityProfileId: selectedEbookQualityProfileId?.asKotlinInt,
                                ebookMetadataProfileId: selectedEbookMetadataProfileId?.asKotlinInt,
                                ebookRootFolderPath: selectedEbookRootFolderPath,
                                ebookMonitorExisting: ebookMonitor,
                                ebookMonitorFuture: ebookMonitorNew,
                                searchOnAdd: searchOnAdd
                            )
                        )
                        
                        let abMonitorIdx = chaptarrMonitorOptions.firstIndex(of: audiobookMonitor) ?? 0
                        let ebMonitorIdx = chaptarrMonitorOptions.firstIndex(of: ebookMonitor) ?? 0
                        
                        let newAuthor = author.doCopyForChaptarrCreation(
                            selectedMediaType: chaptarrMediaType,
                            audiobookQualityProfileId: selectedAudiobookQualityProfileId?.asKotlinInt,
                            audiobookMetadataProfileId: selectedAudiobookMetadataProfileId?.asKotlinInt,
                            audiobookRootFolderPath: selectedAudiobookRootFolderPath,
                            audiobookMonitorExisting: Int32(abMonitorIdx).asKotlinInt,
                            audiobookMonitorFuture: KotlinBoolean(value: audiobookMonitorNew),
                            audiobookTags: Array(selectedTags.map { $0.asKotlinInt }),
                            ebookQualityProfileId: selectedEbookQualityProfileId?.asKotlinInt,
                            ebookMetadataProfileId: selectedEbookMetadataProfileId?.asKotlinInt,
                            ebookRootFolderPath: selectedEbookRootFolderPath,
                            ebookMonitorExisting: Int32(ebMonitorIdx).asKotlinInt,
                            ebookMonitorFuture: KotlinBoolean(value: ebookMonitorNew),
                            ebookTags: Array(selectedTags.map { $0.asKotlinInt }),
                            tags: Array(selectedTags.map { $0.asKotlinInt }),
                            searchForMissingBooks: searchOnAdd
                        )
                        onAddItem(newAuthor, searchOnAdd)
                    } else {
                        if let profileId = selectedQualityProfileId, let path = selectedRootFolderPath {
                            onUpdatePreferences(
                                preferences.doCopyWithAuthorAddDefaults(
                                    monitor: monitor,
                                    monitorNew: monitorNewBooks,
                                    qualityProfileId: Int32(profileId).asKotlinInt,
                                    rootFolderPath: path,
                                    searchOnAdd: searchOnAdd
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
                }
            } label: {
                if (isLoading) {
                    ProgressView().tint(nil)
                } else {
                    Label(saveButtonTitle, systemImage: "checkmark")
                }
            }
            .disabled(isLoading)
        }
    }
}
