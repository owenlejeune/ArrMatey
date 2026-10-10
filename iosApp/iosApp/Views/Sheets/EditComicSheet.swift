//
//  EditComicSheet.swift
//  iosApp
//

import SwiftUI
import Shared

struct EditComicSheet: View {
    let item: ComicVolume
    let rootFolders: [RootFolder]
    let editInProgress: Bool
    let onEditItem: (ArrMedia) -> Void

    @Environment(\.dismiss) var dismiss

    @State private var monitored: Bool
    @State private var monitorNewIssues: Bool
    @State private var selectedMonitoringScheme: MonitoringScheme
    @State private var selectedSpecialVersion: SpecialVersion
    @State private var volumeFolder: String
    @State private var rootFolderId: Int32?

    init(item: ComicVolume, rootFolders: [RootFolder], editInProgress: Bool, onEditItem: @escaping (ArrMedia) -> Void) {
        self.item = item
        self.rootFolders = rootFolders
        self.editInProgress = editInProgress
        self.onEditItem = onEditItem

        self.monitored = item.monitored
        self.monitorNewIssues = item.monitorNewIssues
        self.selectedMonitoringScheme = item.monitoringScheme ?? .all
        self.selectedSpecialVersion = SpecialVersion.allCases.first(where: { $0.value == item.specialVersion }) ?? .automatic
        self.volumeFolder = item.volumeFolder ?? item.folder ?? ""
        self.rootFolderId = item.rootFolder?.int32Value ?? rootFolders.first?.id
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Toggle(MR.strings().monitor_volume.localized(), isOn: $monitored)
                    Toggle(MR.strings().monitor_new_issues.localized(), isOn: $monitorNewIssues)

                    Picker(MR.strings().monitoring_scheme.localized(), selection: $selectedMonitoringScheme) {
                        ForEach(MonitoringScheme.allCases, id: \.self) { scheme in
                            Text(scheme.resource.localized()).tag(scheme)
                        }
                    }

                    if rootFolders.count > 0 {
                        Picker(MR.strings().root_folder.localized(), selection: $rootFolderId) {
                            ForEach(rootFolders, id: \.id) { folder in
                                Text(folder.path).tag(folder.id as Int32?)
                            }
                        }
                    }

                    TextField(MR.strings().volume_folder.localized(), text: $volumeFolder)

                    Picker(MR.strings().special_version.localized(), selection: $selectedSpecialVersion) {
                        ForEach(SpecialVersion.allCases, id: \.self) { version in
                            Text(version.resource.localized()).tag(version)
                        }
                    }
                }
            }
            .toolbarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigation) {
                    Button {
                        dismiss()
                    } label: {
                        Label(MR.strings().close.localized(), systemImage: "xmark")
                            .foregroundStyle(.white)
                    }
                    .tint(nil)
                }

                ToolbarItem(placement: .primaryAction) {
                    Button {
                        let updatedItem = item.doCopyForUpdate(
                            monitored: monitored,
                            monitorNewIssues: monitorNewIssues,
                            folder: volumeFolder,
                            volumeFolder: volumeFolder,
                            specialVersion: selectedSpecialVersion.value,
                            monitoringScheme: selectedMonitoringScheme,
                            rootFolder: rootFolderId?.asKotlinInt
                        )
                        onEditItem(updatedItem)
                    } label: {
                        if editInProgress {
                            ProgressView()
                                .progressViewStyle(.circular)
                                .foregroundStyle(.white)
                        } else {
                            Label(MR.strings().save.localized(), systemImage: "checkmark")
                                .foregroundStyle(.white)
                        }
                    }
                    .tint(nil)
                }
            }
        }
    }
}
