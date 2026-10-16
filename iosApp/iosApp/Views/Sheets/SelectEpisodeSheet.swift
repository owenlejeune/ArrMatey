import Shared
import SwiftUI

struct SelectEpisodeSheet: View {
    let instanceId: Int64
    let seriesId: Int64
    let seriesTitle: String?
    let initialSeasonNumber: Int32?
    let initialEpisodeIds: [Int64]
    let onSelect: (Int32?, [Episode]) -> Void
    let onDismiss: () -> Void

    @ObservedObject var viewModel: ActivityQueueViewModelS
    @State private var allEpisodes: [Episode] = []
    @State private var isLoading: Bool = true
    @State private var errorMessage: String? = nil
    @State private var selectedSeason: Int32 = 1
    @State private var selectedEpisodeIds: Set<Int64> = []

    init(
        instanceId: Int64,
        seriesId: Int64,
        seriesTitle: String?,
        initialSeasonNumber: Int32?,
        initialEpisodeIds: [Int64],
        onSelect: @escaping (Int32?, [Episode]) -> Void,
        onDismiss: @escaping () -> Void,
        viewModel: ActivityQueueViewModelS
    ) {
        self.instanceId = instanceId
        self.seriesId = seriesId
        self.seriesTitle = seriesTitle
        self.initialSeasonNumber = initialSeasonNumber
        self.initialEpisodeIds = initialEpisodeIds
        self.onSelect = onSelect
        self.onDismiss = onDismiss
        self.viewModel = viewModel
        _selectedSeason = State(initialValue: initialSeasonNumber ?? 1)
        _selectedEpisodeIds = State(initialValue: Set(initialEpisodeIds))
    }

    private var availableSeasons: [Int32] {
        let seasons = Set(allEpisodes.map { $0.seasonNumber }).sorted()
        return seasons
    }

    private var seasonEpisodes: [Episode] {
        allEpisodes
            .filter { $0.seasonNumber == selectedSeason }
            .sorted { $0.episodeNumber < $1.episodeNumber }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
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
                } else if allEpisodes.isEmpty {
                    Spacer()
                    ContentUnavailableView(
                        MR.strings().no_episodes_found_for_series.localized(),
                        systemImage: "tv"
                    )
                    Spacer()
                } else {
                    if !availableSeasons.isEmpty {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(availableSeasons, id: \.self) { seasonNum in
                                    let isSelected = seasonNum == selectedSeason
                                    Button {
                                        selectedSeason = seasonNum
                                    } label: {
                                        Text(seasonNum == 0 ? MR.strings().specials.localized() : MR.strings().season_label.formatted(args: [seasonNum]))
                                            .font(.subheadline.weight(isSelected ? .semibold : .regular))
                                            .padding(.horizontal, 14)
                                            .padding(.vertical, 8)
                                            .background(isSelected ? Color.themePrimary : Color(UIColor.secondarySystemFill))
                                            .foregroundColor(isSelected ? .white : .primary)
                                            .clipShape(Capsule())
                                    }
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 10)
                        }
                        Divider()
                    }

                    List(seasonEpisodes, id: \.id) { ep in
                        let isSelected = selectedEpisodeIds.contains(ep.id)
                        Button {
                            if isSelected {
                                selectedEpisodeIds.remove(ep.id)
                            } else {
                                selectedEpisodeIds = [ep.id]
                            }
                        } label: {
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    HStack(spacing: 6) {
                                        Text("E\(ep.episodeNumber)")
                                            .font(.subheadline.weight(.bold))
                                            .foregroundColor(Color.themePrimary)

                                        Text(ep.title ?? "\(MR.strings().episode.localized()) \(ep.episodeNumber)")
                                            .font(.body.weight(.medium))
                                            .foregroundColor(.primary)
                                            .lineLimit(1)
                                    }

                                    if let airDate = ep.airDate {
                                        Text(airDate.description)
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                    }
                                }

                                Spacer()

                                if isSelected {
                                    Image(systemName: "checkmark")
                                        .foregroundColor(Color.themePrimary)
                                        .font(.body.weight(.semibold))
                                }
                            }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle(MR.strings().select_episode.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(MR.strings().cancel.localized()) {
                        onDismiss()
                    }
                }

                ToolbarItem(placement: .confirmationAction) {
                    Button(MR.strings().done.localized()) {
                        let chosen = allEpisodes.filter { selectedEpisodeIds.contains($0.id) }
                        onSelect(selectedSeason, chosen)
                        onDismiss()
                    }
                    .disabled(selectedEpisodeIds.isEmpty)
                }
            }
        }
        .task {
            loadEpisodes()
        }
    }

    private func loadEpisodes() {
        viewModel.getEpisodesForSeries(
            instanceId: instanceId,
            seriesId: seriesId,
            seasonNumber: nil,
            onSuccess: { eps in
                allEpisodes = eps
                isLoading = false
                let seasons = Set(eps.map { $0.seasonNumber }).sorted()
                if let initial = initialSeasonNumber, seasons.contains(initial) {
                    selectedSeason = initial
                } else if let firstSeason = seasons.first(where: { $0 > 0 }) ?? seasons.first {
                    selectedSeason = firstSeason
                }
            },
            onError: { err in
                errorMessage = err
                isLoading = false
            }
        )
    }
}
