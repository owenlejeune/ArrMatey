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
            .alert(MR.strings().confirm.localized(), isPresented: $confirmDelete) {
                confirmDeleteButtons()
            } message: {
                Text(MR.strings().confirm_delete_download_client.localized())
            }
    }
    
    @ViewBuilder
    private func confirmDeleteButtons() -> some View {
        Button(MR.strings().yes.localized(), role: .destructive) {
            viewModel.deleteClient()
            dismiss()
        }
        Button(MR.strings().no.localized(), role: .cancel) {
            confirmDelete = false
        }
    }
    
    private var formContent: some View {
        Form {
            typeSection
            authSection
            headersSection
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
                if viewModel.uiState.endpointError {
                    Text(MR.strings().invalid_host.localized())
                        .font(.caption)
                        .foregroundColor(.red)
                } else if hasUrlConflict {
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
            
            Toggle(MR.strings().use_basic_auth.localized(), isOn: Binding(
                get: { viewModel.uiState.basicAuthEnabled },
                set: { viewModel.updateBasicAuthEnabled($0) }
            ))
            
            HStack(spacing: 24) {
                Text(MR.strings().client_api_key.localized()).layoutPriority(2)
                    .foregroundStyle(viewModel.uiState.basicAuthEnabled ? Color.primary.opacity(1.0) : Color.primary.opacity(0.3))
                TextField(
                    text: Binding(
                        get: { viewModel.uiState.apiKey },
                        set: { viewModel.updateApiKey($0) }
                    )
                ) {
                    EmptyView()
                }
                .disabled(viewModel.uiState.basicAuthEnabled)
                .multilineTextAlignment(.trailing)
                .textInputAutocapitalization(.never)
            }
        }
    }
    
    @ViewBuilder
    private var headersSection: some View {
        Section {
            ForEach(viewModel.uiState.headers.indices, id: \.self) { index in
                HeaderItemView(
                    header: Binding(
                        get: { viewModel.uiState.headers[index] },
                        set: { newValue in
                            var headers = viewModel.uiState.headers
                            headers[index] = newValue
                            viewModel.updateHeadrs(headers)
                        }
                    )
                )
                .swipeActions {
                    Button(MR.strings().delete.localized()) {
                        var headers = viewModel.uiState.headers
                        headers.remove(at: index)
                        viewModel.updateHeadrs(headers)
                    }
                    .tint(.red)
                }
            }
            
            Button(action: {
                var headers = viewModel.uiState.headers
                headers.append(InstanceHeader(key: "", value: ""))
                viewModel.updateHeadrs(headers)
            }) {
                Label(MR.strings().add_header.localized(), systemImage: "plus")
            }
        } header: {
            Text(MR.strings().custom_headers.localized())
        } footer: {
            Text(MR.strings().custom_headers_description.localized())
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
