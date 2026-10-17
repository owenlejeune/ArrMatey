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
    let onNoApiKeyRequiredChanged: (Bool) -> Void
    let onInstanceLabelChanged: (String) -> Void
    let onIsSlowInstanceChanged: (Bool) -> Void
    let onCustomTimeoutChanged: (Int64?) -> Void
    let onHeadersChanged: ([InstanceHeader]) -> Void
    let onLocalNetworkEnabledChanged: (Bool) -> Void
    let onLocalNetworkUrlChanged: (String) -> Void
    let onLocalNetworkSsidsChanged: ([String]) -> Void
    let onTestLocalConnection: () -> Void
    let onToggleNotificationsEnabled: () -> Void
    let onDismissInfoCard: (InstanceType) -> Void
    let showInfoCard: Bool
    let showInstancePicker: Bool
    @Binding var instanceType: InstanceType
    @Binding var showError: Bool
    var onSeerrAuthTypeChanged: ((SeerrAuthType) -> Void)? = nil
    var onLoginLocal: ((String, String) -> Void)? = nil
    var onLoginPlex: ((String) -> Void)? = nil
    var onLoginJellyfin: ((String, String) -> Void)? = nil
    var onStartQuickConnect: (() -> Void)? = nil
    var onCancelQuickConnect: (() -> Void)? = nil
    var onPlexTokenChange: ((String) -> Void)? = nil
    var onStartPlexSignIn: (((@escaping (String) -> Void)) -> Void)? = nil
    var onCancelPlexSignIn: (() -> Void)? = nil
    var onClearAuthSession: (() -> Void)? = nil
    
    @State private var apiEndpoint: String = ""
    @State private var instanceLabel: String = ""
    @State private var customTimeoutText: String = ""
    @State private var headers: [InstanceHeader] = []
    @State private var localEmail: String = ""
    @State private var localPassword: String = ""
    @State private var jellyfinUsername: String = ""
    @State private var jellyfinPassword: String = ""
    @State private var copiedPlexLink: Bool = false
    
    @StateObject private var permissionHandler = LocationPermissionHandler()
    @State private var showRationale = false
    
    @Environment(\.openURL) var openURL
    
    private var urlPlaceholder: String {
        "https://example.com:\(instanceType.defaultPort)"
    }
    
    var body: some View {
        Form {
            if showInfoCard {
                infoCardView()
            }
            
            instanceSection
            if instanceType.supportsNotifications {
                notificationSection
            }
            localNetworkArea
            slowInstanceSection
            headersSection
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
                    if uiState.hasLabelConflict {
                        Text(MR.strings().instance_label_exists.localized())
                    }
                    if uiState.hasUrlConflict {
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
                
                if uiState.hasLabelConflict {
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
                } else if uiState.hasUrlConflict {
                    Text(MR.strings().instance_url_exists.localized())
                        .font(.caption)
                        .foregroundColor(.red)
                }
            }

            if instanceType == .seerr {
                Picker(MR.strings().auth_type.localized(), selection: Binding(
                    get: { uiState.seerrAuthType },
                    set: { onSeerrAuthTypeChanged?($0) }
                )) {
                    Text(MR.strings().auth_type_api_key.localized()).tag(SeerrAuthType.apiKey)
                    Text(MR.strings().auth_type_local.localized()).tag(SeerrAuthType.local)
                    Text(MR.strings().auth_type_plex.localized()).tag(SeerrAuthType.plex)
                    Text(MR.strings().auth_type_jellyfin.localized()).tag(SeerrAuthType.jellyfin)
                    Text(MR.strings().auth_type_quick_connect.localized()).tag(SeerrAuthType.jellyfinQuickConnect)
                }
                .tint(.primary)
            }
            
            if instanceType != .seerr || uiState.seerrAuthType == .apiKey {
                Toggle(MR.strings().no_api_key.localized(), isOn: Binding(
                    get: { uiState.noApiKeyRequired },
                    set: { onNoApiKeyRequiredChanged($0) }
                ))
                
                HStack(spacing: 24) {
                    Text(MR.strings().api_key.localized())
                        .foregroundStyle(!uiState.noApiKeyRequired ? Color.primary.opacity(1.0) : Color.primary.opacity(0.3))
                    TextField(
                        text: Binding(
                            get: { uiState.apiKey },
                            set: { newValue in
                                onApiKeyChanged(newValue)
                            }
                        ),
                        prompt: Text(MR.strings().api_key_placeholder.localized())
                    ) {
                        EmptyView()
                    }
                    .disabled(uiState.noApiKeyRequired)
                    .multilineTextAlignment(.trailing)
                    .textInputAutocapitalization(.never)
                }
                
                if let getApiKey = instanceType.getApiKeyEndpoint {
                    let enabled = uiState.apiEndpoint.isValidUrl()
                    Button(action: {
                        if let url = URL(string: "\(uiState.apiEndpoint)/\(getApiKey)") {
                            openURL(url)
                        }
                    }) {
                        HStack(spacing: 4) {
                            Text(MR.strings().api_key_in_browser.localized())
                                .underline()
                                .font(.caption)
                            Image(systemName: "safari")
                                .font(.caption)
                        }
                        .foregroundStyle(enabled ? Color.accentColor : Color.secondary)
                    }
                    .disabled(!enabled)
                    .buttonStyle(.plain)
                }
            } else {
                seerrAuthSection
            }
        } footer: {
            Text(MR.strings().host_description.formatted(args: [instanceType.name]))
        }
    }

    @ViewBuilder
    private var seerrAuthSection: some View {
        if !uiState.sessionToken.isEmpty {
            HStack {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(.green)
                Text(uiState.authenticatedUserDisplayName.map { MR.strings().signed_in_as.formatted(args: [$0]) } ?? MR.strings().authenticated_session.localized())
                    .font(.subheadline)
                Spacer()
                Button(MR.strings().sign_out.localized()) {
                    onClearAuthSession?()
                }
                .buttonStyle(.bordered)
                .font(.caption)
            }
        } else {
            if let authError = uiState.authError {
                Text(authError)
                    .font(.caption)
                    .foregroundColor(.red)
            }
            
            switch uiState.seerrAuthType {
            case .local:
                TextField(
                    MR.strings().email.localized(),
                    text: $localEmail
                )
                .textContentType(.emailAddress)
                .keyboardType(.emailAddress)
                .textInputAutocapitalization(.never)
                
                SecureField(
                    MR.strings().password.localized(),
                    text: $localPassword
                )
                .textContentType(.password)
                
                Button(action: {
                    onLoginLocal?(localEmail, localPassword)
                }) {
                    HStack {
                        Spacer()
                        if uiState.isAuthenticating {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle())
                        } else {
                            Text(MR.strings().sign_in.localized())
                        }
                        Spacer()
                    }
                }
                .disabled(uiState.isAuthenticating || localEmail.trimmingCharacters(in: .whitespaces).isEmpty || localPassword.isEmpty || !uiState.apiEndpoint.isValidUrl())
                
            case .plex:
                if uiState.isPlexWaiting {
                    VStack(alignment: .center, spacing: 12) {
                        HStack(spacing: 8) {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle())
                            Text(MR.strings().waiting_for_plex.localized())
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        
                        Text(MR.strings().plex_sign_in_instructions.localized())
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                        
                        if let authUrl = uiState.plexAuthUrl, let url = URL(string: authUrl) {
                            Button(action: { openURL(url) }) {
                                HStack {
                                    Spacer()
                                    Image(systemName: "safari")
                                    Text(MR.strings().open_page_again.localized())
                                    Spacer()
                                }
                            }
                            .buttonStyle(.borderedProminent)
                            .font(.subheadline)
                            
                            HStack(spacing: 12) {
                                Button(action: {
                                    UIPasteboard.general.string = authUrl
                                    copiedPlexLink = true
                                }) {
                                    HStack(spacing: 4) {
                                        Image(systemName: "link")
                                        Text(copiedPlexLink ? MR.strings().copied_to_clipboard.localized() : MR.strings().copy_link.localized())
                                    }
                                    .frame(maxWidth: .infinity)
                                }
                                .buttonStyle(.bordered)
                                .font(.caption)
                                
                                Button(MR.strings().cancel.localized()) {
                                    onCancelPlexSignIn?()
                                }
                                .buttonStyle(.bordered)
                                .font(.caption)
                                .frame(maxWidth: .infinity)
                            }
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                } else {
                    Button(action: {
                        onStartPlexSignIn? { urlString in
                            if let url = URL(string: urlString) {
                                openURL(url)
                            }
                        }
                    }) {
                        HStack {
                            Spacer()
                            Image(systemName: "arrow.right.to.line")
                            Text(MR.strings().sign_in_with_plex.localized())
                            Spacer()
                        }
                    }
                    .disabled(uiState.isAuthenticating)
                }
                
                HStack {
                    VStack { Divider() }
                    Text(MR.strings().or.localized())
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    VStack { Divider() }
                }
                .padding(.vertical, 4)
                
                TextField(
                    MR.strings().plex_auth_token.localized(),
                    text: Binding(
                        get: { uiState.plexToken },
                        set: { onPlexTokenChange?($0) }
                    )
                )
                .textInputAutocapitalization(.never)
                
                Button(action: {
                    onLoginPlex?(uiState.plexToken)
                }) {
                    HStack {
                        Spacer()
                        if uiState.isAuthenticating {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle())
                        } else {
                            Text(MR.strings().sign_in.localized())
                        }
                        Spacer()
                    }
                }
                .disabled(uiState.isAuthenticating || uiState.plexToken.trimmingCharacters(in: .whitespaces).isEmpty || !uiState.apiEndpoint.isValidUrl())
                
            case .jellyfin:
                TextField(
                    MR.strings().username.localized(),
                    text: $jellyfinUsername
                )
                .textInputAutocapitalization(.never)
                
                SecureField(
                    MR.strings().password.localized(),
                    text: $jellyfinPassword
                )
                .textContentType(.password)
                
                Button(action: {
                    onLoginJellyfin?(jellyfinUsername, jellyfinPassword)
                }) {
                    HStack {
                        Spacer()
                        if uiState.isAuthenticating {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle())
                        } else {
                            Text(MR.strings().sign_in.localized())
                        }
                        Spacer()
                    }
                }
                .disabled(uiState.isAuthenticating || jellyfinUsername.trimmingCharacters(in: .whitespaces).isEmpty || jellyfinPassword.isEmpty || !uiState.apiEndpoint.isValidUrl())
                
            case .jellyfinQuickConnect:
                if let code = uiState.quickConnectCode {
                    VStack(alignment: .center, spacing: 12) {
                        Text(MR.strings().quick_connect_instructions.localized())
                            .font(.subheadline)
                            .multilineTextAlignment(.center)
                        
                        Text(code)
                            .font(.system(size: 32, weight: .bold, design: .monospaced))
                            .foregroundColor(.accentColor)
                        
                        HStack(spacing: 8) {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle())
                            Text(MR.strings().quick_connect_waiting.localized())
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        
                        Button(MR.strings().cancel.localized()) {
                            onCancelQuickConnect?()
                        }
                        .buttonStyle(.bordered)
                        .font(.caption)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                } else {
                    Button(action: {
                        onStartQuickConnect?()
                    }) {
                        HStack {
                            Spacer()
                            if uiState.isAuthenticating {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle())
                            } else {
                                Text(MR.strings().quick_connect_start.localized())
                            }
                            Spacer()
                        }
                    }
                    .disabled(uiState.isAuthenticating || !uiState.apiEndpoint.isValidUrl())
                }
                
            default:
                EmptyView()
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
                    ),
                    availableSsids: uiState.localNetworkSsids,
                    localNetworkConfigured: uiState.localNetworkConfigured
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
                            TextField("MyHomeWiFi, MyGuestWiFi",
                                      text: Binding(
                                        get: { uiState.localNetworkSsids.joined(separator: ", ") },
                                        set: { newValue in
                                            let ssids = newValue.components(separatedBy: ",").map { $0.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
                                            onLocalNetworkSsidsChanged(ssids)
                                        }
                                      ))
                            .multilineTextAlignment(.trailing)
                        }
                    }
                            
                    Button(action: {
                        if let ssid = NetworkUtilsKt.getNetworkUtils().getCurrentWifiSsid() {
                            var currentSsids = uiState.localNetworkSsids
                            if !currentSsids.contains(ssid) {
                                currentSsids.append(ssid)
                                onLocalNetworkSsidsChanged(currentSsids)
                            }
                        }
                    }) {
                        Label(MR.strings().use_current_network.localized(), systemImage: "wifi")
                    }
                            
                    HStack {
                        Button(action: onTestLocalConnection) {
                            if uiState.localTesting {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle())
                            } else {
                                Text(MR.strings().test.localized())
                            }
                        }
                        .disabled(uiState.localTesting || uiState.localNetworkUrl.isEmpty || (!uiState.noApiKeyRequired && uiState.apiKey.isEmpty))
                        
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
