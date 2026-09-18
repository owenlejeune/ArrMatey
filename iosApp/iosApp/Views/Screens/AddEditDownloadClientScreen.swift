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
    
    @StateObject private var permissionHandler = LocationPermissionHandler()
    @State private var showRationale = false

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
            localNetworkSection
            headersSection
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
            
            Toggle(MR.strings().no_api_key.localized(), isOn: Binding(
                get: { viewModel.uiState.noApiKeyRequired },
                set: { viewModel.updateNoApiKeyRequired($0) }
            ))
            
            HStack(spacing: 24) {
                Text(MR.strings().client_api_key.localized()).layoutPriority(2)
                    .foregroundStyle(viewModel.uiState.noApiKeyRequired ? Color.primary.opacity(1.0) : Color.primary.opacity(0.3))
                TextField(
                    text: Binding(
                        get: { viewModel.uiState.apiKey },
                        set: { viewModel.updateApiKey($0) }
                    )
                ) {
                    EmptyView()
                }
                .disabled(viewModel.uiState.noApiKeyRequired)
                .multilineTextAlignment(.trailing)
                .textInputAutocapitalization(.never)
            }
            
            HStack {
                Button(action: {
                    viewModel.testConnection()
                }) {
                    if viewModel.uiState.isTesting && !viewModel.uiState.localTesting {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle())
                    } else {
                        Text(MR.strings().test.localized())
                    }
                }
                .disabled(viewModel.uiState.isTesting || viewModel.uiState.url.isEmpty)
                
                Spacer()
                
                if let testResult = viewModel.uiState.testResult?.boolValue {
                    HStack(spacing: 4) {
                        Text(testResult ? MR.strings().success.localized() : MR.strings().failure.localized())
                            .foregroundColor(testResult ? .green : .red)
                            .multilineTextAlignment(.trailing)
                    }
                }
            }
        }
    }
    
    @ViewBuilder
    private var localNetworkSection: some View {
        Section {
            Toggle(isOn: Binding(
                get: { viewModel.uiState.localNetworkEnabled },
                set: { newValue in
                    viewModel.updateLocalNetworkEnabled(newValue)
                    if newValue && !permissionHandler.isGranted() {
                        showRationale = true
                    }
                }
            )) {
                Text(MR.strings().use_local_network.localized())
            }
                
            if viewModel.uiState.localNetworkEnabled {
                if !permissionHandler.isGranted() && permissionHandler.authorizationStatus != .notDetermined {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(MR.strings().location_denied_message.localized())
                            .font(.subheadline).foregroundColor(.red)
                        
                        Button(action: {
                            if let url = URL(string: UIApplication.openSettingsURLString) {
                                UIApplication.shared.open(url)
                            }
                        }) {
                            Text(MR.strings().open_location_permissions.localized())
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                    }
                } else if permissionHandler.isGranted() {
                    VStack(alignment: .leading, spacing: 16) {
                        HStack(spacing: 24) {
                            Text(MR.strings().host.localized()).layoutPriority(2)
                            TextField("http://192.168.1.100:\(viewModel.uiState.selectedType.defaultPort)",
                                      text: Binding(
                                        get: { viewModel.uiState.localNetworkEndpoint },
                                        set: { viewModel.updateLocalNetworkUrl($0) }
                                      ))
                            .multilineTextAlignment(.trailing)
                            .textInputAutocapitalization(.never)
                        }
                        
                        if viewModel.uiState.localNetworkEndpointError {
                            Text(MR.strings().invalid_url.localized())
                                .font(.caption).foregroundColor(.red)
                        }
                    }
                            
                    VStack {
                        HStack(spacing: 24) {
                            Text(MR.strings().wifi_network_name.localized()).layoutPriority(2)
                            TextField("MyHomeWiFi, MyGuestWiFi",
                                      text: Binding(
                                        get: { viewModel.uiState.localNetworkSsids.joined(separator: ", ") },
                                        set: { newValue in
                                            let ssids = newValue.components(separatedBy: ",").map { $0.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
                                            viewModel.updateLocalNetworkSsids(ssids)
                                        }
                                      ))
                            .multilineTextAlignment(.trailing)
                        }
                    }
                            
                    Button(action: {
                        if let ssid = NetworkUtilsKt.getNetworkUtils().getCurrentWifiSsid() {
                            var currentSsids = viewModel.uiState.localNetworkSsids
                            if !currentSsids.contains(ssid) {
                                currentSsids.append(ssid)
                                viewModel.updateLocalNetworkSsids(currentSsids)
                            }
                        }
                    }) {
                        Label(MR.strings().use_current_network.localized(), systemImage: "wifi")
                    }
                            
                    HStack {
                        Button(action: {
                            viewModel.testLocalConnection()
                        }) {
                            if viewModel.uiState.localTesting {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle())
                            } else {
                                Text(MR.strings().test.localized())
                            }
                        }
                        .disabled(viewModel.uiState.localTesting || viewModel.uiState.localNetworkEndpoint.isEmpty || viewModel.uiState.localNetworkSsids.isEmpty)
                        
                        Spacer()
                        
                        if let localTestResult = viewModel.uiState.localTestResult?.boolValue {
                            HStack(spacing: 4) {
                                Text(localTestResult ? MR.strings().success.localized() : MR.strings().failure.localized())
                                    .foregroundColor(localTestResult ? .green : .red)
                                    .multilineTextAlignment(.trailing)
                            }
                        }
                    }
                }
            }
        } header: {
            Text(MR.strings().local_network_switching.localized())
        } footer: {
            Text(MR.strings().local_network_description.localized())
        }
        .alert(MR.strings().location_rationale_title.localized(), isPresented: $showRationale) {
            Button(MR.strings().confirm.localized()) {
                permissionHandler.checkAndPerformAction()
            }
            Button(MR.strings().cancel.localized(), role: .cancel) {
                viewModel.updateLocalNetworkEnabled(false)
            }
        } message: {
            Text(MR.strings().location_rationale_description_ios.localized())
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
        ToolbarItem(placement: .cancellationAction) {
            Button(MR.strings().cancel.localized()) {
                dismiss()
            }
        }
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
