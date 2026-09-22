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
        case .details(let arrId, let tmdbId, let tvdbId, let instanceType, let requestType, let instanceId, let episodeId):
            UnifiedMediaDetailsScreen(
                arrId: arrId,
                tmdbId: tmdbId,
                tvdbId: tvdbId,
                instanceType: instanceType,
                requestType: requestType,
                instanceId: instanceId,
                initialEpisodeId: episodeId
            )

        case .search(let query, let type, let instanceId):
            MediaSearchScreen(query: query, type: type, instanceId: instanceId)

        case .globalSearch(let query):
            UnifiedSearchScreen(query: query)

        case .preview(let json, let type):
            MediaPreviewScreen(json: json, type: type)

        case .movieRelease(let movieId, let instanceId):
            let releaseParams = ReleaseParamsMovie(mediaId: movieId)
            InteractiveSearchScreen(type: .radarr, releaseParams: releaseParams, instanceId: instanceId)

        case .movieFiles(let json):
            MovieFilesScreen(json: json)

        case .seriesReleases(let seriesId, let seasonNumber, let episodeId, let instanceId):
            let releaseParams = ReleaseParamsSeries(
                seriesId: seriesId?.asKotlinLong,
                seasonNumber: seasonNumber?.asKotlinInt,
                episodeId: episodeId?.asKotlinLong
            )
            let defaultFilter: ReleaseFilterBy = if episodeId != nil { .singleEpisode } else { .seasonPack }
            InteractiveSearchScreen(type: .sonarr, releaseParams: releaseParams, defaultFilter: defaultFilter, instanceId: instanceId)

        case .episodeDetails(let seriesJson, let episodeJson, let instanceId):
            EpisodeDetailsScreen(seriesJson: seriesJson, episodeJson: episodeJson, instanceId: instanceId)

        case .albumReleases(let albumId, let artistId, let instanceId):
            let releaseParams = ReleaseParamsAlbum(
                mediaId: albumId,
                artistId: artistId?.asKotlinLong
            )
            InteractiveSearchScreen(type: .lidarr, releaseParams: releaseParams, instanceId: instanceId)

        case .bookReleases(let bookId, let instanceId):
            let releaseParams = ReleaseParamsBook(mediaId: bookId)
            InteractiveSearchScreen(type: .bookshelf, releaseParams: releaseParams, instanceId: instanceId)

        case .audiobookReleases(let id, let query, let instanceId):
            let releaseParams = ReleaseParamsAudiobook(mediaId: id?.asKotlinLong, query: query)
            InteractiveSearchScreen(type: .listenarr, releaseParams: releaseParams, instanceId: instanceId)

        case .authorFiles(let authorJson):
            AuthorFilesScreen(authorJson: authorJson)

        case .audiobookFiles(let audiobookJson):
            AudiobookFilesScreen(audiobookJson: audiobookJson)

        case .bookDetails(let bookJson, let authorJson, let instanceId):
            BookDetailsScreen(bookJson: bookJson, authorJson: authorJson, instanceId: instanceId)
        }
    }
}
