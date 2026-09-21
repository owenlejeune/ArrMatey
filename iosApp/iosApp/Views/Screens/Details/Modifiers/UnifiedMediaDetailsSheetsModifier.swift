//
//  UnifiedMediaDetailsSheetsModifier.swift
//  iosApp
//

import SwiftUI
import Shared

@MainActor
struct UnifiedMediaDetailsSheetsModifier: ViewModifier {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    @Binding var showEditSheet: Bool
    @Binding var showEditPathSheet: Bool
    @Binding var showAddSheet: Bool
    @Binding var showConfirmSheet: Bool
    @Binding var moveFilesItem: ArrMedia?
    @Binding var editAlbum: ArrAlbum?
    @Binding var selectedQueueItem: QueueItem?
    @Binding var selectedTracearrSession: TracearrStreamSession?

    func body(content: Content) -> some View {
        let requestSheetBinding = SwiftUI.Binding<Bool>(
            get: { viewModel.isRequestSheetVisible },
            set: { if !$0 { viewModel.hideRequestSheet() } }
        )
        let reportIssueSheetBinding = SwiftUI.Binding<Bool>(
            get: { viewModel.isReportIssueSheetVisible },
            set: { if !$0 { viewModel.hideReportIssueSheet() } }
        )
        let viewRequestSheetBinding = SwiftUI.Binding<Bool>(
            get: { viewModel.isViewRequestSheetVisible },
            set: { if !$0 { viewModel.hideViewRequestSheet() } }
        )
        let queueItemBinding = SwiftUI.Binding<IdentifiableQueueItem?>(
            get: { selectedQueueItem.map { IdentifiableQueueItem(item: $0) } },
            set: { selectedQueueItem = $0?.item }
        )

        content
            .sheet(isPresented: $showEditSheet) {
                if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess {
                    EditMediaSheetsHostView(
                        success: success,
                        viewModel: viewModel,
                        showEditPathSheet: false,
                        showEditSheet: true,
                        onDismissEditPath: { showEditPathSheet = false },
                        onDismissEditMedia: { showEditSheet = false },
                        onRequestMoveFiles: { moveFilesItem = $0 }
                    )
                }
            }
            .sheet(isPresented: $showEditPathSheet) {
                if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess {
                    EditMediaSheetsHostView(
                        success: success,
                        viewModel: viewModel,
                        showEditPathSheet: true,
                        showEditSheet: false,
                        onDismissEditPath: { showEditPathSheet = false },
                        onDismissEditMedia: { showEditSheet = false },
                        onRequestMoveFiles: { moveFilesItem = $0 }
                    )
                }
            }
            .sheet(isPresented: $showAddSheet) {
                if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess {
                    AddMediaSheetsHostView(
                        success: success,
                        viewModel: viewModel,
                        onDismiss: { showAddSheet = false }
                    )
                }
            }
            .sheet(isPresented: $showConfirmSheet) {
                DeleteMediaSheet(
                    isLoading: viewModel.deleteStatus is OperationStatusInProgress,
                    initialAddExclusion: viewModel.preferences.deleteAddExclusion,
                    initialDeleteFiles: viewModel.preferences.deleteDeleteFiles,
                    onConfirm: { addExclusion, deleteFiles in
                        viewModel.deleteMedia(deleteFiles: deleteFiles, addImportExclusion: addExclusion)
                    }
                )
            }
            .sheet(item: $editAlbum) { album in
                EditAlbumSheet(
                    album: album,
                    editInProgress: viewModel.editStatus is OperationStatusInProgress,
                    onEditAlbum: { updatedAlbum in
                        viewModel.updateAlbum(album: updatedAlbum)
                    }
                )
            }
            .sheet(isPresented: requestSheetBinding) {
                if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess, let seerrMedia = success.seerrMedia {
                    SeerrRequestSheet(
                        details: seerrMedia,
                        serviceDetails: viewModel.serviceDetails,
                        currentUser: viewModel.currentUser,
                        users: viewModel.users,
                        is4k: viewModel.isRequest4k,
                        onDismiss: { viewModel.hideRequestSheet() },
                        onSubmit: { profileId, rootFolder, langId, seasons, is4k, userId in
                            viewModel.submitRequest(
                                profileId: profileId,
                                rootFolder: rootFolder,
                                languageProfileId: langId,
                                seasons: seasons,
                                is4k: is4k,
                                userId: userId
                            )
                        }
                    )
                }
            }
            .sheet(isPresented: reportIssueSheetBinding) {
                SeerrReportIssueSheet(
                    viewModel: viewModel,
                    onDismiss: { viewModel.hideReportIssueSheet() }
                )
            }
            .sheet(isPresented: viewRequestSheetBinding) {
                if let success = viewModel.uiState as? UnifiedMediaDetailsUiStateSuccess, let seerrMedia = success.seerrMedia {
                    SeerrViewRequestSheet(
                        details: seerrMedia,
                        viewModel: viewModel,
                        onDismissRequest: { viewModel.hideViewRequestSheet() }
                    )
                }
            }
            .sheet(item: queueItemBinding) { wrapper in
                QueueItemInfoSheet(
                    item: wrapper.item,
                    deleteInProgress: viewModel.removeQueueItemStatus is OperationStatusInProgress,
                    onDelete: { remove, block, skip in
                        viewModel.removeQueueItem(item: wrapper.item, removeFromClient: remove, addToBlocklist: block, skipRedownload: skip)
                        selectedQueueItem = nil
                    }
                )
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
            }
            .sheet(item: $selectedTracearrSession) { session in
                TracearrStreamDetailsSheet(
                    session: session,
                    onNavigateToDetails: { _, _ in },
                    onNavigateToUser: { _ in }
                )
            }
    }
}
