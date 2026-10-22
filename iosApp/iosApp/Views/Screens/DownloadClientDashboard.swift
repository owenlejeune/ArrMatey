//
//  DownloadClientDashboard.swift
//  iosApp
//

import Shared
import SwiftUI

struct DownloadClientDashboard: View {
    private let id: Int64

    @StateObject private var viewModel: DownloadClientDashboardViewModelS
    @State private var confirmDelete: Bool = false

    @EnvironmentObject private var navigationManager: NavigationManager
    @Environment(\.openURL) private var openURL
    @Environment(\.dismiss) private var dismiss

    init(id: Int64) {
        self.id = id
        _viewModel = StateObject(wrappedValue: DownloadClientDashboardViewModelS(id))
    }

    var body: some View {
        contentForState()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .navigationTitle(viewModel.client?.label ?? "")
            .toolbar {
                ToolbarItemGroup(placement: .topBarTrailing) {
                    NavigationLink(value: SettingsRoute.editDownloadClient(id)) {
                        Image(systemName: "pencil")
                    }

                    Menu {
                        if let clientUrl = viewModel.client?.url, let url = URL(string: clientUrl) {
                            Button {
                                openURL(url)
                            } label: {
                                Label(MR.strings().view_web_gui.localized(), systemImage: "globe")
                            }
                        }

                        Button(role: .destructive) {
                            confirmDelete = true
                        } label: {
                            Label(MR.strings().delete.localized(), systemImage: "trash")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                    }
                }
            }
            .alert(
                MR.strings().confirm.localized(),
                isPresented: $confirmDelete
            ) {
                Button(MR.strings().yes.localized(), role: .destructive) {
                    viewModel.deleteClient()
                    dismiss()
                }
                Button(MR.strings().no.localized(), role: .cancel) {}
            } message: {
                Text(String(format: MR.strings().confirm_delete_download_client.localized(), viewModel.client?.label ?? ""))
            }
    }

    @ViewBuilder
    private func contentForState() -> some View {
        let state = viewModel.state

        if state is DownloadClientDashboardUiStateInitial {
            ZStack { EmptyView() }
        } else if state is DownloadClientDashboardUiStateLoading {
            loadingArea
        } else if let error = state as? DownloadClientDashboardUiStateError {
            errorArea(error)
        } else if let success = state as? DownloadClientDashboardUiStateSuccess {
            successArea(success)
        } else {
            ZStack { EmptyView() }
        }
    }

    @ViewBuilder
    private func successArea(_ state: DownloadClientDashboardUiStateSuccess) -> some View {
        let overview = state.overview
        ZStack {
            Form {
                if let update = overview.updateInfo, update.isAvailable {
                    updateArea(update)
                }
                activityArea(overview)
                diskSpaceArea(overview)
                infoArea(overview)
            }
        }
        .refreshable {
            viewModel.refresh()
        }
    }

    @ViewBuilder
    private func updateArea(_ update: UpdateInfo) -> some View {
        Section {
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 8) {
                    Image(systemName: "arrow.up.circle.fill")
                        .foregroundColor(.accentColor)
                        .imageScale(.large)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(update.latestVersion != nil ? String(format: MR.strings().new_version_available.localized(), update.latestVersion!) : MR.strings().update_available.localized())
                            .font(.headline)
                        if let current = update.currentVersion {
                            Text("\(MR.strings().version.localized()): \(current)")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                    Spacer()
                    if let urlStr = update.releaseUrl, let url = URL(string: urlStr) {
                        Link(destination: url) {
                            Image(systemName: "arrow.up.right.square")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
            .padding(.vertical, 4)
        }
    }

    private func activityItems(_ overview: DownloadClientSystemOverview) -> [InfoItem] {
        var items: [InfoItem] = [
            InfoItem(label: MR.strings().active_downloads.localized(), value: "\(overview.activeDownloadsCount)")
        ]
        if let transfer = overview.transferInfo {
            items.append(InfoItem(label: MR.strings().download_speed.localized(), value: "\(transfer.downloadSpeed.bytesAsFileSizeString())/s"))
            items.append(InfoItem(label: MR.strings().upload_speed.localized(), value: "\(transfer.uploadSpeed.bytesAsFileSizeString())/s"))
        }
        return items
    }

    @ViewBuilder
    private func activityArea(_ overview: DownloadClientSystemOverview) -> some View {
        let items = activityItems(overview)

        Section {
            VStack(spacing: 12) {
                ForEach(items, id: \.self) { info in
                    HStack(alignment: .center) {
                        Text(info.label)
                            .font(.system(size: 14))
                        Spacer()
                        Text(info.value)
                            .font(.system(size: 14))
                            .foregroundColor(.themePrimary)
                            .lineLimit(1)
                            .truncationMode(.tail)
                            .multilineTextAlignment(.trailing)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }

                    if info != items.last {
                        Divider()
                    }
                }
            }
        } header: {
            Text(MR.strings().activity.localized())
        }
    }

    @ViewBuilder
    private func diskSpaceArea(_ overview: DownloadClientSystemOverview) -> some View {
        if !overview.diskSpaces.isEmpty {
            Section {
                ForEach(overview.diskSpaces) { disk in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(disk.path ?? disk.label ?? MR.strings().unknown.localized())
                                .font(.headline)
                            Spacer()
                            Text(disk.totalSpace > 0 && disk.freeSpace > 0 ? (disk.freeSpace.bytesAsFileSizeString() + " " + MR.strings().free_space.localized()) : MR.strings().unknown.localized())
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }

                        if disk.totalSpace > 0 {
                            ProgressView(value: disk.usedPercentage)
                                .tint(disk.usedPercentage > 0.9 ? .red : .accentColor)

                            HStack {
                                Text(MR.strings().total_space.localized() + ": " + disk.totalSpace.bytesAsFileSizeString())
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text("\(Int(disk.usedPercentage * 100))%")
                                    .font(.caption)
                                    .bold()
                            }
                        }
                    }
                    .padding(.vertical, 4)
                }
            } header: {
                Text(MR.strings().disk_space.localized())
            }
        }
    }

    private func infoItems(_ overview: DownloadClientSystemOverview) -> [InfoItem] {
        var items: [InfoItem] = [
            InfoItem(label: MR.strings().host_endpoint.localized(), value: viewModel.client?.url ?? "")
        ]

        if let version = overview.version {
            items.append(InfoItem(label: MR.strings().version.localized(), value: version))
        }
        if let webApiVersion = overview.webApiVersion {
            items.append(InfoItem(label: MR.strings().web_api_version.localized(), value: webApiVersion))
        }
        if let downloadDir = overview.downloadDir {
            items.append(InfoItem(label: MR.strings().download_directory.localized(), value: downloadDir))
        }
        if let connectionStatus = overview.connectionStatus {
            items.append(InfoItem(label: MR.strings().connection_status.localized(), value: connectionStatus))
        }
        if let listenPort = overview.listenPort {
            items.append(InfoItem(label: MR.strings().listen_port.localized(), value: "\(listenPort)"))
        }
        if let dhtNodes = overview.dhtNodes {
            items.append(InfoItem(label: MR.strings().dht_nodes.localized(), value: "\(dhtNodes)"))
        }
        if let totalPeers = overview.totalPeers {
            items.append(InfoItem(label: MR.strings().active_peers.localized(), value: "\(totalPeers)"))
        }
        if let alltimeDl = overview.alltimeDownloaded {
            items.append(InfoItem(label: MR.strings().alltime_downloaded.localized(), value: alltimeDl.int64Value.bytesAsFileSizeString()))
        }
        if let alltimeUl = overview.alltimeUploaded {
            items.append(InfoItem(label: MR.strings().alltime_uploaded.localized(), value: alltimeUl.int64Value.bytesAsFileSizeString()))
        }
        for pair in overview.extraInfo {
            if let label = pair.first as String?, let value = pair.second as String? {
                items.append(InfoItem(label: label, value: value))
            }
        }
        if let update = overview.updateInfo {
            items.append(InfoItem(label: MR.strings().update_available.localized(), value: update.isAvailable ? MR.strings().yes.localized() : MR.strings().no.localized()))
        }

        return items
    }

    @ViewBuilder
    private func infoArea(_ overview: DownloadClientSystemOverview) -> some View {
        let items = infoItems(overview)

        Section {
            VStack(spacing: 12) {
                ForEach(items, id: \.self) { info in
                    HStack(alignment: .center) {
                        Text(info.label)
                            .font(.system(size: 14))
                        Spacer()
                        Text(info.value)
                            .font(.system(size: 14))
                            .foregroundColor(.themePrimary)
                            .lineLimit(1)
                            .truncationMode(.tail)
                            .multilineTextAlignment(.trailing)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }

                    if info != items.last {
                        Divider()
                    }
                }
            }
        } header: {
            Text(MR.strings().system_info.localized())
        }
    }

    private var loadingArea: some View {
        ZStack {
            ProgressView()
                .progressViewStyle(.circular)
        }
    }

    @ViewBuilder
    private func errorArea(_ error: DownloadClientDashboardUiStateError) -> some View {
        ZStack {
            ErrorView(
                errorType: error.type,
                message: error.message ?? "",
                onOpenSettings: {
                    navigationManager.settingsPath.append(SettingsRoute.editDownloadClient(id))
                },
                onRetry: {
                    viewModel.refresh()
                }
            )
        }
    }
}
