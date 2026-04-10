//
//  ArrConfigurationView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-20.
//

import Shared
import SwiftUI
import UserNotifications

struct ArrConfigurationView: View {
    let uiState: AddInstanceUiState
    let onApiEndpointChanged: (String) -> Void
    let onApiKeyChanged: (String) -> Void
    let onInstanceLabelChanged: (String) -> Void
    let onIsSlowInstanceChanged: (Bool) -> Void
    let onCustomTimeoutChanged: (Int64?) -> Void
    let onHeadersChanged: ([InstanceHeader]) -> Void
    let onTestConnection: () -> Void
    let onLocalNetworkEnabledChanged: (Bool) -> Void
    let onLocalNetworkUrlChanged: (String) -> Void
    let onLocalNetworkSsidChanged: (String) -> Void
    let onTestLocalConnection: () -> Void
    let onToggleNotificationsEnabled: () -> Void
    let onDismissInfoCard: (InstanceType) -> Void
    let showInfoCard: Bool
    let showInstancePicker: Bool
    @Binding var instanceType: InstanceType
    @Binding var showError: Bool
    
    @State private var apiEndpoint: String = ""
    @State private var apiKey: String = ""
    @State private var instanceLabel: String = ""
    @State private var customTimeoutText: String = ""
    @State private var headers: [InstanceHeader] = []
    
    @StateObject private var permissionHandler = LocationPermissionHandler()
    @State private var showRationale = false
    
    @Environment(\.openURL) var openURL
    
    private var urlPlaceholder: String {
        "https://example.com:\(instanceType.defaultPort)"
    }
    
    private var hasLabelConflict: Bool {
        if let result = uiState.createResult as? InsertResultConflict {
            return result.fields.contains(.instanceLabel)
        }
        return false
    }
    
    private var hasUrlConflict: Bool {
        if let result = uiState.createResult as? InsertResultConflict {
            return result.fields.contains(.instanceUrl)
        }
        return false
    }
    
    var body: some View {
        Form {
            if showInfoCard {
                infoCardView()
            }
            
            instanceSection
            testSection
            notificationSection
            localNetworkArea
            slowInstanceSection
            
        }
        .onChange(of: instanceType, initial: true) { _, newValue in
            instanceLabel = newValue.name
            onInstanceLabelChanged(newValue.name)
        }
        .onChange(of: uiState.headers) { _, newHeaders in
            headers = newHeaders
        }
        .alert(MR.strings().error.localized(), isPresented: $showError) {
            Button(MR.strings().ok.localized()) { showError = false }
        } message: {
            Group {
                if let error = uiState.createResult as? InsertResultError {
                    Text(error.message)
                } else {
                    if hasLabelConflict {
                        Text(MR.strings().instance_label_exists.localized())
                    }
                    if hasUrlConflict {
                        Text(MR.strings().instance_url_exists.localized())
                    }
                }
            }
        }
    }
    
    @ViewBuilder
    private func infoCardView() -> some View {
        VStack(spacing: 8) {
            HStack {
                Image(resource: instanceType.icon)
                    .resizable()
                    .frame(width: 24, height: 24)
                Text(String(instanceType.name))
                    .font(.system(size: 18, weight: .medium))
                Spacer()
                Button(action: {
                    onDismissInfoCard(instanceType)
                }) {
                    Image(systemName: "xmark")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundStyle(.secondary)
                        .frame(width: 32, height: 32)
                }
                .buttonStyle(.plain)
            }
            Text(instanceType.resource.localized())
                .font(.system(size: 14))
            HStack(spacing: 8) {
                Button(MR.strings().github.localized(), action: {
                    if let url = URL(string: instanceType.github) {
                        openURL(url)
                    }
                })
                .frame(maxWidth: .infinity)
                
                Button(MR.strings().website.localized(), action: {
                    if let url = URL(string: instanceType.website) {
                        openURL(url)
                    }
                })
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal)
            .alignmentGuide(VerticalAlignment.center) { d in d[VerticalAlignment.center]}
        }
    }
    
    @ViewBuilder
    private var instanceSection: some View {
        Section {
            if (showInstancePicker) {
                Picker(MR.strings().instance_type.localized(), selection: $instanceType) {
                    ForEach(InstanceType.allCases,  id: \.self) { type in
                        Text(String(localized: LocalizedStringResource(stringLiteral: type.name)))
                            .tag(type)
                    }
                }
                .tint(.primary)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 24) {
                    Text(MR.strings().label.localized()).layoutPriority(2)
                    TextField(
                        text: Binding(
                            get: { instanceLabel.isEmpty ? uiState.instanceLabel : instanceLabel },
                            set: { newValue in
                                instanceLabel = newValue
                                onInstanceLabelChanged(newValue)
                            }),
                        prompt: Text(instanceType.name)) {
                            EmptyView()
                        }
                        .multilineTextAlignment(.trailing)
                }
                
                if hasLabelConflict {
                    Text(MR.strings().instance_label_exists.localized())
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
            
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 24) {
                    Text(MR.strings().host.localized()).layoutPriority(2)
                    TextField(
                        text: Binding(
                            get: { apiEndpoint.isEmpty ? uiState.apiEndpoint : apiEndpoint },
                            set: { newValue in
                                apiEndpoint = newValue
                                onApiEndpointChanged(newValue)
                            }
                        ),
                        prompt: Text(urlPlaceholder)
                    ) {
                        EmptyView()
                    }
                    .multilineTextAlignment(.trailing)
                    .textInputAutocapitalization(.never)
                }
                
                if uiState.endpointError {
                    Text(MR.strings().invalid_host.localized())
                        .font(.caption)
                        .foregroundColor(.red)
                } else if hasUrlConflict {
                    Text(MR.strings().instance_url_exists.localized())
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }
            
            HStack(spacing: 24) {
                Text(MR.strings().api_key.localized())
                TextField(
                    text: Binding(
                        get: { apiKey.isEmpty ? uiState.apiKey : apiKey },
                        set: { newValue in
                            apiKey = newValue
                            onApiKeyChanged(newValue)
                        }
                    ),
                    prompt: Text(MR.strings().api_key_placeholder.localized())
                ) {
                    EmptyView()
                }
                .multilineTextAlignment(.trailing)
                .textInputAutocapitalization(.never)
            }
        } footer: {
            Text(MR.strings().host_description.formatted(args: [instanceType.name]))
        }
    }
    
    @ViewBuilder
    private var testSection: some View {
        HStack {
            Button(action: onTestConnection) {
                if uiState.testing {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                } else {
                    Text(MR.strings().test.localized())
                }
            }
            .disabled(uiState.testing || uiState.apiEndpoint.isEmpty || uiState.apiKey.isEmpty)
            
            Spacer()
            
            if let testResult = uiState.testResult {
                HStack(spacing: 4) {
                    Text(testResult.boolValue ? MR.strings().success.localized() : MR.strings().failure.localized())
                        .foregroundColor(testResult.boolValue ? .green : .red)
                        .multilineTextAlignment(.trailing)
                }
            }
        }
    }

    @ViewBuilder
    private var notificationSection: some View {
        Section {
            Toggle(isOn: Binding(
                get: { uiState.notificationsEnabled },
                set: { newValue in
                    if newValue {
                        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
                            if granted {
                                DispatchQueue.main.async {
                                    onToggleNotificationsEnabled()
                                }
                            }
                        }
                    } else {
                        onToggleNotificationsEnabled()
                    }
                }
            )) {
                VStack(alignment: .leading) {
                    Text(MR.strings().enable_notifications.localized())
                    Text(MR.strings().enable_notifications_description.localized())
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }
    
    @ViewBuilder
    private var slowInstanceSection: some View {
        Section {
            Toggle(
                MR.strings().slow_instance.localized(),
                isOn: Binding(
                    get: { uiState.isSlowInstance },
                    set: { onIsSlowInstanceChanged($0) }
                )
            )
            HStack(spacing: 24) {
                Text(MR.strings().custom_timeout_seconds.localized())
                    .foregroundStyle(uiState.isSlowInstance ? Color.primary.opacity(1.0) : Color.primary.opacity(0.3))
                TextField(
                    text: Binding(
                        get: {
                            if !customTimeoutText.isEmpty {
                                return customTimeoutText
                            }
                            return "\(uiState.customTimeout?.int64Value ?? 0)"
                        },
                        set: { newValue in
                            customTimeoutText = newValue
                            let timeout = Int64(newValue)
                            onCustomTimeoutChanged(timeout)
                        }
                    ),
                    prompt: Text("300")
                ) {
                    EmptyView()
                }
                .multilineTextAlignment(.trailing)
                .keyboardType(.numberPad)
                .disabled(!uiState.isSlowInstance)
            }
        }
    }
    
    @ViewBuilder
    private var headersSection: some View {
        Section {
            ForEach(headers.indices, id: \.self) { index in
                HeaderItemView(
                    header: Binding(
                        get: { headers[index] },
                        set: { newValue in
                            headers[index] = newValue
                            onHeadersChanged(headers)
                        }
                    )
                )
                .swipeActions {
                    Button(MR.strings().delete.localized()) {
                        headers.remove(at: index)
                        onHeadersChanged(headers)
                    }
                    .tint(.red)
                }
            }
            
            Button(action: {
                headers.append(InstanceHeader(key: "", value: ""))
                onHeadersChanged(headers)
            }) {
                Label(MR.strings().add_header.localized(), systemImage: "plus")
            }
        } header: {
            Text(MR.strings().custom_headers.localized())
        } footer: {
            Text(MR.strings().custom_headers_description.localized())
        }
    }
    
    @ViewBuilder
    private var localNetworkArea: some View {
        Section {
            Toggle(isOn: Binding(
                get: { uiState.localNetworkEnabled },
                set: { newValue in
                    onLocalNetworkEnabledChanged(newValue)
                    if newValue && !permissionHandler.isGranted() {
                        showRationale = true
                    }
                }
            )) {
                Text(MR.strings().use_local_network.localized())
            }
                
            if uiState.localNetworkEnabled {
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
                            TextField("http://192.168.1.100:\(instanceType.defaultPort)",
                                      text: Binding(get: { uiState.localNetworkUrl }, set: onLocalNetworkUrlChanged))
                            .multilineTextAlignment(.trailing)
                            
                        }
                        
                        if uiState.localNetworkUrlError {
                            Text(MR.strings().invalid_url.localized())
                                .font(.caption).foregroundColor(.red)
                        }
                    }
                            
                    VStack {
                        HStack(spacing: 24) {
                                Text(MR.strings().wifi_network_name.localized()).layoutPriority(2)
                            TextField("MyHomeWiFi",
                                      text: Binding(get: { uiState.localNetworkSsid }, set: onLocalNetworkSsidChanged))
                            .multilineTextAlignment(.trailing)
                        }
                        
                    }
                            
                    Button(action: {
                        if let ssid = NetworkUtilsKt.getNetworkUtils().getCurrentWifiSsid() {
                            onLocalNetworkSsidChanged(ssid)
                        }
                    }) {
                        Label(MR.strings().use_current_network.localized(), systemImage: "wifi")
                    }
                            
                    HStack {
                        Button(action: onTestConnection) {
                            if uiState.localTesting {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle())
                            } else {
                                Text(MR.strings().test.localized())
                            }
                        }
                        .disabled(uiState.localTesting || uiState.localNetworkUrl.isEmpty || uiState.localNetworkSsid.isEmpty)
                        
                        Spacer()
                        
                        if let localTestResult = uiState.localTestResult {
                            HStack(spacing: 4) {
                                Text(localTestResult.boolValue ? MR.strings().success.localized() : MR.strings().failure.localized())
                                    .foregroundColor(localTestResult.boolValue ? .green : .red)
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
                onLocalNetworkEnabledChanged(false)
            }
        } message: {
            Text(MR.strings().location_rationale_description_ios.localized())
        }
    }
    
    @ViewBuilder
    private var testResultView: some View {
        if let result = uiState.localTestResult?.boolValue {
            HStack {
                Image(systemName: result ? "checkmark.circle.fill" : "xmark.circle.fill")
                    .foregroundColor(result ? .green : .red)
                Text(result ? MR.strings().success.localized() : MR.strings().failure.localized())
                    .foregroundColor(result ? .green : .red)
            }
        }
    }
}
