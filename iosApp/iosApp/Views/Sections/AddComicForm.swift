//
//  AddComicForm.swift
//  iosApp
//

import Shared
import SwiftUI

struct AddComicForm: View {
    let volume: ComicVolume
    let addItemStatus: OperationStatus
    let rootFolders: [RootFolder]
    let preferences: InstancePreferences
    let onUpdatePreferences: (InstancePreferences) -> Void
    let onAddItem: (ArrMedia, Bool) -> Void
    let onDismiss: () -> Void
    let instances: [Instance]
    let selectedInstance: Instance?
    let onInstanceSelected: (Instance) -> Void

    @State private var monitorVolume: Bool
    @State private var monitorNewIssues: Bool
    @State private var selectedMonitoringScheme: MonitoringScheme
    @State private var selectedSpecialVersion: SpecialVersion
    @State private var volumeFolder: String
    @State private var selectedRootFolderId: Int32?
    @State private var searchOnAdd: Bool

    private var selectedRootFolder: RootFolder? {
        rootFolders.first { $0.id == selectedRootFolderId }
    }

    private var isLoading: Bool {
        addItemStatus is OperationStatusInProgress
    }

    init(
        volume: ComicVolume,
        addItemStatus: OperationStatus,
        rootFolders: [RootFolder],
        preferences: InstancePreferences,
        onUpdatePreferences: @escaping (InstancePreferences) -> Void,
        onAddItem: @escaping (ArrMedia, Bool) -> Void,
        onDismiss: @escaping () -> Void,
        instances: [Instance] = [],
        selectedInstance: Instance? = nil,
        onInstanceSelected: @escaping (Instance) -> Void = { _ in }
    ) {
        self.volume = volume
        self.addItemStatus = addItemStatus
        self.rootFolders = rootFolders
        self.preferences = preferences
        self.onUpdatePreferences = onUpdatePreferences
        self.onAddItem = onAddItem
        self.onDismiss = onDismiss
        self.instances = instances
        self.selectedInstance = selectedInstance
        self.onInstanceSelected = onInstanceSelected

        self._monitorVolume = State(initialValue: preferences.addKapowarrMonitorVolume)
        self._monitorNewIssues = State(initialValue: preferences.addKapowarrMonitorNewIssues)
        self._selectedMonitoringScheme = State(initialValue: preferences.addKapowarrMonitoringScheme)
        self._selectedSpecialVersion = State(initialValue: preferences.addKapowarrSpecialVersion)
        self._searchOnAdd = State(initialValue: preferences.addSearchOnAdd)

        let volNum = volume.volumeNumber.map { $0.intValue < 10 ? "0\($0.intValue)" : "\($0.intValue)" } ?? "01"
        let fallbackLabel = MR.strings().type_volume.localized()
        let defaultFolder = volume.folder ?? "\(volume.title ?? fallbackLabel)/\(fallbackLabel) \(volNum)\(volume.year.flatMap { " (\($0))" } ?? "")"
        self._volumeFolder = State(initialValue: defaultFolder)

        let rf = rootFolders.first(where: { $0.path == preferences.addRootFolderPath }) ?? rootFolders.first(where: { $0.isDefault }) ?? rootFolders.first
        self._selectedRootFolderId = State(initialValue: rf?.id)
    }

    var body: some View {
        NavigationStack {
            content
                .toolbar {
                    toolbarButtons
                }
                .onChange(of: rootFolders, initial: true) {
                    if !rootFolders.isEmpty && selectedRootFolderId == nil {
                        selectedRootFolderId = rootFolders.first(where: { $0.isDefault })?.id ?? rootFolders[0].id
                    }
                }
        }
    }

    @ViewBuilder
    private var content: some View {
        Form {
            if instances.count > 1, let selectedInstance = selectedInstance {
                Section {
                    Picker(MR.strings().instances.localized(), selection: Binding(
                        get: { selectedInstance },
                        set: { onInstanceSelected($0) }
                    )) {
                        ForEach(instances, id: \.id) { instance in
                            Text(instance.label).tag(instance)
                        }
                    }
                }
            }

            Section {
                if !rootFolders.isEmpty {
                    Picker(MR.strings().root_folder.localized(), selection: $selectedRootFolderId) {
                        ForEach(rootFolders, id: \.self) { folder in
                            Text("\(folder.path)\(folder.isDefault ? " (\(MR.strings().default_label.localized()))" : "")")
                                .tag(folder.id)
                        }
                    }
                }

                TextField(MR.strings().volume_folder.localized(), text: $volumeFolder)

                Toggle(MR.strings().monitor_volume.localized(), isOn: $monitorVolume)

                Toggle(MR.strings().monitor_new_issues.localized(), isOn: $monitorNewIssues)

                Picker(MR.strings().monitoring_scheme.localized(), selection: $selectedMonitoringScheme) {
                    ForEach(MonitoringScheme.allCases, id: \.self) { scheme in
                        Text(scheme.resource.localized()).tag(scheme)
                    }
                }

                Picker(MR.strings().special_version.localized(), selection: $selectedSpecialVersion) {
                    ForEach(SpecialVersion.allCases, id: \.self) { version in
                        Text(version.resource.localized()).tag(version)
                    }
                }

                Toggle(MR.strings().search_missing_volume.localized(), isOn: $searchOnAdd)
            }
        }
    }

    @ToolbarContentBuilder
    private var toolbarButtons: some ToolbarContent {
        ToolbarItem(placement: .cancellationAction) {
            Button {
                onDismiss()
            } label: {
                Label(MR.strings().cancel.localized(), systemImage: "xmark")
            }
            .tint(.primary)
        }

        ToolbarItem(placement: .primaryAction) {
            Button {
                Task {
                    if let rf = selectedRootFolder {
                        onUpdatePreferences(
                            preferences.doCopyWithKapowarrAddDefaults(
                                monitorVolume: monitorVolume,
                                monitorNewIssues: monitorNewIssues,
                                monitoringScheme: selectedMonitoringScheme,
                                specialVersion: selectedSpecialVersion,
                                rootFolderPath: rf.path,
                                searchOnAdd: searchOnAdd
                            )
                        )
                        let newVolume = volume.doCopyForCreation(
                            monitored: monitorVolume,
                            monitorNewIssues: monitorNewIssues,
                            folder: volumeFolder,
                            volumeFolder: volumeFolder,
                            specialVersion: selectedSpecialVersion.value,
                            monitoringScheme: selectedMonitoringScheme,
                            rootFolder: rf.id.asKotlinInt,
                            searchOnAdd: searchOnAdd
                        )
                        onAddItem(newVolume, searchOnAdd)
                    }
                }
            } label: {
                if (isLoading) {
                    ProgressView().tint(nil)
                } else {
                    Label(MR.strings().save.localized(), systemImage: "checkmark")
                }
            }
            .disabled(isLoading || volumeFolder.isEmpty || selectedRootFolder == nil)
        }
    }
}
