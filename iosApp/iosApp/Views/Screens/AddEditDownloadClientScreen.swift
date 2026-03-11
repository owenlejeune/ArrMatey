//
//  AddEditDownloadClientScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct AddEditDownloadClientScreen: View {

    @ObservedObject private var viewModel: DownloadClientSettingsViewModelS
    @Environment(\.dismiss) private var dismiss
    
    @State private var confirmDelete: Bool = false

    init(id: Int64? = nil) {
        self.viewModel = DownloadClientSettingsViewModelS(id: id)
    }
    
    var hasLabelConflict: Bool {
        (viewModel.uiState.mutationState as? DownloadClientMutationStateConflict)?.fields.contains(.downloadClientLabel) ?? false
    }
    
    var hasUrlConflict: Bool {
        (viewModel.uiState.mutationState as? DownloadClientMutationStateConflict)?.fields.contains(.downloadClientUrl) ?? false
    }

    var body: some View {
        formContent
            .toolbar {
                toolbarContent
            }
            .onChange(of: viewModel.mutationSuccess) { _, isSuccess in
                if isSuccess {
                    dismiss()
                }
            }
    }
    
    private var formContent: some View {
        Form {
            typeSection
            authSection
            Section {
                Toggle(
                    MR.strings().client_enabled.localized(),
                    isOn: Binding(
                        get: { viewModel.uiState.enabled },
                        set: { viewModel.updateEnabled($0) }
                    )
                )
            }
        }
    }
    
    private var typeSection: some View {
        Section {
            Picker(MR.strings().client_type.localized(), selection: Binding(
                get: { viewModel.uiState.selectedType },
                set: { viewModel.updateSelectedType($0) }
            )) {
                ForEach(DownloadClientType.allCases, id: \.self) { type in
                    Text(type.displayName).tag(type)
                }
            }
            .tint(.primary)
            
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 24) {
                    Text(MR.strings().client_label.localized()).layoutPriority(2)
                    TextField(
                        text: Binding(
                            get: { viewModel.uiState.label },
                            set: { viewModel.updateLabel($0) }
                        ),
                        prompt: Text(viewModel.uiState.selectedType.displayName)
                    ) {
                        EmptyView()
                    }
                    .multilineTextAlignment(.trailing)
                }
                if hasLabelConflict {
                    Text(MR.strings().field_conflict.formatted(args: [MR.strings().client_label.localized()]))
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
            
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 24) {
                    Text(MR.strings().host.localized()).layoutPriority(2)
                    TextField(
                        text: Binding(
                            get: { viewModel.uiState.url },
                            set: { viewModel.updateUrl($0) }
                        ),
                        prompt: Text(MR.strings().host_placeholder.localized() + String(describing: viewModel.uiState.selectedType.defaultPort))
                    ) {
                        EmptyView()
                    }
                    .multilineTextAlignment(.trailing)
                    .textInputAutocapitalization(.never)
                }
                if hasUrlConflict {
                    Text(MR.strings().field_conflict.formatted(args: [MR.strings().client_url.localized()]))
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
        }
    }
    
    private var authSection: some View {
        Section {
            HStack(spacing: 24) {
                Text(MR.strings().client_username.localized()).layoutPriority(2)
                TextField(
                    text: Binding(
                        get: { viewModel.uiState.username },
                        set: { viewModel.updateUsername($0) }
                    )
                ) {
                    EmptyView()
                }
                .multilineTextAlignment(.trailing)
                .textInputAutocapitalization(.never)
            }
            HStack(spacing: 24) {
                Text(MR.strings().client_password.localized()).layoutPriority(2)
                SecureField(
                    text: Binding(
                        get: { viewModel.uiState.password },
                        set: { viewModel.updatePassword($0) }
                    )
                ) {
                    EmptyView()
                }
                .multilineTextAlignment(.trailing)
                .textInputAutocapitalization(.never)
            }
            HStack(spacing: 24) {
                Text(MR.strings().client_api_key.localized()).layoutPriority(2)
                TextField(
                    text: Binding(
                        get: { viewModel.uiState.apiKey },
                        set: { viewModel.updateApiKey($0) }
                    )
                ) {
                    EmptyView()
                }
                .multilineTextAlignment(.trailing)
                .textInputAutocapitalization(.never)
            }
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if viewModel.uiState.isEditing {
            ToolbarItem(placement: .primaryAction) {
                Button(action: {
                    confirmDelete = true
                }) {
                    Image(systemName: "trash")
                        .imageScale(.medium)
                }
                .buttonStyle(BorderedProminentButtonStyle())
                .tint(.red)
            }
        }
        ToolbarItem(placement: .primaryAction) {
            Button(action: {
                viewModel.submit()
            }) {
                if viewModel.uiState.isTesting {
                    ProgressView().progressViewStyle(CircularProgressViewStyle())
                } else {
                    Text(MR.strings().save.localized())
                }
            }
            .disabled(!viewModel.uiState.saveButtonEnabled || viewModel.uiState.isTesting)
        }
    }
}
