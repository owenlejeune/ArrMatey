//
//  ManualImportSheet.swift
//  iosApp
//

import Shared
import SwiftUI

struct ManualImportSheet: View {
    let item: QueueItem
    let onDismiss: () -> Void

    @StateObject private var viewModel: ManualImportViewModelS
    @Environment(\.dismiss) private var dismiss

    init(item: QueueItem, onDismiss: @escaping () -> Void) {
        self.item = item
        self.onDismiss = onDismiss
        _viewModel = StateObject(wrappedValue: ManualImportViewModelS(item: item))
    }

    var body: some View {
        ManualImportSheetContent(
            files: viewModel.files,
            selectedIds: viewModel.selectedIds,
            isLoading: viewModel.isLoading,
            isWorking: viewModel.isWorking,
            error: viewModel.error,
            onToggleFile: { viewModel.toggleFileSelected($0) },
            onImport: { viewModel.importFiles() },
            onClearError: { viewModel.clearError() }
        )
        .task {
            viewModel.loadFiles()
        }
        .onChange(of: viewModel.importSuccess) { _, success in
            if success {
                onDismiss()
                dismiss()
            }
        }
    }
}

struct ManualImportSheetContent: View {
    let files: [ManualImportFile]
    let selectedIds: Set<String>
    let isLoading: Bool
    let isWorking: Bool
    let error: String?
    let onToggleFile: (ManualImportFile) -> Void
    let onImport: () -> Void
    let onClearError: () -> Void

    var body: some View {
        List(files, id: \.stableId) { file in
            ManualImportRow(
                file: file,
                isSelected: selectedIds.contains(file.stableId),
                onToggle: { onToggleFile(file) }
            )
        }
        #if os(macOS)
            .frame(minHeight: 200)
        #endif
        .listStyle(.plain)
        .navigationTitle(MR.strings().manual_import.localized())
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .confirmationAction) {
                Button(action: onImport) {
                    if isWorking {
                        ProgressView()
                    } else {
                        Label(MR.strings().import_action.localized(), systemImage: "arrow.down.to.line")
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(selectedIds.isEmpty || isWorking || isLoading)
            }
        }
        .overlay {
            if isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if files.isEmpty {
                ContentUnavailableView {
                    Label(MR.strings().no_importable_files_found.localized(), systemImage: "doc.questionmark")
                }
            }
        }
        .alert(
            MR.strings().manual_import_failed.localized(),
            isPresented: Binding(
                get: { error != nil },
                set: { if !$0 { onClearError() } }
            )
        ) {
            Button("OK", action: onClearError)
        } message: {
            if let error = error {
                Text(error)
            }
        }
    }
}

private struct ManualImportRow: View {
    let file: ManualImportFile
    let isSelected: Bool
    let onToggle: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Button(action: onToggle) {
                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(isSelected ? .themePrimary : .secondary)
                    .imageScale(.large)
            }
            .buttonStyle(.plain)
            .padding(.top, 2)

            VStack(alignment: .leading, spacing: 4) {
                Text(file.displayName)
                    .font(.headline)
                    .fontWeight(.semibold)
                    .lineLimit(1)
                    .truncationMode(.middle)

                HStack(spacing: 6) {
                    Text(file.qualityLabel)
                    Text("•")
                    Text(file.sizeLabel)
                    Text("•")
                    Text(file.languageLabel)
                }
                .foregroundStyle(.secondary)
                .lineLimit(1)
                .font(.subheadline)

                if !file.reasons.isEmpty {
                    VStack(alignment: .leading, spacing: 2) {
                        ForEach(file.reasons, id: \.self) { reason in
                            Text(reason)
                        }
                    }
                    .font(.footnote)
                    .foregroundStyle(.orange)
                }
            }
            Spacer(minLength: 0)
        }
        .contentShape(Rectangle())
        .onTapGesture(perform: onToggle)
        .listRowBackground(Color.clear)
    }
}

#Preview {
    NavigationStack {
        ManualImportSheetContent(
            files: [
                ManualImportFile(
                    id: 1,
                    path: "/downloads/Movie.2024.1080p/movie.mkv",
                    relativePath: "Movie.2024.1080p.mkv",
                    name: "Movie.2024.1080p.mkv",
                    size: 4294967296,
                    quality: QualityInfo(
                        quality: Quality(id: 1, name: "WEBDL-1080p", source: nil, resolution: 1080, modifier: nil),
                        revision: Revision(version: 1, real: 0, isRepack: false)
                    ),
                    languages: [Language(id: 1, name: "English")],
                    releaseGroup: "FLUX",
                    qualityWeight: 1,
                    downloadId: "dl-1",
                    customFormats: [],
                    customFormatScore: 0,
                    rejections: [],
                    seriesId: nil,
                    episodeIds: [],
                    movieId: 1,
                    artistId: nil,
                    albumId: nil,
                    authorId: nil,
                    bookId: nil
                ),
                ManualImportFile(
                    id: 2,
                    path: "/downloads/Movie.2024.1080p/sample.mkv",
                    relativePath: "sample.mkv",
                    name: "sample.mkv",
                    size: 52428800,
                    quality: QualityInfo(
                        quality: Quality(id: 1, name: "WEBDL-1080p", source: nil, resolution: 1080, modifier: nil),
                        revision: Revision(version: 1, real: 0, isRepack: false)
                    ),
                    languages: [Language(id: 1, name: "English")],
                    releaseGroup: "FLUX",
                    qualityWeight: 1,
                    downloadId: "dl-1",
                    customFormats: [],
                    customFormatScore: 0,
                    rejections: [ManualImportRejection(reason: "Sample file detected", type: "permanent")],
                    seriesId: nil,
                    episodeIds: [],
                    movieId: 1,
                    artistId: nil,
                    albumId: nil,
                    authorId: nil,
                    bookId: nil
                )
            ],
            selectedIds: ["1"],
            isLoading: false,
            isWorking: false,
            error: nil,
            onToggleFile: { _ in },
            onImport: {},
            onClearError: {}
        )
    }
}
