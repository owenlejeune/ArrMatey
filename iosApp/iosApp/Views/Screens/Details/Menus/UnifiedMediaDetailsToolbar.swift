//
//  UnifiedMediaDetailsToolbar.swift
//  iosApp
//

import SwiftUI
import Shared

struct UnifiedMediaDetailsToolbarTrailingView: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    let onShowEditSheet: () -> Void
    let onShowConfirmSheet: () -> Void
    let onShowAddSheet: () -> Void
    let onShowAddSheetForInstance: (Instance) -> Void
    let onConfirmDeleteFile: () -> Void
    let onConfirmRemoveFromService: () -> Void
    let onConfirmClearData: () -> Void
    var onAddNewInstance: ((InstanceType) -> Void)? = nil
    var onShowMonitoring: (() -> Void)? = nil

    @Environment(\.openURL) private var openURL

    var body: some View {
        HStack(spacing: 12) {
            watchMenu
            addRequestButton
            approvalMenu
            monitorButton
            overflowMenu
        }
    }

    @ViewBuilder
    private var watchMenu: some View {
        let buttonState = viewModel.buttonState
        if buttonState.showWatchButton || buttonState.showWatchTrailerOption {
            Menu {
                if buttonState.showWatchButton, let url = buttonState.watchButtonUrl {
                    Button(action: { if let urlObj = URL(string: url) { openURL(urlObj) } }) {
                        Label(buttonState.watchButtonLabel.localized(), systemImage: "play.fill")
                    }
                }
                if buttonState.showWatchTrailerOption, let url = buttonState.trailerUrl {
                    Button(action: { if let urlObj = URL(string: url) { openURL(urlObj) } }) {
                        Label(MR.strings().watch_trailer.localized(), systemImage: "film")
                    }
                }
            } label: {
                Image(systemName: "play.circle")
            }
        }
    }

    @ViewBuilder
    private var addRequestButton: some View {
        let buttonState = viewModel.buttonState
        let canAddDirectly = !success.hasArrId && success.arrMedia != nil && viewModel.isArrConfigured
        let isInProgress = viewModel.isAddOrRequestInProgress

        if canAddDirectly || buttonState.showRequestButton || buttonState.showRequest4kButton || buttonState.showRequestMoreButton || isInProgress {
            Button(action: {
                if let selectedId = success.selectedInstanceId?.int64Value,
                   let currentInst = success.availableInstances.first(where: { $0.id == selectedId }) {
                    viewModel.setAddSheetTargetInstance(instance: currentInst)
                }
                onShowAddSheet()
            }) {
                if isInProgress {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                } else {
                    Image(systemName: "plus")
                }
            }
            .disabled(isInProgress)
        }
    }

    @ViewBuilder
    private var approvalMenu: some View {
        let buttonState = viewModel.buttonState
        if buttonState.showViewRequestButton {
            Menu {
                Button(action: { viewModel.showViewRequestSheet() }) {
                    Label(MR.strings().view_request.localized(), systemImage: "clock")
                }
                if buttonState.showApproveRequestButton {
                    Button(action: { viewModel.showViewRequestSheet() }) {
                        Label(MR.strings().approve_request.localized(), systemImage: "checkmark")
                    }
                }
                if buttonState.showDeclineRequestButton {
                    Button(role: .destructive, action: { viewModel.declineRequest(requestId: buttonState.pendingRequestId?.int64Value ?? 0) }) {
                        Label(MR.strings().decline_request.localized(), systemImage: "xmark")
                    }
                }
            } label: {
                Image(systemName: "clock")
            }
        }
    }

    @ViewBuilder
    private var monitorButton: some View {
        let showArrActions = success.hasArrId && viewModel.isArrConfigured
        if showArrActions {
            Button(action: { viewModel.toggleMonitored() }) {
                Image(systemName: viewModel.isMonitored ? "bookmark.fill" : "bookmark")
            }
        }
    }

    @ViewBuilder
    private var overflowMenu: some View {
        UnifiedMediaDetailsToolbarMenuView(
            success: success,
            viewModel: viewModel,
            onShowEditSheet: onShowEditSheet,
            onShowConfirmSheet: onShowConfirmSheet,
            onShowAddSheetForInstance: onShowAddSheetForInstance,
            onConfirmDeleteFile: onConfirmDeleteFile,
            onConfirmRemoveFromService: onConfirmRemoveFromService,
            onConfirmClearData: onConfirmClearData,
            onShowMonitoring: onShowMonitoring
        )
    }
}
