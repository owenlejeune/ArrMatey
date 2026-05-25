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
        viewModel.uiState.observeAsync(on: self) { owner, state in
            owner.uiState = state
            if let success = state as? MediaDetailsUiStateSuccess {
                owner.item = success.item
            }
        }
        viewModel.history.observeAsync(on: self, to: \.history)
        viewModel.monitorStatus.observeAsync(on: self, to: \.monitorStatus)
        viewModel.editItemStatus.observeAsync(on: self) { owner, status in
            owner.editItemStatus = status
            owner.editItemSucceeded = status is OperationStatusSuccess
            owner.editInProgress = status is OperationStatusInProgress
        }
        viewModel.isMonitored.observeAsync(on: self) { owner, monitored in
            owner.isMonitored = monitored.boolValue
        }
        viewModel.automaticSearchIds.observeAsync(on: self) { owner, ids in
            owner.automaticSearchIds = Set(ids.map { $0.int64Value })
        }
        viewModel.lastSearchResult.observeAsync(on: self) { owner, result in
            owner.lastSearchResult = result?.boolValue
        }
        viewModel.deleteStatus.observeAsync(on: self) { owner, status in
            owner.deleteStatus = status
            owner.deleteSucceeded = status is OperationStatusSuccess
            owner.deleteInProgress = status is OperationStatusInProgress
        }
        viewModel.deleteSeasonStatus.observeAsync(on: self) { owner, status in
            owner.deleteSeasonStatus = status
            owner.deleteSeasonSucceeded = status is OperationStatusSuccess
            owner.deleteSeasonInProgress = status is OperationStatusInProgress
        }
        viewModel.deleteAlbumStatus.observeAsync(on: self) { owner, status in
            owner.deleteAlbumStatus = status
            owner.deleteAlbumSucceeded = status is OperationStatusSuccess
            owner.deleteAlbumInProgress = status is OperationStatusInProgress
        }
        
        viewModel.qualityProfiles.observeAsync(on: self, to: \.qualityProfiles)
        viewModel.rootFolders.observeAsync(on: self, to: \.rootFolders)
        viewModel.tags.observeAsync(on: self, to: \.tags)
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
    
    func deleteMovieFile() {
        viewModel.deleteMovieFile()
    }
}
