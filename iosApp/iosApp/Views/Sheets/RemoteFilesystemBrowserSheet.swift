import Shared
import SwiftUI

struct RemoteFilesystemBrowserSheet: View {
    @ObservedObject var viewModel: ActivityQueueViewModelS
    let instanceId: Int64
    let initialPath: String?
    let onSelectPath: (String) -> Void
    let onDismiss: () -> Void

    @State private var currentPath: String

    init(
        viewModel: ActivityQueueViewModelS,
        instanceId: Int64,
        initialPath: String? = nil,
        onSelectPath: @escaping (String) -> Void,
        onDismiss: @escaping () -> Void
    ) {
        self.viewModel = viewModel
        self.instanceId = instanceId
        self.initialPath = initialPath
        self.onSelectPath = onSelectPath
        self.onDismiss = onDismiss
        _currentPath = State(initialValue: initialPath ?? "/")
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack(spacing: 8) {
                    Image(systemName: "display")
                        .foregroundStyle(Color.themePrimary)
                    Text(currentPath.isEmpty ? "/" : currentPath)
                        .font(.system(size: 13, design: .monospaced))
                        .lineLimit(1)
                        .truncationMode(.middle)
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(uiColor: .secondarySystemBackground))

                Divider()

                if viewModel.isFilesystemLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if let error = viewModel.filesystemError {
                    Spacer()
                    VStack(spacing: 12) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.largeTitle)
                            .foregroundStyle(.secondary)
                        Text(error)
                            .font(.subheadline)
                            .foregroundStyle(.red)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                        Button(MR.strings().retry.localized()) {
                            viewModel.loadFilesystem(instanceId: instanceId, path: currentPath)
                        }
                        .buttonStyle(.bordered)
                    }
                    Spacer()
                } else {
                    List {
                        if !currentPath.isEmpty && currentPath != "/" {
                            Button {
                                navigateToParent()
                            } label: {
                                HStack(spacing: 12) {
                                    Image(systemName: "arrow.up.circle.fill")
                                        .foregroundStyle(Color.themePrimary)
                                    Text(MR.strings().parent_folder.localized())
                                        .font(.body.weight(.medium))
                                    Spacer()
                                }
                            }
                            .foregroundStyle(.primary)
                        }

                        let directories = viewModel.filesystemItems.filter { $0.isDirectory }
                        ForEach(directories, id: \.path) { item in
                            Button {
                                currentPath = item.path
                                viewModel.loadFilesystem(instanceId: instanceId, path: item.path)
                            } label: {
                                HStack(spacing: 12) {
                                    Image(systemName: "folder.fill")
                                        .foregroundStyle(Color.themePrimary)
                                    Text(item.name)
                                        .font(.body)
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .font(.caption)
                                        .foregroundStyle(.tertiary)
                                }
                            }
                            .foregroundStyle(.primary)
                        }
                    }
                    .listStyle(.plain)
                }

                Divider()

                HStack(spacing: 12) {
                    Button(MR.strings().cancel.localized()) {
                        onDismiss()
                    }
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity)

                    Button(MR.strings().select_folder.localized()) {
                        onSelectPath(currentPath)
                        onDismiss()
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)
                }
                .padding(16)
                .background(Color(uiColor: .systemBackground))
            }
            .navigationTitle(MR.strings().browse_folder_title.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        onDismiss()
                    }
                }
            }
            .onAppear {
                viewModel.loadFilesystem(instanceId: instanceId, path: currentPath)
            }
        }
    }

    private func navigateToParent() {
        let parentFromItem = viewModel.filesystemItems.first?.parent?.trimmingCharacters(in: .whitespacesAndNewlines)
        if let p = parentFromItem, !p.isEmpty {
            currentPath = p
            viewModel.loadFilesystem(instanceId: instanceId, path: p)
            return
        }

        var trimmed = currentPath
        if trimmed.hasSuffix("/") && trimmed.count > 1 {
            trimmed.removeLast()
        }
        if let lastSlash = trimmed.lastIndex(of: "/") {
            let parent = String(trimmed[..<lastSlash])
            currentPath = parent.isEmpty ? "/" : parent
        } else {
            currentPath = "/"
        }
        viewModel.loadFilesystem(instanceId: instanceId, path: currentPath)
    }
}
