//
//  EditInstanceScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-17.
//

import Shared
import SwiftUI

struct EditInstanceScreen: View {
    private let id: Int64
    
    @Environment(\.dismiss) var dismiss
    
    @StateObject private var viewModel: EditInstanceViewModelS
    
    @State private var showDeleteConfirmation: Bool = false
    private let onSaveSuccess: () -> Void
    private let onDelete: (() -> Void)?
    
    init(id: Int64, onSaveSuccess: @escaping () -> Void, onDelete: (() -> Void)? = nil) {
        self.id = id
        _viewModel = StateObject(wrappedValue: EditInstanceViewModelS(id))
        self.onSaveSuccess = onSaveSuccess
        self.onDelete = onDelete
    }
    
    private var uiState: AddInstanceUiState {
        viewModel.uiState
    }
    
    private var instance: Instance? {
        viewModel.instance
    }
    
    private var testResult: Bool? {
        uiState.testResult?.boolValue
    }
    
    private var editSuccess: Bool {
        uiState.editResult is InsertResultSuccess
    }
    
    var body: some View {
        content
            .onChange(of: editSuccess) { _, newValue in
                if newValue {
                    dismiss()
                    onSaveSuccess()
                }
            }
    }
    
    @ViewBuilder
    private var content: some View {
        ArrConfigurationView(
            uiState: uiState,
            onApiEndpointChanged: { viewModel.setApiEndpoint($0) },
            onApiKeyChanged: { viewModel.setApiKey($0) },
            onNoApiKeyRequiredChanged: { viewModel.setNoApiKeyRequired($0) },
            onInstanceLabelChanged: { viewModel.setInstanceLabel($0) },
            onIsSlowInstanceChanged: { viewModel.setIsSlowInstance($0) },
            onCustomTimeoutChanged: { viewModel.setCustomTimeout($0) },
            onHeadersChanged: { viewModel.updateHeaders($0) },
            onLocalNetworkEnabledChanged: { viewModel.setLocalNetworkEnabled($0) },
            onLocalNetworkUrlChanged: { viewModel.setLocalNetworkUrl($0) },
            onLocalNetworkSsidsChanged: { viewModel.setLocalNetworkSsids($0) },
            onTestLocalConnection: { viewModel.testLocalConnection() },
            onToggleNotificationsEnabled: { viewModel.toggleNotificationsEnabled() },
            onDismissInfoCard: { _ in },
            showInfoCard: false,
            showInstancePicker: false,
            instanceType: .constant(instance?.type ?? .sonarr),
            showError: $viewModel.showError,
            onSeerrAuthTypeChanged: { viewModel.setSeerrAuthType($0) },
            onLoginLocal: { email, pass in viewModel.loginLocal(email: email, pass: pass) },
            onLoginPlex: { token in viewModel.loginPlex(token: token) },
            onLoginJellyfin: { user, pass in viewModel.loginJellyfin(username: user, pass: pass) },
            onStartQuickConnect: { viewModel.startJellyfinQuickConnect() },
            onCancelQuickConnect: { viewModel.cancelJellyfinQuickConnect() },
            onPlexTokenChange: { viewModel.setPlexToken(token: $0) },
            onStartPlexSignIn: { onOpenUrl in viewModel.startPlexSignIn(onOpenUrl: onOpenUrl) },
            onCancelPlexSignIn: { viewModel.cancelPlexSignIn() },
            onClearAuthSession: { viewModel.clearAuthSession() }
        )
        .toolbar { toolbarContent }
        .alert(MR.strings().confirm.localized(), isPresented: $showDeleteConfirmation, presenting: viewModel.instance) { instance in
            confirmDeleteButtons(instance)
        } message: { instance in
            Text(MR.strings().confirm_delete_instance.formatted(args: [instance.label]))
        }
    }
    
    @ViewBuilder
    private func confirmDeleteButtons(_ instance: Instance) -> some View {
        Button(MR.strings().yes.localized(), role: .destructive) {
            viewModel.delete(instance)
            if let onDelete = onDelete {
                onDelete()
            } else {
                dismiss()
            }
        }
        Button(MR.strings().no.localized(), role: .cancel) {
            showDeleteConfirmation = false
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if instance != nil {
            ToolbarItem(placement: .primaryAction) {
                Image(systemName: "trash")
                    .imageScale(.medium)
                    .foregroundColor(.red)
                    .onTapGesture {
                        showDeleteConfirmation = true
                    }
            }
            ToolbarItem(placement: .primaryAction) {
                if uiState.testing {
                    ProgressView()
                } else {
                    Button(MR.strings().save.localized()) {
                        viewModel.updateInstance()
                    }
                    .disabled(!uiState.saveButtonEnabled)
                }
            }
        }
    }
}
