//
//  NewInstanceView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-03.
//

import SwiftUI
import Shared

struct NewInstanceView: View {
    @Environment(\.dismiss) var dismiss
    
    @StateObject private var viewModel = AddInstanceViewModelS()
    
    @State private var instanceType: InstanceType
    private let onSaveSuccess: () -> Void
    
    init(
        initialType: InstanceType = .sonarr,
        onSaveSuccess: @escaping () -> Void
    ) {
        self.instanceType = initialType
        self.onSaveSuccess = onSaveSuccess
    }
    
    private var uiState: AddInstanceUiState {
        viewModel.uiState
    }
    
    private var showInfoCard: Bool {
        uiState.shouldShowInfoCard(type: instanceType)
    }
    
    var body: some View {
        content
            .toolbar { toolbarContent }
            .onChange(of: instanceType) { _, _ in
                viewModel.setInstanceLabel(instanceType.name)
            }
            .onChange(of: viewModel.createWasSuccessful) { _, newValue in
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
            onTestLocalConnection: { viewModel.testLocalConnection(instanceType) },
            onToggleNotificationsEnabled: { viewModel.toggleNotificationsEnabled() },
            onDismissInfoCard: { viewModel.dismissInfoCard($0) },
            showInfoCard: showInfoCard,
            showInstancePicker: true,
            instanceType: $instanceType,
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
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .cancellationAction) {
            Button(MR.strings().cancel.localized()) {
                dismiss()
            }
        }
        ToolbarItem(placement: .primaryAction) {
            if uiState.testing {
                ProgressView()
            } else {
                Button(MR.strings().save.localized()) {
                    viewModel.createInstance(instanceType)
                }
                .disabled(!uiState.saveButtonEnabled)
            }
        }
    }
}
