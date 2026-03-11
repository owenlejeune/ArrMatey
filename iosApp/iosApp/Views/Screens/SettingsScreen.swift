//
//  SettingsScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-11.
//

import SwiftUI
import Shared

struct SettingsScreen: View {
    
    @Environment(\.openURL) private var openURL
    @EnvironmentObject private var navigationManager: NavigationManager
    
    @ObservedObject private var viewModel = MoreScreenViewModelS()
    
    @State private var showLibrariesSheet: Bool = false
    @State private var showShareLogAlert: Bool = false
    
    private let crashManager = IOSCrashManager()
    
    private var instances: [Instance] {
        viewModel.instances
    }
    
    private var downloadClients: [DownloadClient] {
        viewModel.downloadClients
    }
    
    private func route(for instance: Instance) -> SettingsRoute {
        switch instance.type {
        case .sonarr, .radarr, .lidarr:
            return .arrDashboard(instance.id)
        default:
            return .editInstance(instance.id)
        }
    }
    
    var body: some View {
        Form {
            Section {
                ForEach(instances, id: \.self) { instance in
                    InstanceCard(instance: instance, route: route(for: instance), connectionStatuses: viewModel.connectionStatuses)
                }
                NavigationLink(value: SettingsRoute.newInstance()) {
                    Text(MR.strings().add_instance.localized())
                        .foregroundColor(.themePrimary)
                }
            } header: {
                Text(MR.strings().instances.localized())
            }
            
            Section {
                ForEach(downloadClients, id: \.self) { client in
                    DownloadClientCard(client: client, connectionStatuses: viewModel.connectionStatuses)
                }
                NavigationLink(value: SettingsRoute.newDownloadClient) {
                    Text(MR.strings().add_download_client.localized())
                        .foregroundColor(.themePrimary)
                }
            } header: {
                Text(MR.strings().download_clients.localized())
            }

            Section {
                NavigationLink(value: SettingsRoute.navigationConfig) {
                    HStack(spacing: 24) {
                        Image(systemName: "location.north.fill")
                            .foregroundColor(.themePrimary)
                            .frame(width: 32, height: 32)
                        
                        VStack(alignment: .leading, spacing: 1) {
                            Text(MR.strings().navigation_bar_configuration.localized())
                                .font(.system(size: 18, weight: .medium))
                        }
                    }
                }
                Toggle(isOn: Binding(
                    get: { viewModel.useServiceNavLogos },
                    set: { _ in viewModel.toggleUseServiceNavLogos() }
                )) {
                    Text(MR.strings().service_icons_title.localized())
                }
            }
            
            AboutCard(
                onFeatureRequestClick: { if let url = URL(string: MR.strings().feature_request_link.localized()) {
                    openURL(url)
                } },
                onBugReportClick: {
                    if crashManager.getLastCrashLog() != nil {
                        showShareLogAlert = true
                    } else if let url = URL(string: MR.strings().bug_report_link.localized()) {
                        openURL(url)
                    }
                },
                onGitHubClick: { if let url = URL(string: MR.strings().app_link.localized()) {
                    openURL(url)
                } },
                onDonateClick: { if let url = URL(string: MR.strings().bmac_link.localized()) {
                    openURL(url)
                } },
                onLibrariesClick: { showLibrariesSheet = true }
            )
            
            Section {
                if isDebug() {
                    Button("Simulate crash") {
                        NSException(
                            name: NSExceptionName("SimulatedCrash"),
                            reason: "Manual simulation for log testing",
                            userInfo: nil
                        ).raise()
                    }
                    Button("Dev settings") {
                        navigationManager.go(to: .dev)
                    }
                }
            }
        }
        .navigationTitle(MR.strings().settings.localized())
        .sheet(isPresented: $showLibrariesSheet) {
            LibrariesSheet()
        }
        .alert(MR.strings().share_crash_log.localized(), isPresented: $showShareLogAlert) {
            Button(MR.strings().yes.localized()) {
                if let log = crashManager.getLastCrashLog() {
                    crashManager.shareCrashLog(log: log)
                }
                finalizeCrashCleanup()
            }
            
            Button(MR.strings().no.localized(), role: .cancel) {
                finalizeCrashCleanup()
                if let url = URL(string: MR.strings().bug_report_link.localized()) {
                    openURL(url)
                }
            }
        } message: {
            Text(MR.strings().share_crash_log_message.localized())
        }
    }
    
    private func finalizeCrashCleanup() {
        crashManager.clearCrashLog()
        showShareLogAlert = false
    }
}

struct InstanceCard: View {
    let instance: Instance
    let route: SettingsRoute
    let connectionStatuses: [KotlinLong:OperationStatus]
    
    var body: some View {
        NavigationLink(value: route) {
            HStack(spacing: 24) {
                instance.type.icon.toImage(renderingMode: .original)
                    .frame(width: 32, height: 32)
                VStack(alignment: .leading, spacing: 1) {
                    HStack(alignment: .center, spacing: 12) {
                        Text(instance.label)
                            .font(.system(size: 18, weight: .medium))
                        Group {
                            switch connectionStatuses[instance.id.asKotlinLong] {
                            case is OperationStatusInProgress:
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle())
                            case is OperationStatusError:
                                Image(systemName: "wifi.slash")
                                    .tint(.red)
                            case is OperationStatusSuccess:
                                Image(systemName: "wifi")
                            default: ZStack{}
                            }
                        }
                        .frame(width: 8, height: 8)
                    }
                    Text(instance.url)
                        .font(.system(size: 16))
                }
            }
        }
    }
}

struct DownloadClientCard: View {
    let client: DownloadClient
    let connectionStatuses: [KotlinLong:OperationStatus]
    
    var body: some View {
        NavigationLink(value: SettingsRoute.editDownloadClient(client.id)) {
            HStack(spacing: 24) {
                client.type.icon.toImage(renderingMode: .original)
                    .frame(width: 32, height: 32)
                VStack(alignment: .leading, spacing: 1) {
                    HStack(alignment: .center, spacing: 12) {
                        Text(client.label)
                            .font(.system(size: 18, weight: .medium))
                        Group {
                            switch connectionStatuses[client.id.asKotlinLong] {
                            case is OperationStatusInProgress:
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle())
                            case is OperationStatusError:
                                Image(systemName: "wifi.slash")
                                    .tint(.red)
                            case is OperationStatusSuccess:
                                Image(systemName: "wifi")
                            default: ZStack{}
                            }
                        }
                        .frame(width: 8, height: 8)
                    }
                    Text(client.url)
                        .font(.system(size: 16))
                }
            }
        }
    }
}
