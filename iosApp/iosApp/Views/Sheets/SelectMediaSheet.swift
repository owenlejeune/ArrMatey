import Shared
import SwiftUI

struct SelectMediaSheet: View {
    let instance: Instance?
    let currentSelectionId: Int64?
    let onSelect: (ArrMedia) -> Void
    let onDismiss: () -> Void

    @ObservedObject var viewModel: ActivityQueueViewModelS
    @State private var searchQuery: String = ""
    @State private var mediaList: [ArrMedia] = []
    @State private var isLoading: Bool = true
    @State private var errorMessage: String? = nil

    private var titleText: String {
        switch instance?.type {
        case .sonarr: return MR.strings().select_series.localized()
        case .radarr: return MR.strings().select_movie.localized()
        case .lidarr: return MR.strings().select_artist.localized()
        case .bookshelf, .chaptarr: return MR.strings().select_book.localized()
        default: return MR.strings().select_media.localized()
        }
    }

    private var searchPrompt: String {
        switch instance?.type {
        case .sonarr: return MR.strings().search_series.localized()
        case .radarr: return MR.strings().search_movies.localized()
        case .lidarr: return MR.strings().search_artists.localized()
        case .bookshelf, .chaptarr: return MR.strings().search_books.localized()
        default: return MR.strings().search_media.localized()
        }
    }

    private var filteredMedia: [ArrMedia] {
        let trimmed = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if trimmed.isEmpty {
            return mediaList
        }
        return mediaList.filter { item in
            (item.title?.lowercased().contains(trimmed) ?? false) ||
            (item.cleanTitle?.lowercased().contains(trimmed) ?? false)
        }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.secondary)
                    TextField(searchPrompt, text: $searchQuery)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                    if !searchQuery.isEmpty {
                        Button {
                            searchQuery = ""
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color(UIColor.secondarySystemFill))
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .padding(.horizontal, 16)
                .padding(.vertical, 12)

                if isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if let error = errorMessage {
                    Spacer()
                    Text(error)
                        .foregroundColor(.red)
                        .font(.subheadline)
                        .padding()
                    Spacer()
                } else if filteredMedia.isEmpty {
                    Spacer()
                    ContentUnavailableView(
                        MR.strings().no_results_found.localized(),
                        systemImage: "magnifyingglass"
                    )
                    Spacer()
                } else {
                    List(filteredMedia, id: \.guid) { media in
                        let isSelected = media.id?.int64Value == currentSelectionId
                        Button {
                            onSelect(media)
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(media.title ?? MR.strings().unknown.localized())
                                        .font(.body.weight(.medium))
                                        .foregroundColor(.primary)

                                    let subtitle = formatSubtitle(for: media)
                                    if !subtitle.isEmpty {
                                        Text(subtitle)
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                    }
                                }

                                Spacer()

                                if isSelected {
                                    Image(systemName: "checkmark")
                                        .foregroundColor(Color.themePrimary)
                                }
                            }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle(titleText)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        onDismiss()
                    }
                }
            }
        }
        .task {
            loadMedia()
        }
    }

    private func formatSubtitle(for media: ArrMedia) -> String {
        var parts: [String] = []
        if let network = media.releasedBy, !network.isEmpty {
            parts.append(network)
        }
        if let year = media.year?.intValue, year > 0 {
            parts.append(String(year))
        }
        return parts.joined(separator: " • ")
    }

    private func loadMedia() {
        guard let instanceId = instance?.id else {
            isLoading = false
            return
        }
        viewModel.getMediaForInstance(
            instanceId: instanceId,
            onSuccess: { list in
                self.mediaList = list.sorted(by: { ($0.title ?? "").localizedCaseInsensitiveCompare($1.title ?? "") == .orderedAscending })
                self.isLoading = false
            },
            onError: { err in
                self.errorMessage = err
                self.isLoading = false
            }
        )
    }
}
