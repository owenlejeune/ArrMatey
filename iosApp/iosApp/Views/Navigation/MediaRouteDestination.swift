//
//  MediaRouteDestination.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-01.
//

import SwiftUI
import Shared

struct MediaRouteDestination: View {
    let route: MediaRoute
    
    var body: some View {
        switch route {
        case .details(let id, let type):
            MediaDetailsScreen(id: id, type: type)
            
        case .search(let query, let type):
            MediaSearchScreen(query: query, type: type)
            
        case .preview(let json, let type):
            MediaPreviewScreen(json: json, type: type)
            
        case .movieRelease(let movieId):
            let releaseParams = ReleaseParamsMovie(movieId: movieId)
            InteractiveSearchScreen(type: .radarr, releaseParams: releaseParams)
            
        case .movieFiles(let json):
            MovieFilesScreen(json: json)
            
        case .seriesReleases(let seriesId, let seasonNumber, let episodeId):
            let releaseParams = ReleaseParamsSeries(
                seriesId: seriesId?.asKotlinLong,
                seasonNumber: seasonNumber?.asKotlinInt,
                episodeId: episodeId?.asKotlinLong
            )
            let defaultFilter: ReleaseFilterBy = if episodeId != nil { .singleEpisode } else { .seasonPack }
            InteractiveSearchScreen(type: .sonarr, releaseParams: releaseParams, defaultFilter: defaultFilter)
            
        case .episodeDetails(let seriesJson, let episodeJson):
            EpisodeDetailsScreen(seriesJson: seriesJson, episodeJson: episodeJson)
            
        case .albumReleases(let albumId, let artistId):
            let releaseParams = ReleaseParamsAlbum(
                albumId: albumId,
                artistId: artistId?.asKotlinLong
            )
            InteractiveSearchScreen(type: .lidarr, releaseParams: releaseParams)
        }
    }
}
