//
//  UnifiedMediaDetailsToolbarMenuView.swift
//  iosApp
//

import SwiftUI
import Shared

struct UnifiedMediaDetailsToolbarMenuView: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onShowEditSheet: () -> Void
    let onShowConfirmSheet: () -> Void
    let onShowAddSheetForInstance: (Instance) -> Void
    var onConfirmDeleteFile: (() -> Void)? = nil
    let onConfirmRemoveFromService: () -> Void
    let onConfirmClearData: () -> Void

    var body: some View {
        let buttonState = viewModel.buttonState
        let showArrActions = success.hasArrId && viewModel.isArrConfigured
        let showSeerrActions = viewModel.isSeerrConfigured && (buttonState.showRemoveFromServiceButton || buttonState.showClearDataButton || buttonState.showMarkAsAvailableButton)
        let showMissingInstances = !success.missingInstances.isEmpty
        let showMenuButton = showArrActions || showSeerrActions || showMissingInstances

        if showMenuButton {
            Menu {
                if showArrActions {
                    Section {
                        Button(action: { viewModel.performRefresh() }) {
                            Label(MR.strings().refresh.localized(), systemImage: "arrow.clockwise")
                        }

                        if viewModel.resolvedInstanceType?.includeTopLevelAutomaticSearchOption == true {
                            Button(action: { viewModel.performAutomaticLookup() }) {
                                Label(MR.strings().search_monitored.localized(), systemImage: "magnifyingglass")
                            }
                            .disabled(!viewModel.isMonitored)
                        }

                        Button(action: onShowEditSheet) {
                            Label(MR.strings().edit.localized(), systemImage: "pencil")
                        }

                        if success.canDeleteFile(instanceType: viewModel.resolvedInstanceType), let onConfirmDeleteFile = onConfirmDeleteFile {
                            Button(role: .destructive, action: onConfirmDeleteFile) {
                                Label(MR.strings().delete_files.localized(), systemImage: "trash")
                            }
                        }

                        Button(role: .destructive, action: onShowConfirmSheet) {
                            Label(MR.strings().delete.localized(), systemImage: "trash")
                        }
                    }
                }

                if showMissingInstances {
                    Section {
                        ForEach(success.missingInstances, id: \.id) { instance in
                            Button(action: {
                                onShowAddSheetForInstance(instance)
                            }) {
                                Label(MR.strings().add_to_arr.formatted(args: [instance.label]), systemImage: "plus")
                            }
                        }
                    }
                }

                if showSeerrActions {
                    Section {
                        if buttonState.showMarkAsAvailableButton {
                            let markTitle = viewModel.resolvedRequestType == RequestType.movie ? MR.strings().mark_as_available.localized() : MR.strings().mark_all_seasons_as_available.localized()
                            Button(action: { viewModel.markSeerrMediaAsAvailable() }) {
                                Label(markTitle, systemImage: "checkmark.circle")
                            }
                        }

                        if buttonState.showRemoveFromServiceButton {
                            let removeTitle = viewModel.resolvedRequestType == RequestType.movie ? MR.strings().remove_from_radarr.localized() : MR.strings().remove_from_sonarr.localized()
                            Button(role: .destructive, action: onConfirmRemoveFromService) {
                                Label(removeTitle, systemImage: "trash")
                            }
                        }

                        if buttonState.showClearDataButton {
                            Button(role: .destructive, action: onConfirmClearData) {
                                Label(MR.strings().clear_data.localized(), systemImage: "xmark.bin")
                            }
                        }
                    }
                }
            } label: {
                Image(systemName: "ellipsis.circle")
            }
        }
    }
}
