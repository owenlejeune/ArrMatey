//
//  ArrMediaDetailsViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-20.
//

import Shared
import SwiftUI

@MainActor
class ArrMediaDetailsViewModelS: ObservableObject {
    private let viewModel: ArrMediaDetailsViewModel
    
    @Published private(set) var uiState: MediaDetailsUiState = MediaDetailsUiStateInitial()
    @Published private(set) var history: [HistoryItem] = []
    @Published private(set) var monitorStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var editItemStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var editItemSucceeded: Bool = false
    @Published private(set) var editInProgress: Bool = false
    @Published private(set) var isMonitored: Bool = false
    @Published private(set) var automaticSearchIds: Set<Int64> = Set()
    @Published private(set) var lastSearchResult: Bool? = nil
    
    @Published private(set) var deleteStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var deleteSucceeded: Bool = false
    @Published private(set) var deleteInProgress: Bool = false
    
    @Published private(set) var deleteSeasonStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var deleteSeasonSucceeded: Bool = false
    @Published private(set) var deleteSeasonInProgress: Bool = false
    
    @Published private(set) var deleteAlbumStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var deleteAlbumSucceeded: Bool = false
    @Published private(set) var deleteAlbumInProgress: Bool = false
    
    @Published private(set) var qualityProfiles: [QualityProfile] = []
    @Published private(set) var rootFolders: [RootFolder] = []
    @Published private(set) var tags: [Tag] = []
    
    @Published private(set) var item: ArrMedia? = nil
    
    init(id: Int64, type: InstanceType) {
        self.viewModel = KoinBridge.shared.getArrMediaDetailsViewModel(id: id, type: type)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync {
            self.uiState = $0
            if let success = $0 as? MediaDetailsUiStateSuccess {
                self.item = success.item
            }
        }
        viewModel.history.observeAsync { self.history = $0 }
        viewModel.monitorStatus.observeAsync { self.monitorStatus = $0 }
        viewModel.editItemStatus.observeAsync {
            self.editItemStatus = $0
            self.editItemSucceeded = $0 is OperationStatusSuccess
            self.editInProgress = $0 is OperationStatusInProgress
        }
        viewModel.isMonitored.observeAsync { self.isMonitored = $0.boolValue }
        viewModel.automaticSearchIds.observeAsync { self.automaticSearchIds = Set($0.map { $0.int64Value }) }
        viewModel.lastSearchResult.observeAsync { self.lastSearchResult = $0?.boolValue }
        viewModel.deleteStatus.observeAsync {
            self.deleteStatus = $0
            self.deleteSucceeded = $0 is OperationStatusSuccess
            self.deleteInProgress = $0 is OperationStatusInProgress
        }
        viewModel.deleteSeasonStatus.observeAsync {
            self.deleteSeasonStatus = $0
            self.deleteSeasonSucceeded = $0 is OperationStatusSuccess
            self.deleteSeasonInProgress = $0 is OperationStatusInProgress
        }
        viewModel.deleteAlbumStatus.observeAsync {
            self.deleteAlbumStatus = $0
            self.deleteAlbumSucceeded = $0 is OperationStatusSuccess
            self.deleteAlbumInProgress = $0 is OperationStatusInProgress
        }
        
        viewModel.qualityProfiles.observeAsync { self.qualityProfiles = $0 }
        viewModel.rootFolders.observeAsync { self.rootFolders = $0 }
        viewModel.tags.observeAsync { self.tags = $0 }
    }
    
    func refreshDetails() {
        viewModel.refreshDetails()
    }
    
    func updateItem(_ item: ArrMedia) {
        viewModel.updateItem(item: item)
    }
    
    func editItem(_ item: ArrMedia, moveFiles: Bool = false) {
        viewModel.editItem(item: item, moveFiles: moveFiles)
    }
    
    func toggleMonitor() {
        viewModel.toggleMonitored()
    }
    
    func toggleSeasonMonitor(seasonNumber: Int32) {
        viewModel.toggleSeasonMonitored(seasonNumber: seasonNumber)
    }
    
    func toggleEpisodeMonitor(episode: Episode) {
        viewModel.toggleEpisodeMonitored(episode: episode)
    }
    
    func toggleAlbumMonitored(album: ArrAlbum) {
        viewModel.toggleAlbumMonitored(album: album)
    }
    
    func toggleBookMonitored(book: Book) {
        viewModel.toggleBookMonitored(book: book)
    }
    
    func toggleBookSeriesMonitored(books: [Book]) {
        viewModel.toggleBookSeriesMonitored(books: books)
    }
    
    func deleteMedia(deleteFiles: Bool, addImportExclusion: Bool) {
        viewModel.deleteMedia(deleteFiles: deleteFiles, addImportExclusion: addImportExclusion)
    }
    
    func deleteSeasonFiles(_ seasonNumber: Int32) {
        viewModel.deleteSeasonFiles(seasonNumber: seasonNumber)
    }
    
    func deleteAlbumFiles(_ albumId: Int64) {
        viewModel.deleteAlbumFiles(albumId: albumId)
    }
    
    func performRefresh() {
        viewModel.performRefresh()
    }
    
    func performAutomaticLookup() {
        viewModel.performAutomaticLookup()
    }
    
    func performEpisodeAutomaticLookup(episodeId: Int64) {
        viewModel.performEpisodeAutomaticLookup(episodeId: episodeId)
    }
    
    func performSeasonAutomaticLookup(seasonNumber: Int32) {
        viewModel.performSeasonAutomaticLookup(seasonNumber: seasonNumber)
    }
    
    func performAlbumAutomaticLookup(albumId: Int64) {
        viewModel.performAlbumAutomaticLookup(albumId: albumId)
    }
    
    func performBookAutomaticLookup(bookId: Int64) {
        viewModel.performBookAutomaticLookup(bookId: bookId)
    }
    
    func delete(_ addExclusion: Bool, _ deleteFiles: Bool) {
        viewModel.deleteMedia(deleteFiles: deleteFiles, addImportExclusion: addExclusion)
    }
}
