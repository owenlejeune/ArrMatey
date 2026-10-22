import Shared
import SwiftUI

struct ArrInstanceDashboard: View {
    private let id: Int64

    @StateObject private var viewModel: ArrInstanceDashboardViewModelS
    @StateObject private var activityQueueViewModel = ActivityQueueViewModelS()
    @State private var showManualImportSheet: Bool = false

    @EnvironmentObject private var navigationManager: NavigationManager

    init(id: Int64) {
        self.id = id
        _viewModel = StateObject(wrappedValue: ArrInstanceDashboardViewModelS(id))
    }

    var body: some View {
        contentForState()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .toolbar {
                ToolbarItemGroup(placement: .topBarTrailing) {
                    NavigationLink(value: SettingsRoute.editInstance(id)) {
                        Image(systemName: "pencil")
                    }

                    InstanceOptionsMenu(
                        instanceUrl: viewModel.instance?.url,
                        onRunRssSync: { viewModel.runRssSync() },
                        onSearchAllMissing: { viewModel.searchAllMissing() },
                        onManualImport: (viewModel.instance?.supportsManualImport == true) ? {
                            showManualImportSheet = true
                        } : nil,
                        onUpdateLibrary: { viewModel.updateLibrary() },
                        onBackupDatabase: { viewModel.backupDatabase() }
                    ) {
                        Image(systemName: "ellipsis")
                    }
                }
            }
            .sheet(isPresented: $showManualImportSheet) {
                if let instance = viewModel.instance {
                    DirectoryManualImportSheet(
                        viewModel: activityQueueViewModel,
                        instances: [instance],
                        initialInstanceId: instance.id,
                        onDismiss: { showManualImportSheet = false },
                        onSuccess: { viewModel.refresh() }
                    )
                }
            }
    }

    @ViewBuilder
    private func contentForState() -> some View {
        let state = viewModel.state

        if state is ArrDashboardStateInitial {
            ZStack { EmptyView() }
        } else if state is ArrDashboardStateLoading {
            loadingArea
        } else if let error = state as? ArrDashboardStateError {
            errorArea(error)
        } else if let success = state as? ArrDashboardStateSuccess {
            successArea(success)
        } else {
            ZStack { EmptyView() }
        }
    }

    @ViewBuilder
    private func successArea(_ state: ArrDashboardStateSuccess) -> some View {
        ZStack {
            Form {
                if let update = state.updateInfo, update.isAvailable {
                    updateArea(update)
                }
                if state.softwareStatus != nil || state.prowlarrOverview != nil || !state.healthItems.isEmpty {
                    healthArea(state)
                }
                diskSpaceArea(state)
                prowlarrArea(state)
                bazarrArea(state)
                tautulliArea(state)
                tracearrArea(state)
                seerrArea(state)
                if state.softwareStatus != nil {
                    infoArea(state)
                }
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

    @ViewBuilder
    private func healthArea(_ state: ArrDashboardStateSuccess) -> some View {
        Section {
            ForEach(state.healthItems) { health in
                ArrHealthCard(health: health)
            }
        } header: {
            Text(MR.strings().health.localized())
        }
    }

    @ViewBuilder
    private func diskSpaceArea(_ state: ArrDashboardStateSuccess) -> some View {
        if !state.disks.isEmpty {
            Section {
                ForEach(state.disks) { disk in
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

    @ViewBuilder
    private func prowlarrArea(_ state: ArrDashboardStateSuccess) -> some View {
        if let prowlarr = state.prowlarrOverview {
            let items: [InfoItem] = [
                InfoItem(label: MR.strings().total.localized(), value: "\(prowlarr.totalIndexers)"),
                InfoItem(label: MR.strings().healthy_indexers.localized(), value: "\(prowlarr.healthyIndexers)"),
                InfoItem(label: MR.strings().failing_indexers.localized(), value: "\(prowlarr.failingIndexers)")
            ]
            infoSection(title: MR.strings().indexers.localized(), items: items)
        }
    }

    private func bazarrSystemItems(_ bazarr: BazarrOverview) -> [InfoItem] {
        var systemItems: [InfoItem] = [
            InfoItem(label: MR.strings().host_endpoint.localized(), value: viewModel.instance?.url ?? "")
        ]
        if let version = bazarr.version { systemItems.append(InfoItem(label: MR.strings().version.localized(), value: version)) }
        if let pkg = bazarr.packageVersion { systemItems.append(InfoItem(label: MR.strings().package_version.localized(), value: pkg)) }
        if let os = bazarr.operatingSystem { systemItems.append(InfoItem(label: MR.strings().operating_system.localized(), value: os)) }
        if let py = bazarr.pythonVersion { systemItems.append(InfoItem(label: MR.strings().python_version.localized(), value: py)) }
        if let cpu = bazarr.cpuCores { systemItems.append(InfoItem(label: MR.strings().cpu_cores.localized(), value: "\(cpu)")) }
        if let tz = bazarr.timezone { systemItems.append(InfoItem(label: MR.strings().timezone.localized(), value: tz)) }
        if let dir = bazarr.bazarrDirectory { systemItems.append(InfoItem(label: MR.strings().startup_path.localized(), value: dir)) }
        if let cfg = bazarr.configDirectory { systemItems.append(InfoItem(label: MR.strings().app_data_path.localized(), value: cfg)) }
        return systemItems
    }

    private func tautulliStreamItems(_ tautulli: TautulliOverview) -> [InfoItem] {
        var streamItems: [InfoItem] = [
            InfoItem(label: MR.strings().active_streams.localized(), value: "\(tautulli.streamCount)")
        ]
        if let totalBw = tautulli.totalBandwidth {
            streamItems.append(InfoItem(label: MR.strings().total_bandwidth.localized(), value: "\(totalBw.int64Value / 1000) Mbps"))
        }
        if let lanBw = tautulli.lanBandwidth {
            streamItems.append(InfoItem(label: MR.strings().lan_bandwidth.localized(), value: "\(lanBw.int64Value / 1000) Mbps"))
        }
        if let wanBw = tautulli.wanBandwidth {
            streamItems.append(InfoItem(label: MR.strings().wan_bandwidth.localized(), value: "\(wanBw.int64Value / 1000) Mbps"))
        }
        return streamItems
    }

    private func tautulliSystemItems(_ tautulli: TautulliOverview) -> [InfoItem] {
        var systemItems: [InfoItem] = [
            InfoItem(label: MR.strings().host_endpoint.localized(), value: viewModel.instance?.url ?? "")
        ]
        if let v = tautulli.tautulliVersion { systemItems.append(InfoItem(label: MR.strings().version.localized(), value: v)) }
        if let inst = tautulli.installType { systemItems.append(InfoItem(label: MR.strings().install_type.localized(), value: inst)) }
        if let pms = tautulli.pmsName { systemItems.append(InfoItem(label: MR.strings().plex_server.localized(), value: pms)) }
        if let pmsV = tautulli.pmsVersion { systemItems.append(InfoItem(label: MR.strings().plex_server_version.localized(), value: pmsV)) }
        systemItems.append(InfoItem(label: MR.strings().update_available.localized(), value: tautulli.updateAvailable ? MR.strings().yes.localized() : MR.strings().no.localized()))
        return systemItems
    }

    @ViewBuilder
    private func bazarrArea(_ state: ArrDashboardStateSuccess) -> some View {
        if let bazarr = state.bazarrOverview {
            let subtitleItems: [InfoItem] = [
                InfoItem(label: MR.strings().wanted_episodes.localized(), value: "\(bazarr.wantedEpisodes)"),
                InfoItem(label: MR.strings().wanted_movies.localized(), value: "\(bazarr.wantedMovies)"),
                InfoItem(label: MR.strings().provider_issues.localized(), value: "\(bazarr.providerIssues)")
            ]
            infoSection(title: MR.strings().wanted_subtitles.localized(), items: subtitleItems)
            infoSection(title: MR.strings().system_info.localized(), items: bazarrSystemItems(bazarr))
        }
    }

    @ViewBuilder
    private func tautulliArea(_ state: ArrDashboardStateSuccess) -> some View {
        if let tautulli = state.tautulliOverview {
            infoSection(title: MR.strings().active_streams.localized(), items: tautulliStreamItems(tautulli))
            infoSection(title: MR.strings().system_info.localized(), items: tautulliSystemItems(tautulli))
        }
    }

    private func tracearrSystemItems(_ tracearr: TracearrOverview) -> [InfoItem] {
        var systemItems: [InfoItem] = [
            InfoItem(label: MR.strings().host_endpoint.localized(), value: viewModel.instance?.url ?? "")
        ]
        if let v = tracearr.version { systemItems.append(InfoItem(label: MR.strings().version.localized(), value: v)) }
        if let tag = tracearr.commitTag { systemItems.append(InfoItem(label: MR.strings().commit_tag.localized(), value: tag)) }
        systemItems.append(InfoItem(label: MR.strings().update_available.localized(), value: tracearr.updateAvailable ? MR.strings().yes.localized() : MR.strings().no.localized()))
        return systemItems
    }

    private func seerrSystemItems(_ seerr: SeerrOverview) -> [InfoItem] {
        var systemItems: [InfoItem] = [
            InfoItem(label: MR.strings().host_endpoint.localized(), value: viewModel.instance?.url ?? "")
        ]
        if let v = seerr.version { systemItems.append(InfoItem(label: MR.strings().version.localized(), value: v)) }
        if let tag = seerr.commitTag { systemItems.append(InfoItem(label: MR.strings().commit_tag.localized(), value: tag)) }
        systemItems.append(InfoItem(label: MR.strings().update_available.localized(), value: seerr.updateAvailable ? MR.strings().yes.localized() : MR.strings().no.localized()))
        return systemItems
    }

    @ViewBuilder
    private func tracearrArea(_ state: ArrDashboardStateSuccess) -> some View {
        if let tracearr = state.tracearrOverview {
            let overviewItems: [InfoItem] = [
                InfoItem(label: MR.strings().connected_servers.localized(), value: "\(tracearr.servers.count)"),
                InfoItem(label: MR.strings().active_streams.localized(), value: "\(tracearr.activeStreams.count)")
            ]
            infoSection(title: MR.strings().dashboard_tracearr_overview.localized(), items: overviewItems)

            if let stats = tracearr.todayStats {
                let todayItems: [InfoItem] = [
                    InfoItem(label: MR.strings().today_plays.localized(), value: "\(stats.todayPlays)"),
                    InfoItem(label: MR.strings().watch_time.localized(), value: stats.formattedWatchTime),
                    InfoItem(label: MR.strings().active_users.localized(), value: "\(stats.activeUsersToday)")
                ]
                infoSection(title: MR.strings().today_stats.localized(), items: todayItems)
            }

            infoSection(title: MR.strings().system_info.localized(), items: tracearrSystemItems(tracearr))
        }
    }

    @ViewBuilder
    private func seerrArea(_ state: ArrDashboardStateSuccess) -> some View {
        if let seerr = state.seerrOverview {
            let requestItems: [InfoItem] = [
                InfoItem(label: MR.strings().total.localized(), value: "\(seerr.totalRequests)"),
                InfoItem(label: MR.strings().pending_requests.localized(), value: "\(seerr.pendingRequests)"),
                InfoItem(label: MR.strings().approved_requests.localized(), value: "\(seerr.approvedRequests)"),
                InfoItem(label: MR.strings().processing.localized(), value: "\(seerr.processingRequests)"),
                InfoItem(label: MR.strings().available.localized(), value: "\(seerr.availableRequests)")
            ]
            infoSection(title: MR.strings().requests_label.localized(), items: requestItems)

            let issueItems: [InfoItem] = [
                InfoItem(label: MR.strings().total.localized(), value: "\(seerr.totalIssues)"),
                InfoItem(label: MR.strings().open_issues.localized(), value: "\(seerr.openIssues)")
            ]
            infoSection(title: MR.strings().issues.localized(), items: issueItems)

            infoSection(title: MR.strings().system_info.localized(), items: seerrSystemItems(seerr))
        }
    }

    private func infoItems(state: ArrDashboardStateSuccess, instance: Instance?) -> [InfoItem] {
        var items = [
            InfoItem(label: MR.strings().host_endpoint.localized(), value: instance?.url ?? ""),
            InfoItem(label: MR.strings().version.localized(), value: state.softwareStatus?.version ?? MR.strings().unknown.localized()),
            InfoItem(label: MR.strings().startup_path.localized(), value: state.softwareStatus?.startupPath ?? MR.strings().unknown.localized()),
            InfoItem(label: MR.strings().app_data_path.localized(), value: state.softwareStatus?.appData ?? MR.strings().unknown.localized()),
            InfoItem(label: MR.strings().host_platform.localized(), value: state.softwareStatus?.hostPlatform.localized() ?? MR.strings().unknown.localized()),
            InfoItem(label: MR.strings().host_os.localized(), value: state.softwareStatus?.hostOs ?? MR.strings().unknown.localized())
        ]
        if let update = state.updateInfo {
            items.append(InfoItem(label: MR.strings().update_available.localized(), value: update.isAvailable ? MR.strings().yes.localized() : MR.strings().no.localized()))
        }
        return items
    }

    @ViewBuilder
    private func infoArea(_ state: ArrDashboardStateSuccess) -> some View {
        let items = infoItems(state: state, instance: viewModel.instance)
        infoSection(title: MR.strings().system_info.localized(), items: items)
    }

    @ViewBuilder
    private func infoSection(title: String, items: [InfoItem]) -> some View {
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
            Text(title)
        }
    }

    private var loadingArea: some View {
        ZStack {
            ProgressView()
                .progressViewStyle(.circular)
        }
    }

    @ViewBuilder
    private func errorArea(_ error: ArrDashboardStateError) -> some View {
        ZStack {
            ErrorView(
                errorType: error.type,
                message: error.message ?? "",
                onOpenSettings: {
                    if let instance = viewModel.instance {
                        navigationManager.maybeEditInstance(of: instance.type, instance)
                    }
                },
                onRetry: {}
            )
        }
    }
}
