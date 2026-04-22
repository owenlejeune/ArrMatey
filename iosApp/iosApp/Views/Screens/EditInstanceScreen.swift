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
    
    @ObservedObject private var viewModel: EditInstanceViewModelS
    
    @State private var showDeleteConfirmation: Bool = false
    @State private var saveClicked: Bool = false
    private let onSaveSuccess: () -> Void
    
    init(id: Int64, onSaveSuccess: @escaping () -> Void) {
        self.id = id
        self.viewModel = EditInstanceViewModelS(id)
        self.onSaveSuccess = onSaveSuccess
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
            .onChange(of: viewModel.uiState.testResult) { _, newValue in
                if newValue?.boolValue == true && saveClicked {
                    viewModel.updateInstance()
                }
            }
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
            onBasicAuthChanged: { viewModel.setBasicAuthEnabled($0) },
            onInstanceLabelChanged: { viewModel.setInstanceLabel($0) },
            onIsSlowInstanceChanged: { viewModel.setIsSlowInstance($0) },
            onCustomTimeoutChanged: { viewModel.setCustomTimeout($0) },
            onHeadersChanged: { viewModel.updateHeaders($0) },
            onTestConnection: { viewModel.testConnection() },
            onLocalNetworkEnabledChanged: { viewModel.setLocalNetworkEnabled($0) },
            onLocalNetworkUrlChanged: { viewModel.setLocalNetworkUrl($0) },
            onLocalNetworkSsidsChanged: { viewModel.setLocalNetworkSsids($0) },
            onTestLocalConnection: { viewModel.testLocalConnection() },
            onToggleNotificationsEnabled: { viewModel.toggleNotificationsEnabled() },
            onDismissInfoCard: { _ in },
            showInfoCard: false,
            showInstancePicker: false,
            instanceType: .constant(instance?.type ?? .sonarr),
            showError: $viewModel.showError
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
            dismiss()
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
                Button(MR.strings().save.localized()) {
                    Task {
                        saveClicked = true
                        viewModel.testConnection()
                    }
                }
            }
        }
    }

}
