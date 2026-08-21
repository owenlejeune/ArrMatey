//
//  UnifiedMediaDetailsViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class UnifiedMediaDetailsViewModelS: ObservableObject {
    private let viewModel: UnifiedMediaDetailsViewModel
    
    @Published private(set) var uiState: UnifiedMediaDetailsUiState = UnifiedMediaDetailsUiStateInitial()
    @Published private(set) var buttonState: MediaButtonState = MediaButtonState()
    @Published private(set) var isMonitored: Bool = false
    @Published private(set) var isArrConfigured: Bool = false
    @Published private(set) var isSeerrConfigured: Bool = false
    @Published private(set) var activeInstance: Instance? = nil
    @Published private(set) var activeSeerrInstance: Instance? = nil
    
    @Published var isReportIssueSheetVisible: Bool = false
    @Published var isViewRequestSheetVisible: Bool = false
    @Published var isRequestSheetVisible: Bool = false
    @Published private(set) var isRequest4k: Bool = false
    @Published private(set) var reportIssueState: ReportIssueUiState = ReportIssueUiState()
    @Published private(set) var currentUser: SeerrUser? = nil
    @Published private(set) var users: [SeerrUser] = []
    @Published private(set) var serviceDetails: ServiceDetails? = nil
    
    @Published private(set) var qualityProfiles: [QualityProfile] = []
    @Published private(set) var rootFolders: [RootFolder] = []
    @Published private(set) var tags: [Tag] = []
    @Published private(set) var preferences: InstancePreferences = InstancePreferences()
    
    @Published private(set) var editStatus: NetworkingOperationStatus = NetworkingOperationStatusIdle()
    @Published private(set) var deleteStatus: NetworkingOperationStatus = NetworkingOperationStatusIdle()
    @Published private(set) var deleteSeasonStatus: NetworkingOperationStatus = NetworkingOperationStatusIdle()
    @Published private(set) var deleteAlbumStatus: NetworkingOperationStatus = NetworkingOperationStatusIdle()
    @Published private(set) var deleteMovieFileStatus: NetworkingOperationStatus = NetworkingOperationStatusIdle()
    @Published private(set) var removeQueueItemStatus: NetworkingOperationStatus = NetworkingOperationStatusIdle()
    
    @Published var editSuccessTrigger = false
    @Published var editErrorTrigger = false
    @Published var deleteSuccessTrigger = false
    @Published var deleteErrorTrigger = false
    
    @Published private(set) var automaticSearchIds: Set<Int64> = []
    @Published private(set) var lastSearchResult: Bool? = nil
    @Published private(set) var addSheetUiState: AddSheetUiState = AddSheetUiState()
    
    var resolvedInstanceType: InstanceType? {
        return viewModel.resolvedInstanceType
    }
    
    var resolvedRequestType: RequestType? {
        return viewModel.resolvedRequestType
    }
    
    init(
        arrId: Int64?,
        tmdbId: Int64?,
        tvdbId: Int64?,
        instanceType: InstanceType?,
        requestType: RequestType?
    ) {
        self.viewModel = KoinBridge.shared.getUnifiedMediaDetailsViewModel(
            arrId: arrId.map { KotlinLong(value: $0) },
            tmdbId: tmdbId.map { KotlinLong(value: $0) },
            tvdbId: tvdbId.map { KotlinLong(value: $0) },
            instanceType: instanceType,
            requestType: requestType
        )
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync(on: self, to: \.uiState)
        viewModel.buttonState.observeAsync(on: self, to: \.buttonState)
        viewModel.isMonitored.observeAsync(on: self) { owner, monitored in
            owner.isMonitored = monitored.boolValue
        }
        viewModel.isArrConfigured.observeAsync(on: self) { owner, configured in
            owner.isArrConfigured = configured.boolValue
        }
        viewModel.isSeerrConfigured.observeAsync(on: self) { owner, configured in
            owner.isSeerrConfigured = configured.boolValue
        }
        viewModel.activeInstance.observeAsync(on: self, to: \.activeInstance)
        viewModel.activeSeerrInstance.observeAsync(on: self, to: \.activeSeerrInstance)
        viewModel.isReportIssueSheetVisible.observeAsync(on: self) { owner, visible in
            owner.isReportIssueSheetVisible = visible.boolValue
        }
        viewModel.isViewRequestSheetVisible.observeAsync(on: self) { owner, visible in
            owner.isViewRequestSheetVisible = visible.boolValue
        }
        viewModel.isRequestSheetVisible.observeAsync(on: self) { owner, visible in
            owner.isRequestSheetVisible = visible.boolValue
        }
        viewModel.isRequest4k.observeAsync(on: self) { owner, visible in
            owner.isRequest4k = visible.boolValue
        }
        viewModel.reportIssueState.observeAsync(on: self, to: \.reportIssueState)
        viewModel.currentUser.observeAsync(on: self, to: \.currentUser)
        viewModel.users.observeAsync(on: self, to: \.users)
        viewModel.serviceDetails.observeAsync(on: self, to: \.serviceDetails)
        
        viewModel.qualityProfiles.observeAsync(on: self, to: \.qualityProfiles)
        viewModel.rootFolders.observeAsync(on: self, to: \.rootFolders)
        viewModel.tags.observeAsync(on: self, to: \.tags)
        viewModel.preferences.observeAsync(on: self, to: \.preferences)
        
        viewModel.editStatus.observeAsync(on: self) { owner, status in
            owner.editStatus = status
            if status is NetworkingOperationStatusSuccess {
                owner.editSuccessTrigger.toggle()
            } else if status is NetworkingOperationStatusError {
                owner.editErrorTrigger.toggle()
            }
        }
        viewModel.deleteStatus.observeAsync(on: self) { owner, status in
            owner.deleteStatus = status
            if status is NetworkingOperationStatusSuccess {
                owner.deleteSuccessTrigger.toggle()
            } else if status is NetworkingOperationStatusError {
                owner.deleteErrorTrigger.toggle()
            }
        }
        viewModel.deleteSeasonStatus.observeAsync(on: self, to: \.deleteSeasonStatus)
        viewModel.deleteAlbumStatus.observeAsync(on: self, to: \.deleteAlbumStatus)
        viewModel.deleteMovieFileStatus.observeAsync(on: self, to: \.deleteMovieFileStatus)
        viewModel.removeQueueItemStatus.observeAsync(on: self, to: \.removeQueueItemStatus)
        
        viewModel.automaticSearchIds.observeAsync(on: self) { owner, searchIds in
            owner.automaticSearchIds = Set(searchIds.map { ($0 as! KotlinLong).int64Value })
        }
        viewModel.lastSearchResult.observeAsync(on: self) { owner, result in
            owner.lastSearchResult = result?.boolValue
        }
        viewModel.addSheetUiState.observeAsync(on: self, to: \.addSheetUiState)
    }
    
    func refresh() {
        viewModel.refresh()
    }
    
    func performRefresh() {
        viewModel.performRefresh()
    }
    
    func performAutomaticLookup() {
        viewModel.performAutomaticLookup()
    }
    
    func toggleMonitored() {
        viewModel.toggleMonitored()
    }
    
    func setAddSheetTargetInstance(instance: Instance) {
        viewModel.setAddSheetTargetInstance(instance: instance)
    }
    
    func selectInstance(instanceId: Int64) {
        viewModel.selectInstance(instanceId: instanceId)
    }
    
    func updatePreferences(preferences: InstancePreferences) {
        viewModel.updatePreferences(preferences: preferences)
    }
    

    func smartAdd(item: ArrMedia, searchOnAdd: Bool = false, targetInstanceId: Int64? = nil) {
        viewModel.smartAdd(
            item: item,
            searchOnAdd: searchOnAdd,
            targetInstanceId: targetInstanceId.map { KotlinLong(value: $0) }
        )
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
    
    func editItem(item: ArrMedia, moveFiles: Bool = false) {
        viewModel.editItem(item: item, moveFiles: moveFiles)
    }
    
    func updateAlbum(album: ArrAlbum) {
        viewModel.updateAlbum(album: album)
    }
    
    func deleteMedia(deleteFiles: Bool, addImportExclusion: Bool) {
        viewModel.deleteMedia(deleteFiles: deleteFiles, addImportExclusion: addImportExclusion)
    }
    
    func deleteSeasonFiles(seasonNumber: Int32) {
        viewModel.deleteSeasonFiles(seasonNumber: seasonNumber)
    }
    
    func deleteAlbumFiles(albumId: Int64) {
        viewModel.deleteAlbumFiles(albumId: albumId)
    }
    
    func deleteMovieFile() {
        viewModel.deleteMovieFile()
    }
    
    func toggleSeasonMonitored(seasonNumber: Int32) {
        viewModel.toggleSeasonMonitored(seasonNumber: seasonNumber)
    }
    
    func toggleEpisodeMonitored(episode: Episode) {
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
    
    func setIssueType(issueType: IssueType) {
        viewModel.setIssueType(issueType: issueType)
    }
    
    func setIssueMessage(message: String) {
        viewModel.setIssueMessage(message: message)
    }
    
    func setProblemSeason(season: Int32?) {
        viewModel.setProblemSeason(season: season.map { KotlinInt(value: $0) })
    }
    
    func setProblemEpisode(episode: Int32?) {
        viewModel.setProblemEpisode(episode: episode.map { KotlinInt(value: $0) })
    }
    
    func resetIssueState() {
        viewModel.resetIssueState()
    }
    
    func submitIssue() {
        viewModel.submitIssue()
    }
    
    func showReportIssueSheet() {
        viewModel.showReportIssueSheet()
    }
    
    func hideReportIssueSheet() {
        viewModel.hideReportIssueSheet()
    }
    
    func showViewRequestSheet() {
        viewModel.showViewRequestSheet()
    }
    
    func hideViewRequestSheet() {
        viewModel.hideViewRequestSheet()
    }
    
    func showRequestSheet(is4k: Bool = false) {
        viewModel.showRequestSheet(is4k: is4k)
    }
    
    func hideRequestSheet() {
        viewModel.hideRequestSheet()
    }
    
    func submitRequest(
        profileId: Int64?,
        rootFolder: String?,
        languageProfileId: Int64?,
        seasons: [KotlinInt]?,
        is4k: Bool = false,
        userId: Int64?
    ) {
        viewModel.submitRequest(
            profileId: profileId.map { KotlinLong(value: $0) },
            rootFolder: rootFolder,
            languageProfileId: languageProfileId.map { KotlinLong(value: $0) },
            seasons: seasons,
            is4k: is4k,
            userId: userId.map { KotlinLong(value: $0) }
        )
    }
    
    func cancelRequest(requestId: Int64) {
        viewModel.cancelRequest(requestId: requestId)
    }
    
    func declineRequest(requestId: Int64) {
        viewModel.declineRequest(requestId: requestId)
    }
    
    func approveRequest(
        requestId: Int64,
        profileId: Int64?,
        rootFolder: String?,
        languageProfileId: Int64?,
        seasons: [KotlinInt]?
    ) {
        viewModel.approveRequest(
            requestId: requestId,
            profileId: profileId.map { KotlinLong(value: $0) },
            rootFolder: rootFolder,
            languageProfileId: languageProfileId.map { KotlinLong(value: $0) },
            seasons: seasons
        )
    }
    
    func removeQueueItem(
        item: QueueItem,
        removeFromClient: Bool,
        addToBlocklist: Bool,
        skipRedownload: Bool
    ) {
        viewModel.removeQueueItem(
            queueItem: item,
            removeFromClient: removeFromClient,
            addToBlocklist: addToBlocklist,
            skipRedownload: skipRedownload
        )
    }
    
    func deleteSeerrMediaFile(is4k: Bool = false) {
        viewModel.deleteSeerrMediaFile(is4k: is4k)
    }
    
    func clearSeerrMediaData() {
        viewModel.clearSeerrMediaData()
    }
    
    func markSeerrMediaAsAvailable(is4k: Bool = false) {
        viewModel.markSeerrMediaAsAvailable(is4k: is4k)
    }
}
