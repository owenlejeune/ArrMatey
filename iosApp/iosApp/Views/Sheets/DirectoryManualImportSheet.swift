import Shared
import SwiftUI

struct DirectoryManualImportSheet: View {
    @ObservedObject var viewModel: ActivityQueueViewModelS
    let instances: [Instance]
    let initialInstanceId: Int64?
    let onDismiss: () -> Void
    let onSuccess: () -> Void

    @State private var selectedInstanceId: Int64
    @State private var folderPath: String = ""
    @State private var importMode: ManualImportMode = .move
    @State private var filterExisting: Bool = true
    @State private var showFilesystemBrowser: Bool = false
    @State private var isWorking: Bool = false
    @State private var errorMessage: String? = nil
    @State private var scannedFiles: [ManualImportFile] = []
    @State private var showInteractiveImportSheet: Bool = false

    init(
        viewModel: ActivityQueueViewModelS,
        instances: [Instance],
        initialInstanceId: Int64? = nil,
        onDismiss: @escaping () -> Void,
        onSuccess: @escaping () -> Void
    ) {
        self.viewModel = viewModel
        self.instances = instances
        self.initialInstanceId = initialInstanceId
        self.onDismiss = onDismiss
        self.onSuccess = onSuccess
        _selectedInstanceId = State(initialValue: initialInstanceId ?? instances.first?.id ?? 0)
    }

    private var selectedInstance: Instance? {
        instances.first(where: { $0.id == selectedInstanceId })
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Text(MR.strings().manual_import_directory_description.localized())
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .listRowBackground(Color.clear)
                .listRowInsets(EdgeInsets(top: 0, leading: 0, bottom: 0, trailing: 0))

                Section(header: Text(MR.strings().service.localized())) {
                    if instances.count > 1 {
                        Picker(MR.strings().service.localized(), selection: $selectedInstanceId) {
                            ForEach(instances, id: \.id) { inst in
                                Text(inst.label).tag(inst.id)
                            }
                        }
                    } else if let inst = selectedInstance {
                        Text(inst.label)
                    }
                }

                Section(header: Text(MR.strings().folder_path.localized())) {
                    HStack {
                        TextField(MR.strings().folder_path_placeholder.localized(), text: $folderPath)
                            .autocorrectionDisabled()
                            .textInputAutocapitalization(.never)

                        Button {
                            showFilesystemBrowser = true
                        } label: {
                            Image(systemName: "folder")
                                .foregroundStyle(Color.themePrimary)
                        }
                        .buttonStyle(.borderless)
                    }
                }

                Section(header: Text(MR.strings().import_mode.localized())) {
                    Picker(MR.strings().import_mode.localized(), selection: $importMode) {
                        Text(ManualImportMode.move.label.localized()).tag(ManualImportMode.move)
                        Text(ManualImportMode.theCopy.label.localized()).tag(ManualImportMode.theCopy)
                    }
                    .pickerStyle(.segmented)
                }

                Section {
                    Toggle(isOn: $filterExisting) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(MR.strings().filter_existing_files.localized())
                                .font(.body)
                            Text(MR.strings().filter_existing_files_description.localized())
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }

                if let error = errorMessage {
                    Section {
                        Text(error)
                            .foregroundStyle(.red)
                            .font(.footnote)
                    }
                }
            }
            .navigationTitle(MR.strings().manual_import.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        onDismiss()
                    }
                    .disabled(isWorking)
                }

                ToolbarItem(placement: .confirmationAction) {
                    if isWorking {
                        HStack(spacing: 6) {
                            ProgressView()
                                .progressViewStyle(.circular)
                            Text(MR.strings().interactive_import.localized())
                                .foregroundColor(.secondary)
                        }
                    } else {
                        Button(MR.strings().interactive_import.localized()) {
                            performScan()
                        }
                        .disabled(folderPath.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    }
                }
            }
            .sheet(isPresented: $showFilesystemBrowser) {
                RemoteFilesystemBrowserSheet(
                    viewModel: viewModel,
                    instanceId: selectedInstanceId,
                    initialPath: folderPath.isEmpty ? nil : folderPath,
                    onSelectPath: { path in
                        folderPath = path
                        showFilesystemBrowser = false
                    },
                    onDismiss: {
                        showFilesystemBrowser = false
                    }
                )
            }
            .sheet(isPresented: $showInteractiveImportSheet) {
                InteractiveImportSheet(
                    instance: selectedInstance,
                    folderPath: folderPath,
                    importMode: importMode,
                    initialFiles: scannedFiles,
                    viewModel: viewModel,
                    onDismiss: {
                        showInteractiveImportSheet = false
                    },
                    onImportSuccess: {
                        showInteractiveImportSheet = false
                        onSuccess()
                        onDismiss()
                    }
                )
            }
        }
    }

    private func performScan() {
        let trimmed = folderPath.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            errorMessage = MR.strings().error_enter_folder_path.localized()
            return
        }

        isWorking = true
        errorMessage = nil

        viewModel.scanManualImportFolder(
            instanceId: selectedInstanceId,
            folderPath: trimmed,
            filterExisting: filterExisting,
            onSuccess: { files in
                isWorking = false
                scannedFiles = files
                showInteractiveImportSheet = true
            },
            onError: { msg in
                isWorking = false
                errorMessage = msg
            }
        )
    }
}
