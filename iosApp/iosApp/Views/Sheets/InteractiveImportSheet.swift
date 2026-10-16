import Shared
import SwiftUI

struct InteractiveImportFileItem: Identifiable {
    let id: String
    var file: ManualImportFile
    var isSelected: Bool
    var mappedMediaId: Int64?
    var mappedMediaTitle: String?
}

struct InteractiveImportSheet: View {
    let instance: Instance?
    let folderPath: String
    let importMode: ManualImportMode
    let initialFiles: [ManualImportFile]
    let onDismiss: () -> Void
    let onImportSuccess: () -> Void

    @ObservedObject var viewModel: ActivityQueueViewModelS

    @State private var items: [InteractiveImportFileItem] = []
    @State private var itemForMapping: InteractiveImportFileItem? = nil
    @State private var itemForEpisodeMapping: InteractiveImportFileItem? = nil
    @State private var isImporting: Bool = false
    @State private var errorMessage: String? = nil

    init(
        instance: Instance?,
        folderPath: String,
        importMode: ManualImportMode,
        initialFiles: [ManualImportFile],
        viewModel: ActivityQueueViewModelS,
        onDismiss: @escaping () -> Void,
        onImportSuccess: @escaping () -> Void
    ) {
        self.instance = instance
        self.folderPath = folderPath
        self.importMode = importMode
        self.initialFiles = initialFiles
        self.viewModel = viewModel
        self.onDismiss = onDismiss
        self.onImportSuccess = onImportSuccess

        let mapped = initialFiles.map { f in
            let initialId = f.effectiveSeriesId?.int64Value
                ?? f.effectiveMovieId?.int64Value
                ?? f.artistId?.int64Value
                ?? f.bookId?.int64Value
            return InteractiveImportFileItem(
                id: f.stableId,
                file: f,
                isSelected: false,
                mappedMediaId: initialId,
                mappedMediaTitle: f.series?.title ?? f.movie?.title
            )
        }
        _items = State(initialValue: mapped)
    }

    private var selectedCount: Int {
        items.filter { $0.isSelected }.count
    }

    private var mediaTypeIcon: String {
        switch instance?.type {
        case .sonarr: return "tv"
        case .radarr: return "film"
        case .lidarr: return "music.note"
        case .bookshelf, .chaptarr: return "book"
        default: return "play.rectangle"
        }
    }

    private var defaultMediaButtonLabel: String {
        switch instance?.type {
        case .sonarr: return MR.strings().select_series.localized()
        case .radarr: return MR.strings().select_movie.localized()
        case .lidarr: return MR.strings().select_artist.localized()
        case .bookshelf, .chaptarr: return MR.strings().select_book.localized()
        default: return MR.strings().select_media.localized()
        }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack(alignment: .top, spacing: 10) {
                    Image(systemName: "info.circle")
                        .foregroundColor(Color.themePrimary)
                        .font(.body)
                        .padding(.top, 2)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(MR.strings().folder_label.formatted(args: [folderPath]))
                            .font(.subheadline.weight(.medium))
                            .lineLimit(1)
                            .truncationMode(.middle)
                        Text(MR.strings().interactive_import_scanned_description.formatted(args: [items.count]))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(Color.themePrimary.opacity(0.12))
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, 8)

                if let error = errorMessage {
                    HStack {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.footnote)
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 6)
                }

                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(items.indices, id: \.self) { idx in
                            let item = items[idx]
                            fileCard(item: item, index: idx)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                }

                Divider()

                HStack {
                    Text(MR.strings().selected_of_total_count.formatted(args: [selectedCount, items.count]))
                        .font(.subheadline)
                        .foregroundColor(.secondary)

                    Spacer()

                    Button {
                        executeImport()
                    } label: {
                        HStack(spacing: 6) {
                            if isImporting {
                                ProgressView()
                                    .progressViewStyle(.circular)
                                    .tint(.white)
                            } else {
                                Image(systemName: "arrow.down.to.line")
                                Text(MR.strings().import_selected.localized())
                            }
                        }
                        .font(.subheadline.weight(.semibold))
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)
                        .background(selectedCount > 0 && !isImporting ? Color.themePrimary : Color.gray.opacity(0.4))
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                    .disabled(selectedCount == 0 || isImporting)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(Color(UIColor.systemBackground))
            }
            .navigationTitle(MR.strings().interactive_import.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    HStack(spacing: 12) {
                        Button {
                            selectAll()
                        } label: {
                            Image(systemName: "checklist")
                        }
                        .accessibilityLabel(MR.strings().select_all.localized())

                        Button {
                            selectNone()
                        } label: {
                            Image(systemName: "square.slash")
                        }
                        .accessibilityLabel(MR.strings().none.localized())
                    }
                }
            }
            .sheet(item: $itemForMapping) { mappingItem in
                SelectMediaSheet(
                    instance: instance,
                    currentSelectionId: mappingItem.mappedMediaId,
                    onSelect: { media in
                        applyMapping(media: media, forItemId: mappingItem.id)
                        itemForMapping = nil
                    },
                    onDismiss: {
                        itemForMapping = nil
                    },
                    viewModel: viewModel
                )
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
            }
            .sheet(item: $itemForEpisodeMapping) { mappingItem in
                if let instanceId = instance?.id,
                   let seriesId = mappingItem.mappedMediaId ?? mappingItem.file.effectiveSeriesId?.int64Value {
                    SelectEpisodeSheet(
                        instanceId: instanceId,
                        seriesId: seriesId,
                        seriesTitle: mappingItem.mappedMediaTitle ?? mappingItem.file.series?.title,
                        initialSeasonNumber: mappingItem.file.effectiveSeasonNumber?.int32Value,
                        initialEpisodeIds: mappingItem.file.effectiveEpisodeIds.map { $0.int64Value },
                        onSelect: { seasonNumber, episodes in
                            applyEpisodesMapping(episodes: episodes, forItemId: mappingItem.id)
                            itemForEpisodeMapping = nil
                        },
                        onDismiss: {
                            itemForEpisodeMapping = nil
                        },
                        viewModel: viewModel
                    )
                    .presentationDetents([.medium, .large])
                    .presentationDragIndicator(.visible)
                }
            }
        }
        .task {
            resolveInitialMediaTitles()
        }
    }

    @ViewBuilder
    private func fileCard(item: InteractiveImportFileItem, index: Int) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top, spacing: 12) {
                Button {
                    items[index].isSelected.toggle()
                } label: {
                    Image(systemName: item.isSelected ? "checkmark.square.fill" : "square")
                        .font(.system(size: 20))
                        .foregroundColor(item.isSelected ? Color.themePrimary : .secondary)
                }
                .buttonStyle(.plain)

                VStack(alignment: .leading, spacing: 4) {
                    Text(item.file.displayName)
                        .font(.subheadline.weight(.semibold))
                        .foregroundColor(.primary)
                        .lineLimit(2)

                    Text("\(item.file.sizeLabel) • \(item.file.qualityLabel)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                Spacer()
            }

            Divider()

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    Button {
                        itemForMapping = items[index]
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: mediaTypeIcon)
                                .font(.system(size: 13))
                            Text(item.mappedMediaTitle ?? defaultMediaButtonLabel)
                                .font(.subheadline.weight(.medium))
                                .lineLimit(1)
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(item.mappedMediaTitle != nil ? Color.themePrimary.opacity(0.12) : Color(UIColor.tertiarySystemFill))
                        .foregroundColor(item.mappedMediaTitle != nil ? Color.themePrimary : Color.primary)
                        .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)

                    let seriesId = item.mappedMediaId ?? item.file.effectiveSeriesId?.int64Value
                    if instance?.type == .sonarr && seriesId != nil {
                        let hasEpisodes = !item.file.episodes.isEmpty || !item.file.effectiveEpisodeIds.isEmpty
                        Button {
                            itemForEpisodeMapping = items[index]
                        } label: {
                            HStack(spacing: 6) {
                                Image(systemName: "list.number")
                                    .font(.system(size: 13))
                                Text(episodeButtonLabel(for: item.file))
                                    .font(.subheadline.weight(.medium))
                                    .lineLimit(1)
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(hasEpisodes ? Color.themePrimary.opacity(0.12) : Color(UIColor.tertiarySystemFill))
                            .foregroundColor(hasEpisodes ? Color.themePrimary : Color.primary)
                            .clipShape(Capsule())
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
        .padding(14)
        .background(Color(UIColor.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(item.isSelected ? Color.themePrimary : Color(UIColor.separator).opacity(0.3), lineWidth: item.isSelected ? 1.5 : 1)
        )
    }

    private func episodeButtonLabel(for file: ManualImportFile) -> String {
        let eps = file.episodes
        let season = file.effectiveSeasonNumber?.intValue
        let seasonPrefix = season != nil ? "S\(season!):" : ""
        if !eps.isEmpty {
            let epNumbers = eps.map { "\($0.episodeNumber)" }.joined(separator: "-")
            if let title = eps.first?.title, !title.isEmpty {
                return "\(seasonPrefix)E\(epNumbers) - \(title)"
            }
            return "\(seasonPrefix)E\(epNumbers)"
        }
        if !file.effectiveEpisodeIds.isEmpty {
            return "\(seasonPrefix)\(file.effectiveEpisodeIds.count) Ep(s)"
        }
        return MR.strings().select_episode.localized()
    }

    private func selectAll() {
        for i in items.indices {
            items[i].isSelected = true
        }
    }

    private func selectNone() {
        for i in items.indices {
            items[i].isSelected = false
        }
    }

    private func applyMapping(media: ArrMedia, forItemId itemId: String) {
        guard let idx = items.firstIndex(where: { $0.id == itemId }) else { return }
        let mediaId = media.id?.int64Value ?? 0
        items[idx].mappedMediaId = mediaId
        items[idx].mappedMediaTitle = media.title

        switch instance?.type {
        case .sonarr:
            items[idx].file = items[idx].file.withSeriesId(newSeriesId: mediaId)
        case .radarr:
            items[idx].file = items[idx].file.withMovieId(newMovieId: mediaId)
        case .lidarr:
            items[idx].file = items[idx].file.withArtistId(newArtistId: mediaId)
        case .bookshelf, .chaptarr:
            items[idx].file = items[idx].file.withBookId(newBookId: mediaId)
        default:
            break
        }
    }

    private func applyEpisodesMapping(episodes: [Episode], forItemId itemId: String) {
        guard let idx = items.firstIndex(where: { $0.id == itemId }) else { return }
        items[idx].file = items[idx].file.withEpisodes(newEpisodes: episodes)
    }

    private func resolveInitialMediaTitles() {
        guard let instanceId = instance?.id else { return }
        viewModel.getMediaForInstance(
            instanceId: instanceId,
            onSuccess: { mediaList in
                for i in items.indices {
                    if let mediaId = items[i].mappedMediaId, items[i].mappedMediaTitle == nil {
                        if let match = mediaList.first(where: { $0.id?.int64Value == mediaId }) {
                            items[i].mappedMediaTitle = match.title
                        }
                    }
                }
            },
            onError: { _ in }
        )
    }

    private func executeImport() {
        guard let instanceId = instance?.id else { return }
        let selectedFiles = items.filter { $0.isSelected }.map { $0.file }
        guard !selectedFiles.isEmpty else { return }

        isImporting = true
        errorMessage = nil

        viewModel.executeManualImport(
            instanceId: instanceId,
            files: selectedFiles,
            importMode: importMode.value,
            onSuccess: {
                isImporting = false
                onImportSuccess()
            },
            onError: { err in
                isImporting = false
                errorMessage = err
            }
        )
    }
}
