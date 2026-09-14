//
//  EditMediaSheetsHostView.swift
//  iosApp
//

import SwiftUI
import Shared

struct EditMediaSheetsHostView: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let showEditPathSheet: Bool
    let showEditSheet: Bool
    let onDismissEditPath: () -> Void
    let onDismissEditMedia: () -> Void
    let onRequestMoveFiles: (ArrMedia) -> Void

    var body: some View {
        if let arrMedia = success.arrMedia {
            if showEditPathSheet {
                EditPathView(
                    item: arrMedia,
                    rootFolders: viewModel.rootFolders,
                    onEdit: { updatedItem, moveFiles in
                        viewModel.editItem(item: updatedItem, moveFiles: moveFiles)
                        onDismissEditPath()
                    }
                )
            } else if showEditSheet {
                if let movie = arrMedia as? ArrMovie {
                    EditMovieSheet(
                        item: movie,
                        qualityProfiles: viewModel.qualityProfiles,
                        rootFolders: viewModel.rootFolders,
                        tags: viewModel.tags,
                        editInProgress: viewModel.editStatus is OperationStatusInProgress,
                        onEditItem: { updatedItem, moveFiles in
                            if movie.rootFolderPath != updatedItem.rootFolderPath {
                                onRequestMoveFiles(updatedItem)
                            } else {
                                viewModel.editItem(item: updatedItem, moveFiles: moveFiles)
                                onDismissEditMedia()
                            }
                        }
                    )
                } else if let series = arrMedia as? ArrSeries {
                    EditSeriesSheet(
                        item: series,
                        qualityProfiles: viewModel.qualityProfiles,
                        rootFolders: viewModel.rootFolders,
                        tags: viewModel.tags,
                        editInProgress: viewModel.editStatus is OperationStatusInProgress,
                        onEditItem: { updatedItem, moveFiles in
                            if series.rootFolderPath != updatedItem.rootFolderPath {
                                onRequestMoveFiles(updatedItem)
                            } else {
                                viewModel.editItem(item: updatedItem, moveFiles: moveFiles)
                                onDismissEditMedia()
                            }
                        }
                    )
                } else if let artist = arrMedia as? Arrtist {
                    EditArtistSheet(
                        item: artist,
                        qualityProfiles: viewModel.qualityProfiles,
                        rootFolders: viewModel.rootFolders,
                        tags: viewModel.tags,
                        editInProgress: viewModel.editStatus is OperationStatusInProgress,
                        onEditItem: { updatedItem, moveFiles in
                            if artist.rootFolderPath != updatedItem.rootFolderPath {
                                onRequestMoveFiles(updatedItem)
                            } else {
                                viewModel.editItem(item: updatedItem, moveFiles: moveFiles)
                                onDismissEditMedia()
                            }
                        }
                    )
                } else if let author = arrMedia as? Author {
                    EditAuthorSheet(
                        item: author,
                        qualityProfiles: viewModel.qualityProfiles,
                        rootFolders: viewModel.rootFolders,
                        tags: viewModel.tags,
                        editInProgress: viewModel.editStatus is OperationStatusInProgress,
                        onEditItem: { updatedItem, moveFiles in
                            if author.rootFolderPath != updatedItem.rootFolderPath {
                                onRequestMoveFiles(updatedItem)
                            } else {
                                viewModel.editItem(item: updatedItem, moveFiles: moveFiles)
                                onDismissEditMedia()
                            }
                        }
                    )
                } else if let audiobook = arrMedia as? Audiobook {
                    EditAudiobookSheet(
                        item: audiobook,
                        qualityProfiles: viewModel.qualityProfiles,
                        rootFolders: viewModel.rootFolders,
                        editInProgress: viewModel.editStatus is OperationStatusInProgress,
                        onEditItem: { updatedItem in
                            viewModel.editItem(item: updatedItem)
                            onDismissEditMedia()
                        }
                    )
                }
            }
        }
    }
}
