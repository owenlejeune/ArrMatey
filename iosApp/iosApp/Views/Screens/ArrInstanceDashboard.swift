//
//  ArrInstanceDashboard.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-20.
//

import Shared
import SwiftUI

struct ArrInstanceDashboard: View {
    private let id: Int64
    
    @ObservedObject private var viewModel: ArrInstanceDashboardViewModelS
    
    @EnvironmentObject private var navigationManager: NavigationManager
    
    init(id: Int64) {
        self.id = id
        self.viewModel = ArrInstanceDashboardViewModelS(id)
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
                        onUpdateLibrary: { viewModel.updateLibrary() },
                        onBackupDatabase: { viewModel.backupDatabase() }
                    ) {
                        Image(systemName: "ellipsis")
                    }
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
                healthArea(state)
                diskSpaceArea(state)
                infoArea(state)
            }
        }
        .refreshable {
            viewModel.refresh()
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
        Section {
            ForEach(state.disks) { disk in
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text(disk.path ?? MR.strings().unknown.localized())
                            .font(.headline)
                        Spacer()
                        Text(disk.freeSpace.bytesAsFileSizeString() + " " + MR.strings().free_space.localized())
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    
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
                .padding(.vertical, 4)
            }
        } header: {
            Text(MR.strings().disk_space.localized())
        }
    }
    
    private func infoItems(state: ArrDashboardStateSuccess, instance: Instance?) -> [InfoItem] {
        [
            InfoItem(label: MR.strings().host_endpoint.localized(), value: instance?.url ?? ""),
            InfoItem(label: MR.strings().version.localized(), value: state.softwareStatus?.version ?? MR.strings().unknown.localized()),
            InfoItem(label: MR.strings().startup_path.localized(), value: state.softwareStatus?.startupPath ?? MR.strings().unknown.localized()),
            InfoItem(label: MR.strings().app_data_path.localized(), value: state.softwareStatus?.appData ?? MR.strings().unknown.localized()),
            InfoItem(label: MR.strings().host_platform.localized(), value: state.softwareStatus?.hostPlatform.localized() ?? MR.strings().unknown.localized()),
            InfoItem(label: MR.strings().host_os.localized(), value: state.softwareStatus?.hostOs ?? MR.strings().unknown.localized())
        ]
    }
    
    @ViewBuilder
    private func infoArea(_ state: ArrDashboardStateSuccess) -> some View {
        let items = infoItems(state: state, instance: viewModel.instance)
        
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
