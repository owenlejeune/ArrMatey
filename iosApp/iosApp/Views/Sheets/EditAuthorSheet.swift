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
    private let isAudiobookConfiguredInitially: Bool
    private let isEbookConfiguredInitially: Bool
    @State private var chaptarrMediaType: BookMediaType
    @State private var audiobookEnabled: Bool
    @State private var ebookEnabled: Bool
    
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
        initialMediaType: BookMediaType? = nil,
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
        
        let abConfigured = item.hasAudiobookConfigured
        let ebConfigured = item.hasEbookConfigured
        self.isAudiobookConfiguredInitially = abConfigured
        self.isEbookConfiguredInitially = ebConfigured
        
        let initialTab: BookMediaType = initialMediaType ?? ((ebConfigured && !abConfigured) ? .ebook : .audiobook)
        self._chaptarrMediaType = State(initialValue: initialTab)
        
        let initialAbEnabled = (!abConfigured && !ebConfigured) ? true : abConfigured
        let initialEbEnabled = (!abConfigured && !ebConfigured) ? false : ebConfigured
        self._audiobookEnabled = State(initialValue: initialAbEnabled)
        self._ebookEnabled = State(initialValue: initialEbEnabled)
        
        let abExisting = item.audiobookMonitorExisting != nil ? AuthorMonitorType.companion.fromChaptarrIndex(index: item.audiobookMonitorExisting) : (item.monitored && item.hasAudiobookConfigured ? .all : .none)
        self._audiobookMonitorExisting = State(initialValue: abExisting)
        self._audiobookMonitorFuture = State(initialValue: item.audiobookMonitorFuture?.boolValue ?? false)
        
        let abQp = QualityProfileKt.defaultForAudiobook(qualityProfiles, preferredId: item.audiobookQualityProfileId)
        self._selectedAudiobookQualityProfileId = State(initialValue: abQp?.id)
        
        let abMp = MetadataProfileKt.defaultForAudiobook(metadataProfiles, preferredId: item.audiobookMetadataProfileId)
        self._selectedAudiobookMetadataProfileId = State(initialValue: abMp?.id)
        
        let abRf = RootFolderKt.defaultForAudiobook(rootFolders, preferredPath: item.audiobookRootFolderPath)
        self._selectedAudiobookRootFolder = State(initialValue: abRf?.path)
        
        let abTags = item.audiobookTags.isEmpty ? item.tags : item.audiobookTags
        self._selectedAudiobookTags = State(initialValue: Set(abTags.map(\.intValue)))
        
        let ebExisting = item.ebookMonitorExisting != nil ? AuthorMonitorType.companion.fromChaptarrIndex(index: item.ebookMonitorExisting) : (item.monitored && item.hasEbookConfigured ? .all : .none)
        self._ebookMonitorExisting = State(initialValue: ebExisting)
        self._ebookMonitorFuture = State(initialValue: item.ebookMonitorFuture?.boolValue ?? false)
        
        let ebQp = QualityProfileKt.defaultForEbook(qualityProfiles, preferredId: item.ebookQualityProfileId)
        self._selectedEbookQualityProfileId = State(initialValue: ebQp?.id)
        
        let ebMp = MetadataProfileKt.defaultForEbook(metadataProfiles, preferredId: item.ebookMetadataProfileId)
        self._selectedEbookMetadataProfileId = State(initialValue: ebMp?.id)
        
        let ebRf = RootFolderKt.defaultForEbook(rootFolders, preferredPath: item.ebookRootFolderPath)
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
                        if !audiobookEnabled {
                            Section {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(MR.strings().audiobooks_not_configured.localized())
                                        .font(.headline)
                                    Text(MR.strings().audiobooks_not_configured_desc.localized())
                                        .font(.subheadline)
                                        .foregroundStyle(.secondary)
                                    Button {
                                        audiobookEnabled = true
                                    } label: {
                                        Label(MR.strings().configure_audiobooks.localized(), systemImage: "plus")
                                            .frame(maxWidth: .infinity)
                                    }
                                    .buttonStyle(.borderedProminent)
                                    .padding(.top, 4)
                                }
                                .padding(.vertical, 4)
                            }
                        } else {
                            if !isAudiobookConfiguredInitially {
                                Section {
                                    HStack(spacing: 8) {
                                        Image(systemName: "info.circle.fill")
                                            .foregroundStyle(.blue)
                                        Text(MR.strings().saving_will_add_audiobook_support.localized())
                                            .font(.subheadline)
                                    }
                                }
                            }
                            
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
                                    let profilesToShow = QualityProfileKt.filterForAudiobook(qualityProfiles)
                                    Picker(MR.strings().audiobook_quality_profile.localized(), selection: $selectedAudiobookQualityProfileId) {
                                        ForEach(profilesToShow, id: \.id) { qp in
                                            Text(qp.name ?? "").tag(qp.id as Int32?)
                                        }
                                    }
                                }
                                
                                if !metadataProfiles.isEmpty, let selectedAudiobookMetadataProfileId {
                                    let profilesToShow = MetadataProfileKt.filterForAudiobook(metadataProfiles)
                                    Picker(MR.strings().audiobook_metadata_profile.localized(), selection: $selectedAudiobookMetadataProfileId) {
                                        ForEach(profilesToShow, id: \.id) { mp in
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
                            
                            if ebookEnabled {
                                Section {
                                    Button(role: .destructive) {
                                        audiobookEnabled = false
                                    } label: {
                                        Label(MR.strings().remove_audiobooks.localized(), systemImage: "trash")
                                    }
                                }
                            }
                        }
                    } else {
                        if !ebookEnabled {
                            Section {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(MR.strings().ebooks_not_configured.localized())
                                        .font(.headline)
                                    Text(MR.strings().ebooks_not_configured_desc.localized())
                                        .font(.subheadline)
                                        .foregroundStyle(.secondary)
                                    Button {
                                        ebookEnabled = true
                                    } label: {
                                        Label(MR.strings().configure_ebooks.localized(), systemImage: "plus")
                                            .frame(maxWidth: .infinity)
                                    }
                                    .buttonStyle(.borderedProminent)
                                    .padding(.top, 4)
                                }
                                .padding(.vertical, 4)
                            }
                        } else {
                            if !isEbookConfiguredInitially {
                                Section {
                                    HStack(spacing: 8) {
                                        Image(systemName: "info.circle.fill")
                                            .foregroundStyle(.blue)
                                        Text(MR.strings().saving_will_add_ebook_support.localized())
                                            .font(.subheadline)
                                    }
                                }
                            }
                            
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
                                    let profilesToShow = QualityProfileKt.filterForEbook(qualityProfiles)
                                    Picker(MR.strings().ebook_quality_profile.localized(), selection: $selectedEbookQualityProfileId) {
                                        ForEach(profilesToShow, id: \.id) { qp in
                                            Text(qp.name ?? "").tag(qp.id as Int32?)
                                        }
                                    }
                                }
                                
                                if !metadataProfiles.isEmpty, let selectedEbookMetadataProfileId {
                                    let profilesToShow = MetadataProfileKt.filterForEbook(metadataProfiles)
                                    Picker(MR.strings().ebook_metadata_profile.localized(), selection: $selectedEbookMetadataProfileId) {
                                        ForEach(profilesToShow, id: \.id) { mp in
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
                            
                            if audiobookEnabled {
                                Section {
                                    Button(role: .destructive) {
                                        ebookEnabled = false
                                    } label: {
                                        Label(MR.strings().remove_ebooks.localized(), systemImage: "trash")
                                    }
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
                            let abIdx = KotlinInt(value: AuthorMonitorType.companion.toChaptarrIndex(type: audiobookMonitorExisting))
                            let ebIdx = KotlinInt(value: AuthorMonitorType.companion.toChaptarrIndex(type: ebookMonitorExisting))
                            let activeAudioTags = audiobookEnabled ? selectedAudiobookTags : []
                            let activeEbookTags = ebookEnabled ? selectedEbookTags : []
                            let combinedTags = Array(activeAudioTags.union(activeEbookTags).map { KotlinInt(value: Int32($0)) })
                            
                            let newAuthor = item.doCopyForChaptarrEdit(
                                audiobookQualityProfileId: audiobookEnabled ? selectedAudiobookQualityProfileId.map { KotlinInt(value: $0) } : nil,
                                audiobookMetadataProfileId: audiobookEnabled ? selectedAudiobookMetadataProfileId.map { KotlinInt(value: $0) } : nil,
                                audiobookRootFolderPath: audiobookEnabled ? selectedAudiobookRootFolder : nil,
                                audiobookMonitorExisting: audiobookEnabled ? abIdx : nil,
                                audiobookMonitorFuture: audiobookEnabled ? KotlinBoolean(value: audiobookMonitorFuture) : nil,
                                audiobookTags: audiobookEnabled ? Array(selectedAudiobookTags.map { KotlinInt(value: Int32($0)) }) : [],
                                ebookQualityProfileId: ebookEnabled ? selectedEbookQualityProfileId.map { KotlinInt(value: $0) } : nil,
                                ebookMetadataProfileId: ebookEnabled ? selectedEbookMetadataProfileId.map { KotlinInt(value: $0) } : nil,
                                ebookRootFolderPath: ebookEnabled ? selectedEbookRootFolder : nil,
                                ebookMonitorExisting: ebookEnabled ? ebIdx : nil,
                                ebookMonitorFuture: ebookEnabled ? KotlinBoolean(value: ebookMonitorFuture) : nil,
                                ebookTags: ebookEnabled ? Array(selectedEbookTags.map { KotlinInt(value: Int32($0)) }) : [],
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
                    .disabled(editInProgress || (isChaptarr && !audiobookEnabled && !ebookEnabled))
                }
            }
        }
    }
}
