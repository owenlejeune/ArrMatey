//
//  InstancesPageView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-18.
//

import SwiftUI
import Shared

struct InstancesPageView: View {
    let instances: [Instance]
    let downloadClients: [DownloadClient]
    let onAddInstance: () -> Void
    let onAddDownloadClient: () -> Void
    let onDeleteInstance: (Instance) -> Void
    let onDeleteDownloadClient: (DownloadClient) -> Void

    init(
        instances: [Instance],
        downloadClients: [DownloadClient] = [],
        onAddInstance: @escaping () -> Void,
        onAddDownloadClient: @escaping () -> Void = {},
        onDeleteInstance: @escaping (Instance) -> Void,
        onDeleteDownloadClient: @escaping (DownloadClient) -> Void = { _ in }
    ) {
        self.instances = instances
        self.downloadClients = downloadClients
        self.onAddInstance = onAddInstance
        self.onAddDownloadClient = onAddDownloadClient
        self.onDeleteInstance = onDeleteInstance
        self.onDeleteDownloadClient = onDeleteDownloadClient
    }

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 20) {
                    Spacer(minLength: 16)

                    VStack(spacing: 8) {
                        Text(MR.strings().onboarding_instances_title.localized())
                            .font(.title2.bold())
                            .multilineTextAlignment(.center)

                        Text(MR.strings().onboarding_instances_subtitle.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 20)
                    }

                    if instances.isEmpty && downloadClients.isEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: "server.rack")
                                .font(.system(size: 40))
                                .foregroundColor(.secondary.opacity(0.6))
                                .padding(.top, 24)

                            Text(MR.strings().onboarding_instances_empty.localized())
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 32)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 24)
                        .background(Color(UIColor.secondarySystemGroupedBackground))
                        .cornerRadius(16)
                        .padding(.horizontal, 20)
                    } else {
                        if !instances.isEmpty {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("\(MR.strings().services.localized()) (\(instances.count))")
                                    .font(.caption.bold())
                                    .foregroundColor(.secondary)
                                    .padding(.horizontal, 24)

                                ForEach(instances, id: \.id) { instance in
                                    HStack(spacing: 14) {
                                        instance.type.icon.toImage(renderingMode: .original)
                                                .frame(width: 24, height: 24)

                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(instance.label)
                                                .font(.headline)

                                            Text(instance.url)
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                                .lineLimit(1)
                                        }

                                        Spacer()

                                        Button(action: { onDeleteInstance(instance) }) {
                                            Image(systemName: "trash")
                                                .font(.subheadline)
                                                .foregroundColor(.red)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                    .padding(14)
                                    .background(Color(UIColor.secondarySystemGroupedBackground))
                                    .cornerRadius(12)
                                    .padding(.horizontal, 20)
                                }
                            }
                        }

                        if !downloadClients.isEmpty {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("\(MR.strings().download_clients.localized()) (\(downloadClients.count))")
                                    .font(.caption.bold())
                                    .foregroundColor(.secondary)
                                    .padding(.horizontal, 24)

                                ForEach(downloadClients, id: \.id) { client in
                                    HStack(spacing: 14) {
                                        client.type.tabIcon.toImage(renderingMode: .original)
                                            .frame(width: 24, height: 24)

                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(client.label)
                                                .font(.headline)

                                            Text(client.url)
                                                .font(.caption)
                                                .foregroundColor(.secondary)
                                                .lineLimit(1)
                                        }

                                        Spacer()

                                        Button(action: { onDeleteDownloadClient(client) }) {
                                            Image(systemName: "trash")
                                                .font(.subheadline)
                                                .foregroundColor(.red)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                    .padding(14)
                                    .background(Color(UIColor.secondarySystemGroupedBackground))
                                    .cornerRadius(12)
                                    .padding(.horizontal, 20)
                                }
                            }
                        }
                    }

                    VStack(spacing: 10) {
                        Button(action: onAddInstance) {
                            HStack {
                                Image(systemName: "plus.circle.fill")
                                Text(MR.strings().onboarding_add_instance.localized())
                            }
                            .font(.subheadline.bold())
                            .foregroundColor(.themePrimary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.themePrimary.opacity(0.12))
                            .cornerRadius(14)
                        }
                        .buttonStyle(.plain)

                        Button(action: onAddDownloadClient) {
                            HStack {
                                Image(systemName: "plus.circle.fill")
                                Text(MR.strings().add_download_client.localized())
                            }
                            .font(.subheadline.bold())
                            .foregroundColor(.themePrimary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.themePrimary.opacity(0.12))
                            .cornerRadius(14)
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(.horizontal, 20)

                    Spacer(minLength: 20)
                }
                .frame(maxWidth: 540)
                .frame(minHeight: geometry.size.height)
                .frame(maxWidth: .infinity, alignment: .center)
            }
        }
    }
}
