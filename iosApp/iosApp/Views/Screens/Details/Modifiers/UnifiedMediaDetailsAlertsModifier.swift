//
//  UnifiedMediaDetailsAlertsModifier.swift
//  iosApp
//

import SwiftUI
import Shared

@MainActor
struct UnifiedMediaDetailsArrAlertsModifier: ViewModifier {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    @Binding var confirmDeleteMovie: Bool
    @Binding var confirmDeleteSeasonNumber: Int32?
    @Binding var confirmDeleteAlbumId: Int64?
    @Binding var confirmDeleteEpisodeId: Int64?
    @Binding var moveFilesItem: ArrMedia?

    func body(content: Content) -> some View {
        let confirmDeleteEpisodeBinding = SwiftUI.Binding<Bool>(
            get: { confirmDeleteEpisodeId != nil },
            set: { if !$0 { confirmDeleteEpisodeId = nil } }
        )
        let deleteSeasonBinding = SwiftUI.Binding<Bool>(
            get: { confirmDeleteSeasonNumber != nil },
            set: { if !$0 { confirmDeleteSeasonNumber = nil } }
        )
        let deleteAlbumBinding = SwiftUI.Binding<Bool>(
            get: { confirmDeleteAlbumId != nil },
            set: { if !$0 { confirmDeleteAlbumId = nil } }
        )
        let moveFilesBinding = SwiftUI.Binding<Bool>(
            get: { moveFilesItem != nil },
            set: { if !$0 { moveFilesItem = nil } }
        )

        content
            .alert(MR.strings().move_files.localized(), isPresented: moveFilesBinding) {
                Button(MR.strings().move_files.localized()) {
                    if let item = moveFilesItem {
                        viewModel.editItem(item: item, moveFiles: true)
                    }
                    moveFilesItem = nil
                }
                Button(MR.strings().no.localized()) {
                    if let item = moveFilesItem {
                        viewModel.editItem(item: item, moveFiles: false)
                    }
                    moveFilesItem = nil
                }
                Button(MR.strings().cancel.localized(), role: .cancel) {
                    moveFilesItem = nil
                }
            } message: {
                if let item = moveFilesItem {
                    Text(MR.strings().move_files_confirm.formatted(args: [item.rootFolderPath ?? ""]))
                }
            }
            .alert(MR.strings().confirm_delete.localized(), isPresented: $confirmDeleteMovie) {
                Button(MR.strings().cancel.localized(), role: .cancel) { }
                Button(MR.strings().confirm.localized(), role: .destructive) {
                    viewModel.deleteMovieFile()
                }
            } message: {
                Text(MR.strings().confirm_delete_file.localized())
            }
            .alert(MR.strings().confirm_delete.localized(), isPresented: confirmDeleteEpisodeBinding) {
                Button(MR.strings().cancel.localized(), role: .cancel) { }
                Button(MR.strings().confirm.localized(), role: .destructive) {
                    if let fileId = confirmDeleteEpisodeId {
                        viewModel.deleteEpisodeFile(episodeId: fileId)
                    }
                    confirmDeleteEpisodeId = nil
                }
            } message: {
                Text(MR.strings().episode_delete_message.localized())
            }
            .confirmationDialog("", isPresented: deleteSeasonBinding) {
                Button(MR.strings().confirm.localized(), role: .destructive) {
                    if let season = confirmDeleteSeasonNumber {
                        viewModel.deleteSeasonFiles(seasonNumber: season)
                    }
                    confirmDeleteSeasonNumber = nil
                }
                Button(MR.strings().cancel.localized(), role: .cancel) {
                    confirmDeleteSeasonNumber = nil
                }
            } message: {
                if let season = confirmDeleteSeasonNumber {
                    Text(MR.strings().delete_season_confirm.formatted(args: [season]))
                }
            }
            .confirmationDialog("", isPresented: deleteAlbumBinding) {
                Button(MR.strings().confirm.localized(), role: .destructive) {
                    if let albumId = confirmDeleteAlbumId {
                        viewModel.deleteAlbumFiles(albumId: albumId)
                    }
                    confirmDeleteAlbumId = nil
                }
                Button(MR.strings().cancel.localized(), role: .cancel) {
                    confirmDeleteAlbumId = nil
                }
            } message: {
                Text(MR.strings().delete_album_confirm.localized())
            }
    }
}

@MainActor
struct UnifiedMediaDetailsSeerrAlertsModifier: ViewModifier {
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    @Binding var confirmRemoveFromService: Bool
    @Binding var confirmClearData: Bool
    let removeServiceName: String

    func body(content: Content) -> some View {
        content
            .alert(MR.strings().are_you_sure.localized(), isPresented: $confirmRemoveFromService) {
                Button(MR.strings().no.localized(), role: .cancel) {}
                Button(role: .destructive) {
                    viewModel.deleteSeerrMediaFile(is4k: false)
                } label: {
                    Text(MR.strings().yes.localized())
                }
            } message: {
                Text(MR.strings().remove_from_service_confirm.formatted(args: [removeServiceName]))
            }
            .alert(MR.strings().are_you_sure.localized(), isPresented: $confirmClearData) {
                Button(MR.strings().no.localized(), role: .cancel) {}
                Button(role: .destructive) {
                    viewModel.clearSeerrMediaData()
                } label: {
                    Text(MR.strings().yes.localized())
                }
            } message: {
                Text(MR.strings().clear_data_confirm.localized())
            }
    }
}
