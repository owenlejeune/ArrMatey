//
//  ServicesSettingsView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-08-31.
//

import SwiftUI
import Shared

struct ServicesSettingsView: View {
    @ObservedObject private var viewModel = MoreScreenViewModelS()

    private var instances: [Instance] {
        viewModel.instances
    }

    private var downloadClients: [DownloadClient] {
        viewModel.downloadClients
    }

    private var customWebpages: [CustomWebpage] {
        viewModel.customWebpages
    }

    private func route(for instance: Instance) -> SettingsRoute {
        switch instance.type {
        case .sonarr, .radarr, .lidarr, .bookshelf, .listenarr:
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
                ForEach(customWebpages, id: \.self) { webpage in
                    WebpageCard(webpage: webpage)
                }
                NavigationLink(value: SettingsRoute.newCustomWebpage) {
                    Text("Add custom webpage")
                        .foregroundColor(.themePrimary)
                }
            } header: {
                Text(MR.strings().custom_webpages.localized())
            }
        }
        .navigationTitle(MR.strings().services.localized())
    }
}

struct InstanceCard: View {
    let instance: Instance
    let route: SettingsRoute
    let connectionStatuses: [KotlinLong:OperationStatus]

    var body: some View {
        NavigationLink(value: route) {
            HStack(spacing: 8) {
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
            HStack(spacing: 8) {
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

struct WebpageCard: View {
    let webpage: CustomWebpage

    var body: some View {
        NavigationLink(value: SettingsRoute.editCustomWebpage(webpage.id)) {
            HStack(spacing: 8) {
                Image(systemName: "globe")
                    .frame(width: 32, height: 32)
                VStack(alignment: .leading, spacing: 1) {
                    Text(webpage.name)
                        .font(.system(size: 18, weight: .medium))
                    Text(webpage.url)
                        .font(.system(size: 16))
                }
            }
        }
    }
}
