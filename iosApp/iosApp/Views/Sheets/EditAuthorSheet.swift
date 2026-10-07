//
//  EditAuthorSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import SwiftUI
import Shared

struct EditAuthorSheet: View {
    let item: Author
    let qualityProfiles: [QualityProfile]
    let metadataProfiles: [MetadataProfile]
    let rootFolders: [RootFolder]
    let tags: [Tag]
    let editInProgress: Bool
    let onEditItem: (Author, Bool) -> Void
    
    private let isChaptarr: Bool
    @State private var chaptarrMediaType: BookMediaType
    
    // Audiobook state
    @State private var audiobookMonitorExisting: AuthorMonitorType
    @State private var audiobookMonitorFuture: Bool
    @State private var selectedAudiobookQualityProfileId: Int32?
    @State private var selectedAudiobookMetadataProfileId: Int32?
    @State private var selectedAudiobookRootFolder: String?
    @State private var selectedAudiobookTags: Set<Int>
    
    // Ebook state
    @State private var ebookMonitorExisting: AuthorMonitorType
    @State private var ebookMonitorFuture: Bool
    @State private var selectedEbookQualityProfileId: Int32?
    @State private var selectedEbookMetadataProfileId: Int32?
    @State private var selectedEbookRootFolder: String?
    @State private var selectedEbookTags: Set<Int>
    
    // Standard Readarr state
    @State private var monitored: Bool
    @State private var monitorNewItems: AuthorMonitorType
    @State private var qualityProfileId: Int32
    @State private var rootFolder: String?
    @State private var selectedTags: Set<Int>
    @State private var moveFiles: Bool = false
    
    private var canMove: Bool {
        rootFolder != item.rootFolderPath
    }
    
    private let statusOptions: [AuthorMonitorType] = [.all, .none, .future]
    private let chaptarrMonitorOptions: [AuthorMonitorType] = [
        .none, .all, .future, .missing, .existing, .firstBook, .latestBook
    ]
    
    init(
        item: Author,
        qualityProfiles: [QualityProfile],
        metadataProfiles: [MetadataProfile] = [],
        rootFolders: [RootFolder],
        tags: [Tag],
        editInProgress: Bool,
        onEditItem: @escaping (Author, Bool) -> Void
    ) {
        self.item = item
        self.qualityProfiles = qualityProfiles
        self.metadataProfiles = metadataProfiles
        self.rootFolders = rootFolders
        self.tags = tags
        self.editInProgress = editInProgress
        self.onEditItem = onEditItem
        
        let isChap = item.isChaptarr
        self.isChaptarr = isChap
        
        let initialTab: BookMediaType = (item.hasEbookConfigured && !item.hasAudiobookConfigured) ? .ebook : .audiobook
        self._chaptarrMediaType = State(initialValue: initialTab)
        
        let monitorOpts: [AuthorMonitorType] = [
            .none, .all, .future, .missing, .existing, .firstBook, .latestBook
        ]
        
        let abExistingIdx = item.audiobookMonitorExisting?.intValue ?? (item.monitored && item.hasAudiobookConfigured ? 1 : 0)
        let abExisting = (abExistingIdx >= 0 && abExistingIdx < monitorOpts.count) ? monitorOpts[Int(abExistingIdx)] : .none
        self._audiobookMonitorExisting = State(initialValue: abExisting)
        self._audiobookMonitorFuture = State(initialValue: item.audiobookMonitorFuture?.boolValue ?? false)
        
        let abQp = qualityProfiles.first(where: { $0.id == item.audiobookQualityProfileId?.int32Value })
            ?? qualityProfiles.first(where: { ($0.name ?? "").lowercased().contains("audio") })
            ?? qualityProfiles.first
        self._selectedAudiobookQualityProfileId = State(initialValue: abQp?.id)
        
        let abMp = metadataProfiles.first(where: { $0.id == item.audiobookMetadataProfileId?.int32Value })
            ?? metadataProfiles.first(where: { ($0.name ?? "").lowercased().contains("audio") })
            ?? metadataProfiles.first
        self._selectedAudiobookMetadataProfileId = State(initialValue: abMp?.id)
        
        let abRf = rootFolders.first(where: { $0.path == item.audiobookRootFolderPath })
            ?? rootFolders.first(where: { $0.path.lowercased().contains("audio") })
            ?? rootFolders.first
        self._selectedAudiobookRootFolder = State(initialValue: abRf?.path)
        
        let abTags = item.audiobookTags.isEmpty ? item.tags : item.audiobookTags
        self._selectedAudiobookTags = State(initialValue: Set(abTags.map(\.intValue)))
        
        let ebExistingIdx = item.ebookMonitorExisting?.intValue ?? (item.monitored && item.hasEbookConfigured ? 1 : 0)
        let ebExisting = (ebExistingIdx >= 0 && ebExistingIdx < monitorOpts.count) ? monitorOpts[Int(ebExistingIdx)] : .none
        self._ebookMonitorExisting = State(initialValue: ebExisting)
        self._ebookMonitorFuture = State(initialValue: item.ebookMonitorFuture?.boolValue ?? false)
        
        let ebQp = qualityProfiles.first(where: { $0.id == item.ebookQualityProfileId?.int32Value })
            ?? qualityProfiles.first(where: { ($0.name ?? "").lowercased().contains("ebook") })
            ?? qualityProfiles.first(where: { !($0.name ?? "").lowercased().contains("audio") })
            ?? qualityProfiles.first
        self._selectedEbookQualityProfileId = State(initialValue: ebQp?.id)
        
        let ebMp = metadataProfiles.first(where: { $0.id == item.ebookMetadataProfileId?.int32Value })
            ?? metadataProfiles.first(where: { ($0.name ?? "").lowercased().contains("ebook") })
            ?? metadataProfiles.first(where: { !($0.name ?? "").lowercased().contains("audio") })
            ?? metadataProfiles.first
        self._selectedEbookMetadataProfileId = State(initialValue: ebMp?.id)
        
        let ebRf = rootFolders.first(where: { $0.path == item.ebookRootFolderPath })
            ?? rootFolders.first(where: { !$0.path.lowercased().contains("audio") })
            ?? rootFolders.first
        self._selectedEbookRootFolder = State(initialValue: ebRf?.path)
        
        let ebTags = item.ebookTags.isEmpty ? item.tags : item.ebookTags
        self._selectedEbookTags = State(initialValue: Set(ebTags.map(\.intValue)))
        
        self._monitored = State(initialValue: item.monitored)
        self._monitorNewItems = State(initialValue: item.monitorNewItems)
        self._qualityProfileId = State(initialValue: item.qualityProfileId)
        self._rootFolder = State(initialValue: item.rootFolderPath)
        self._selectedTags = State(initialValue: Set(item.tags.map(\.intValue)))
    }
    
    var body: some View {
        NavigationStack {
            Form {
                if isChaptarr {
                    Section {
                        Picker("", selection: $chaptarrMediaType) {
                            Text(MR.strings().audiobooks.localized()).tag(BookMediaType.audiobook)
                            Text(MR.strings().ebooks.localized()).tag(BookMediaType.ebook)
                        }
                        .pickerStyle(.segmented)
                    }
                    
                    if chaptarrMediaType == .audiobook {
                        Section(header: Text(MR.strings().audiobooks.localized())) {
                            Picker(MR.strings().monitor_authors_audiobooks.localized(), selection: $audiobookMonitorExisting) {
                                ForEach(chaptarrMonitorOptions, id: \.self) { opt in
                                    Text(opt.resource.localized()).tag(opt)
                                }
                            }
                            
                            Toggle(MR.strings().monitor_new_audiobooks.localized(), isOn: $audiobookMonitorFuture)
                            
                            if let selectedAudiobookRootFolder {
                                Picker(MR.strings().audiobook_root_folder.localized(), selection: $selectedAudiobookRootFolder) {
                                    ForEach(rootFolders, id: \.id) { folder in
                                        Text("\(folder.path) (\(folder.freeSpace.bytesAsFileSizeString()))")
                                            .tag(folder.path as String?)
                                    }
                                }
                            }
                            
                            if !qualityProfiles.isEmpty, let selectedAudiobookQualityProfileId {
                                Picker(MR.strings().audiobook_quality_profile.localized(), selection: $selectedAudiobookQualityProfileId) {
                                    ForEach(qualityProfiles, id: \.id) { qp in
                                        Text(qp.name ?? "").tag(qp.id as Int32?)
                                    }
                                }
                            }
                            
                            if !metadataProfiles.isEmpty, let selectedAudiobookMetadataProfileId {
                                Picker(MR.strings().audiobook_metadata_profile.localized(), selection: $selectedAudiobookMetadataProfileId) {
                                    ForEach(metadataProfiles, id: \.id) { mp in
                                        Text(mp.name ?? "").tag(mp.id as Int32?)
                                    }
                                }
                            }
                            
                            if !tags.isEmpty {
                                NavigationLink {
                                    TagSelectionView(tags: tags, selectedTags: $selectedAudiobookTags)
                                } label: {
                                    LabeledContent(
                                        MR.strings().tags.localized(),
                                        value: MR.plurals().tag_count.localized(selectedAudiobookTags.count)
                                    )
                                }
                            }
                        }
                    } else {
                        Section(header: Text(MR.strings().ebooks.localized())) {
                            Picker(MR.strings().monitor_authors_ebooks.localized(), selection: $ebookMonitorExisting) {
                                ForEach(chaptarrMonitorOptions, id: \.self) { opt in
                                    Text(opt.resource.localized()).tag(opt)
                                }
                            }
                            
                            Toggle(MR.strings().monitor_new_ebooks.localized(), isOn: $ebookMonitorFuture)
                            
                            if let selectedEbookRootFolder {
                                Picker(MR.strings().ebook_root_folder.localized(), selection: $selectedEbookRootFolder) {
                                    ForEach(rootFolders, id: \.id) { folder in
                                        Text("\(folder.path) (\(folder.freeSpace.bytesAsFileSizeString()))")
                                            .tag(folder.path as String?)
                                    }
                                }
                            }
                            
                            if !qualityProfiles.isEmpty, let selectedEbookQualityProfileId {
                                Picker(MR.strings().ebook_quality_profile.localized(), selection: $selectedEbookQualityProfileId) {
                                    ForEach(qualityProfiles, id: \.id) { qp in
                                        Text(qp.name ?? "").tag(qp.id as Int32?)
                                    }
                                }
                            }
                            
                            if !metadataProfiles.isEmpty, let selectedEbookMetadataProfileId {
                                Picker(MR.strings().ebook_metadata_profile.localized(), selection: $selectedEbookMetadataProfileId) {
                                    ForEach(metadataProfiles, id: \.id) { mp in
                                        Text(mp.name ?? "").tag(mp.id as Int32?)
                                    }
                                }
                            }
                            
                            if !tags.isEmpty {
                                NavigationLink {
                                    TagSelectionView(tags: tags, selectedTags: $selectedEbookTags)
                                } label: {
                                    LabeledContent(
                                        MR.strings().tags.localized(),
                                        value: MR.plurals().tag_count.localized(selectedEbookTags.count)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Section {
                        Toggle(MR.strings().monitored.localized(), isOn: $monitored)
                        
                        Picker(MR.strings().quality_profile.localized(), selection: $qualityProfileId) {
                            ForEach(qualityProfiles, id: \.id) { qp in
                                Text(qp.name ?? "").tag(qp.id)
                            }
                        }
                        
                        Picker(MR.strings().monitor_new_books.localized(), selection: $monitorNewItems) {
                            ForEach(statusOptions, id: \.self) { status in
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
            }
            .toolbarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        if isChaptarr {
                            let abIdx = chaptarrMonitorOptions.firstIndex(of: audiobookMonitorExisting).map { KotlinInt(value: Int32($0)) }
                            let ebIdx = chaptarrMonitorOptions.firstIndex(of: ebookMonitorExisting).map { KotlinInt(value: Int32($0)) }
                            let combinedTags = Array(selectedAudiobookTags.union(selectedEbookTags).map { KotlinInt(value: Int32($0)) })
                            
                            let newAuthor = item.doCopyForChaptarrEdit(
                                audiobookQualityProfileId: selectedAudiobookQualityProfileId.map { KotlinInt(value: $0) },
                                audiobookMetadataProfileId: selectedAudiobookMetadataProfileId.map { KotlinInt(value: $0) },
                                audiobookRootFolderPath: selectedAudiobookRootFolder,
                                audiobookMonitorExisting: abIdx,
                                audiobookMonitorFuture: KotlinBoolean(value: audiobookMonitorFuture),
                                audiobookTags: Array(selectedAudiobookTags.map { KotlinInt(value: Int32($0)) }),
                                ebookQualityProfileId: selectedEbookQualityProfileId.map { KotlinInt(value: $0) },
                                ebookMetadataProfileId: selectedEbookMetadataProfileId.map { KotlinInt(value: $0) },
                                ebookRootFolderPath: selectedEbookRootFolder,
                                ebookMonitorExisting: ebIdx,
                                ebookMonitorFuture: KotlinBoolean(value: ebookMonitorFuture),
                                ebookTags: Array(selectedEbookTags.map { KotlinInt(value: Int32($0)) }),
                                tags: combinedTags
                            )
                            onEditItem(newAuthor, false)
                        } else {
                            let newAuthor = item.doCopyForEdit(
                                monitored: monitored,
                                monitorNew: monitorNewItems,
                                qualityProfileId: qualityProfileId,
                                rootFolderPath: rootFolder,
                                tags: Array(selectedTags.map { $0.asKotlinInt })
                            )
                            onEditItem(newAuthor, moveFiles && canMove)
                        }
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
