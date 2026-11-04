//
//  MediaServerSettingsViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class MediaServerSettingsViewModelS: ObservableObject {
    private let viewModel: MediaServerSettingsViewModel

    @Published private(set) var configuredServers: [MediaServerType: MediaServerIntegration] = [:]
    @Published private(set) var configState: MediaServerConfigState? = nil

    init() {
        self.viewModel = KoinBridge.shared.getMediaServerSettingsViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.configuredServers.observeAsync(on: self) { owner, servers in
            owner.configuredServers = servers
        }
        viewModel.configState.observeAsync(on: self) { owner, state in
            owner.configState = state
        }
    }

    func openConfiguration(for serverType: MediaServerType) {
        viewModel.openConfiguration(serverType: serverType)
    }

    func closeConfiguration() {
        viewModel.closeConfiguration()
    }

    func updateLabel(_ label: String) {
        viewModel.updateLabel(label: label)
    }

    func updateUrl(_ url: String) {
        viewModel.updateUrl(url: url)
    }

    func updateToken(_ token: String) {
        viewModel.updateToken(token: token)
    }

    func updateUsername(_ username: String) {
        viewModel.updateUsername(username: username)
    }

    func updatePassword(_ password: String) {
        viewModel.updatePassword(password: password)
    }

    func updateAuthMode(_ mode: MediaServerAuthMode) {
        viewModel.updateAuthMode(mode: mode)
    }

    func updateEnabled(_ enabled: Bool) {
        viewModel.updateEnabled(enabled: enabled)
    }

    func updateLocalNetworkEnabled(_ enabled: Bool) {
        viewModel.updateLocalNetworkEnabled(enabled: enabled)
    }

    func updateLocalNetworkEndpoint(_ endpoint: String) {
        viewModel.updateLocalNetworkEndpoint(endpoint: endpoint)
    }

    func updateLocalNetworkSsids(_ ssids: [String]) {
        viewModel.updateLocalNetworkSsids(ssids: ssids)
    }

    func updateHeaders(_ headers: [InstanceHeader]) {
        viewModel.updateHeaders(headers: headers)
    }

    func selectDiscoveredPlexServer(_ server: PlexServerResource) {
        viewModel.selectDiscoveredPlexServer(server: server)
    }

    func selectPlexConnection(_ connection: PlexServerConnection, useDirectIp: Bool = false) {
        viewModel.selectPlexConnection(connection: connection, useDirectIp: useDirectIp)
    }

    func startPlexSignIn(onOpenUrl: @escaping (String) -> Void) {
        viewModel.startPlexSignIn(onOpenUrl: onOpenUrl)
    }

    func cancelPlexSignIn() {
        viewModel.cancelPlexSignIn()
    }

    func startQuickConnect() {
        viewModel.startJellyfinQuickConnect()
    }

    func cancelQuickConnect() {
        viewModel.cancelQuickConnect()
    }

    func authenticateCredentials() {
        viewModel.authenticateCredentials()
    }

    func testConnection() {
        viewModel.testConnection()
    }

    func saveConfiguration() {
        viewModel.saveConfiguration()
    }

    func disconnectServer(serverType: MediaServerType) {
        viewModel.disconnectServer(serverType: serverType)
    }
}

