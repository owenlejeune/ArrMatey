//
//  AddMediaSheetsHostView.swift
//  iosApp
//

import SwiftUI
import Shared

struct AddMediaSheetsHostView: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onDismiss: () -> Void

    var body: some View {
        if let arrMedia = success.arrMedia {
            if let series = arrMedia as? ArrSeries {
                AddSeriesForm(
                    series: series,
                    addItemStatus: viewModel.addItemStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    tags: viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags,
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { newItem, searchOnAdd in
                        viewModel.smartAdd(item: newItem, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        onDismiss()
                    },
                    onDismiss: { onDismiss() },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            } else if let movie = arrMedia as? ArrMovie {
                AddMovieForm(
                    movie: movie,
                    addItemStatus: viewModel.addItemStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    tags: viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags,
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { newItem, searchOnAdd in
                        viewModel.smartAdd(item: newItem, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        onDismiss()
                    },
                    onDismiss: { onDismiss() },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            } else if let artist = arrMedia as? Arrtist {
                AddArtistForm(
                    artist: artist,
                    addItemStatus: viewModel.addItemStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    tags: viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags,
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { newItem, searchOnAdd in
                        viewModel.smartAdd(item: newItem, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        onDismiss()
                    },
                    onDismiss: { onDismiss() },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            } else if let author = arrMedia as? Author {
                AddAuthorForm(
                    author: author,
                    addItemStatus: viewModel.addItemStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    tags: viewModel.addSheetUiState.tags.isEmpty ? viewModel.tags : viewModel.addSheetUiState.tags,
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { newItem, searchOnAdd in
                        viewModel.smartAdd(item: newItem, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        onDismiss()
                    },
                    onDismiss: { onDismiss() },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            } else if let audiobook = arrMedia as? Audiobook {
                let searchAudiobook = SearchAudiobookKt.createSearchAudiobook(audiobook: audiobook)
                AddAudiobookForm(
                    audiobook: searchAudiobook,
                    addItemStatus: viewModel.addItemStatus,
                    qualityProfiles: viewModel.addSheetUiState.qualityProfiles.isEmpty ? viewModel.qualityProfiles : viewModel.addSheetUiState.qualityProfiles,
                    rootFolders: viewModel.addSheetUiState.rootFolders.isEmpty ? viewModel.rootFolders : viewModel.addSheetUiState.rootFolders,
                    relativePath: "",
                    preferences: viewModel.preferences,
                    onUpdatePreferences: { viewModel.updatePreferences(preferences: $0) },
                    onAddItem: { newItem, searchOnAdd in
                        viewModel.smartAdd(item: newItem, searchOnAdd: searchOnAdd, targetInstanceId: viewModel.addSheetUiState.targetInstance?.id)
                        onDismiss()
                    },
                    onDismiss: { onDismiss() },
                    instances: viewModel.addSheetUiState.availableInstances,
                    selectedInstance: viewModel.addSheetUiState.targetInstance,
                    onInstanceSelected: { viewModel.setAddSheetTargetInstance(instance: $0) }
                )
            }
        }
    }
}
