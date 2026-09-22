//
//  SeriesFilesView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI
import Shared

struct SeriesFilesView: View {
    let series: ArrSeries
    var instanceId: Int64? = nil
    let episodes: [Episode]
    let searchIds: Set<Int64>
    let searchResult: Bool?
    let onToggleSeasonMonitor: (Int32) -> Void
    let onToggleEpisodeMonitor: (Episode) -> Void
    let onEpisodeAutomaticSearch: (Int64) -> Void
    let onSeasonAutomaticSearch: (Int32) -> Void
    let onDeleteSeasonFiles: (Int32) -> Void
    let seasonDeleteInProgress: Bool
    
    private var seasonEpisodes: [Int32:[Episode]] {
        Dictionary(grouping: episodes, by: { $0.seasonNumber })
    }
    
    var body: some View {
        let sortedSeasons = series.seasons.sorted { $0.seasonNumber > $1.seasonNumber }
        Section {
            ForEach(sortedSeasons, id: \.self) { season in
                SeasonCard(
                    series: series,
                    instanceId: instanceId,
                    season: season,
                    episodes: seasonEpisodes[season.seasonNumber]?.reversed() ?? [],
                    onToggleSeasonMonitor: onToggleSeasonMonitor,
                    onToggleEpisodeMonitor: onToggleEpisodeMonitor,
                    onEpisodeAutomaticSearch: onEpisodeAutomaticSearch,
                    onSeasonAutomaticSearch: onSeasonAutomaticSearch,
                    automaticSearchIds: searchIds,
                    onDeleteSeason: { onDeleteSeasonFiles(season.seasonNumber) },
                    seasonDeleteInProgress: seasonDeleteInProgress
                )
            }
        } header : {
            Text(MR.strings().seasons_header.localized())
                .font(.system(size: 26, weight: .bold))
        }
    }
}
