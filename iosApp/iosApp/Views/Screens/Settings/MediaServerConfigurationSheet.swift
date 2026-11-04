//
//  MediaServerConfigurationSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct MediaServerConfigurationSheet: View {
    @ObservedObject var viewModel: MediaServerSettingsViewModelS
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            if let state = viewModel.configState {
                Form {
                    Section {
                        TextField(MR.strings().label.localized(), text: Binding(
                            get: { state.label },
                            set: { viewModel.updateLabel($0) }
                        ))

                        TextField(MR.strings().server_url.localized(), text: Binding(
                            get: { state.url },
                            set: { viewModel.updateUrl($0) }
                        ))
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                        .keyboardType(.URL)
                    } header: {
                        Text("Connection")
                    }

                    authSection(state: state)

                    Section {
                        Toggle(MR.strings().use_local_network.localized(), isOn: Binding(
                            get: { state.localNetworkEnabled },
                            set: { viewModel.updateLocalNetworkEnabled($0) }
                        ))

                        if state.localNetworkEnabled {
                            TextField(MR.strings().local_network_url.localized(), text: Binding(
                                get: { state.localNetworkEndpoint },
                                set: { viewModel.updateLocalNetworkEndpoint($0) }
                            ))
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .keyboardType(.URL)
                        }
                    } header: {
                        Text(MR.strings().local_network_switching.localized())
                    }

                    if let error = state.errorMessage {
                        Section {
                            Text(error)
                                .foregroundColor(.red)
                                .font(.footnote)
                        }
                    }

                    if state.testSuccess == true {
                        Section {
                            HStack {
                                Image(systemName: "checkmark.circle.fill")
                                    .foregroundColor(.green)
                                Text(MR.strings().connection_success.localized())
                                    .foregroundColor(.green)
                            }
                        }
                    }

                    Section {
                        Button {
                            viewModel.testConnection()
                        } label: {
                            HStack {
                                Spacer()
                                if state.isTesting {
                                    ProgressView()
                                } else {
                                    Text(MR.strings().test_connection.localized())
                                }
                                Spacer()
                            }
                        }
                        .disabled(state.url.isEmpty || state.token.isEmpty || state.isTesting)

                        Button {
                            viewModel.saveConfiguration()
                        } label: {
                            HStack {
                                Spacer()
                                if state.isSaving {
                                    ProgressView()
                                } else {
                                    Text(MR.strings().save.localized())
                                        .bold()
                                }
                                Spacer()
                            }
                        }
                        .disabled(state.url.isEmpty || state.token.isEmpty || state.isSaving)

                        if state.existingId > 0 {
                            Button(role: .destructive) {
                                viewModel.disconnectServer(serverType: state.serverType)
                                dismiss()
                            } label: {
                                HStack {
                                    Spacer()
                                    Text(MR.strings().disconnect.localized())
                                    Spacer()
                                }
                            }
                        }
                    }
                }
                .navigationTitle(state.serverType.name)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button(MR.strings().cancel.localized()) {
                            viewModel.closeConfiguration()
                            dismiss()
                        }
                    }
                }
                .onChange(of: state.saveCompleted) { _, completed in
                    if completed {
                        viewModel.closeConfiguration()
                        dismiss()
                    }
                }
            } else {
                ProgressView()
            }
        }
    }

    @ViewBuilder
    private func authSection(state: MediaServerConfigState) -> some View {
        Section {
            if state.serverType == .plex {
                plexAuthView(state: state)
            } else if state.serverType == .jellyfin {
                jellyfinAuthView(state: state)
            } else {
                embyAuthView(state: state)
            }
        } header: {
            Text(MR.strings().authentication.localized())
        }
    }

    @ViewBuilder
    private func plexAuthView(state: MediaServerConfigState) -> some View {
        if state.isPlexWaiting {
            HStack {
                ProgressView()
                Spacer().frame(width: 12)
                Text("Authorizing with Plex...")
                    .font(.subheadline)
            }
            Button(MR.strings().cancel.localized(), role: .cancel) {
                viewModel.cancelPlexSignIn()
            }
        } else {
            Button {
                viewModel.startPlexSignIn { authUrl in
                    if let url = URL(string: authUrl) {
                        UIApplication.shared.open(url)
                    }
                }
            } label: {
                HStack {
                    Spacer()
                    Image(systemName: "person.crop.circle.badge.checkmark")
                    Text("Sign in with Plex")
                    Spacer()
                }
            }
        }

        if !state.discoveredPlexServers.isEmpty {
            Picker("Server", selection: Binding(
                get: { state.selectedPlexServer },
                set: { if let s = $0 { viewModel.selectDiscoveredPlexServer(s) } }
            )) {
                ForEach(state.discoveredPlexServers, id: \.clientIdentifier) { server in
                    Text(server.name).tag(server as PlexServerResource?)
                }
            }
        }

        SecureField(MR.strings().api_key.localized(), text: Binding(
            get: { state.token },
            set: { viewModel.updateToken($0) }
        ))
    }

    @ViewBuilder
    private func jellyfinAuthView(state: MediaServerConfigState) -> some View {
        Picker("Mode", selection: Binding(
            get: { state.authMode },
            set: { viewModel.updateAuthMode($0) }
        )) {
            Text("Quick Connect").tag(MediaServerAuthMode.oauthOrQuickConnect)
            Text("Credentials").tag(MediaServerAuthMode.usernamePassword)
            Text("Manual Token").tag(MediaServerAuthMode.manualToken)
        }
        .pickerStyle(.segmented)

        if state.authMode == .oauthOrQuickConnect {
            if let code = state.quickConnectCode {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Quick Connect Code")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    HStack {
                        Text(code)
                            .font(.system(.title2, design: .monospaced, weight: .bold))
                        Spacer()
                        Button {
                            UIPasteboard.general.string = code
                        } label: {
                            Image(systemName: "doc.on.doc")
                        }
                    }
                    if state.isQuickConnectWaiting {
                        Text("Approve this code in your Jellyfin client or web app.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                Button(MR.strings().cancel.localized(), role: .cancel) {
                    viewModel.cancelQuickConnect()
                }
            } else {
                Button("Start Quick Connect") {
                    viewModel.startQuickConnect()
                }
            }
        } else if state.authMode == .usernamePassword {
            TextField(MR.strings().username.localized(), text: Binding(
                get: { state.username },
                set: { viewModel.updateUsername($0) }
            ))
            .autocapitalization(.none)
            .disableAutocorrection(true)

            SecureField(MR.strings().password.localized(), text: Binding(
                get: { state.password },
                set: { viewModel.updatePassword($0) }
            ))

            Button("Log In") {
                viewModel.authenticateCredentials()
            }
            .disabled(state.username.isEmpty || state.password.isEmpty)
        }

        SecureField(MR.strings().api_key.localized(), text: Binding(
            get: { state.token },
            set: { viewModel.updateToken($0) }
        ))
    }

    @ViewBuilder
    private func embyAuthView(state: MediaServerConfigState) -> some View {
        Picker("Mode", selection: Binding(
            get: { state.authMode },
            set: { viewModel.updateAuthMode($0) }
        )) {
            Text("Credentials").tag(MediaServerAuthMode.usernamePassword)
            Text("Manual Token").tag(MediaServerAuthMode.manualToken)
        }
        .pickerStyle(.segmented)

        if state.authMode == .usernamePassword {
            TextField(MR.strings().username.localized(), text: Binding(
                get: { state.username },
                set: { viewModel.updateUsername($0) }
            ))
            .autocapitalization(.none)
            .disableAutocorrection(true)

            SecureField(MR.strings().password.localized(), text: Binding(
                get: { state.password },
                set: { viewModel.updatePassword($0) }
            ))

            Button("Log In") {
                viewModel.authenticateCredentials()
            }
            .disabled(state.username.isEmpty || state.password.isEmpty)
        }

        SecureField(MR.strings().api_key.localized(), text: Binding(
            get: { state.token },
            set: { viewModel.updateToken($0) }
        ))
    }
}

