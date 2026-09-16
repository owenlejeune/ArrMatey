//
//  OverviewTabContentView.swift
//  iosApp
//

import SwiftUI
import Shared

struct OverviewTabContentView: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    @ObservedObject var viewModel: UnifiedMediaDetailsViewModelS
    var onEditPathClick: (() -> Void)? = nil
    let onPersonClick: (Int64) -> Void
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        VStack(alignment: .leading, spacing: 24) {
            if success.overview?.isEmpty == false || !success.keywords.isEmpty {
                ItemDescriptionCard(overview: success.overview)
            }

            if let credits = success.seerrMedia?.credits {
                creditsSection(credits)
            }

            if !viewModel.recommendationsState.items.isEmpty || viewModel.recommendationsState.isLoading {
                DiscoverSection(
                    title: MR.strings().recommended.localized(),
                    icon: nil,
                    data: viewModel.recommendationsState,
                    onItemClick: { item in
                        navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                    },
                    onLoadMore: { viewModel.loadNextRecommendationsPage() }
                )
            }

            if !viewModel.similarState.items.isEmpty || viewModel.similarState.isLoading {
                DiscoverSection(
                    title: MR.strings().similar.localized(),
                    icon: nil,
                    data: viewModel.similarState,
                    onItemClick: { item in
                        navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                    },
                    onLoadMore: { viewModel.loadNextSimilarPage() }
                )
            }

            unifiedInfoArea(success)
        }
    }

    @ViewBuilder
    private func creditsSection(_ credits: Credits) -> some View {
        let cast = credits.groupedCast
        let crew = credits.groupedCrew

        VStack(alignment: .leading, spacing: 12) {
            if !cast.isEmpty {
                Text(MR.strings().cast.localized())
                    .font(.title3.bold())

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(cast.prefix(20), id: \.id) { member in
                            CastMemberView(member: member) { personId in
                                onPersonClick(personId)
                            }
                        }
                    }
                }
            }

            if !crew.isEmpty {
                Text(MR.strings().crew.localized())
                    .font(.title3.bold())

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(crew.prefix(20), id: \.id) { member in
                            CrewMemberView(member: member) { personId in
                                onPersonClick(personId)
                            }
                        }
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func unifiedInfoArea(_ success: UnifiedMediaDetailsUiStateSuccess) -> some View {
        let arrItems = buildArrInfoItems(success: success)
        let seerrItems = buildSeerrInfoItems(success: success)
        let arrInstance = success.availableInstances.first(where: { $0.id == success.selectedInstanceId?.int64Value }) ?? viewModel.activeInstance
        let seerrInstance = viewModel.activeSeerrInstance
        if !arrItems.isEmpty || !seerrItems.isEmpty || !success.keywords.isEmpty {
            MediaInfoArea(
                arrItems: arrItems,
                seerrItems: seerrItems,
                keywords: success.keywords,
                arrInstance: arrInstance,
                seerrInstance: seerrInstance
            )
        }
    }

    private func buildArrInfoItems(success: UnifiedMediaDetailsUiStateSuccess) -> [InfoItem] {
        if success.hasArrId, let arrMedia = success.arrMedia {
            return buildArrInfoItems(
                arrMedia: arrMedia,
                qualityProfiles: viewModel.qualityProfiles,
                tags: viewModel.tags
            )
        }
        return []
    }

    private func buildSeerrInfoItems(success: UnifiedMediaDetailsUiStateSuccess) -> [InfoItem] {
        var items: [InfoItem] = []
        if let seerrMedia = success.seerrMedia {
            items.append(InfoItem(label: MR.strings().status.localized(), value: seerrMedia.status))

            if let movie = seerrMedia as? MovieDetails {
                if let releaseDate = movie.releaseDate {
                    items.append(InfoItem(label: MR.strings().release_date.localized(), value: releaseDate.format(pattern: "MMM dd, yyyy")))
                }
            }

            let countries = seerrMedia.productionCountries.map { $0.name }.joined(separator: "\n")
            if !countries.isEmpty {
                items.append(InfoItem(label: MR.strings().production_countries.localized(), value: countries))
            }

            let studios = seerrMedia.productionCompanies.map { $0.name }.joined(separator: "\n")
            if !studios.isEmpty {
                items.append(InfoItem(label: MR.strings().studios.localized(), value: studios))
            }
        }
        return items
    }

    private func buildArrInfoItems(arrMedia: ArrMedia, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        if let series = arrMedia as? ArrSeries {
            return seriesInfoItems(series, qualityProfiles: qualityProfiles, tags: tags)
        } else if let movie = arrMedia as? ArrMovie {
            return movieInfoItems(movie, qualityProfiles: qualityProfiles, tags: tags)
        } else if let artist = arrMedia as? Arrtist {
            return artistInfoItems(artist, qualityProfiles: qualityProfiles, tags: tags)
        } else if let author = arrMedia as? Author {
            return authorInfoItems(author, qualityProfiles: qualityProfiles, tags: tags)
        } else if let audiobook = arrMedia as? Audiobook {
            return audiobookInfoItems(audiobook)
        }
        return []
    }

    private func seriesInfoItems(_ series: ArrSeries, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let qualityLabel = qualityProfiles.first(where: { $0.id == series.qualityProfileId })?.name ?? unknown
        let tagsLabel = series.formatTags(availableTags: tags) ?? MR.strings().none.localized()
        let monitorLabel = series.monitorNewItems == .all ? MR.strings().monitored.localized() : MR.strings().unmonitored.localized()
        let seasonFolderLabel = series.seasonFolder ? MR.strings().yes.localized() : MR.strings().no.localized()
        return [
            InfoItem(label: MR.strings().status.localized(), value: series.status.resource.localized()),
            InfoItem(label: MR.strings().series_type.localized(), value: series.seriesType.name),
            InfoItem(label: MR.strings().size_on_disk.localized(), value: series.fileSize?.int64Value.bytesAsFileSizeString() ?? unknown),
            InfoItem(label: MR.strings().root_folder.localized(), value: series.rootFolderPath ?? unknown, onClick: onEditPathClick),
            InfoItem(label: MR.strings().path.localized(), value: series.path ?? unknown, onClick: onEditPathClick),
            InfoItem(label: MR.strings().new_seasons.localized(), value: monitorLabel),
            InfoItem(label: MR.strings().season_folders.localized(), value: seasonFolderLabel),
            InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
            InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
        ]
    }

    private func movieInfoItems(_ movie: ArrMovie, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let qualityLabel = qualityProfiles.first(where: { $0.id == movie.qualityProfileId })?.name ?? unknown
        let tagsLabel = movie.formatTags(availableTags: tags) ?? MR.strings().none.localized()
        let rootFolderValue = movie.rootFolderPath.isEmpty ? unknown : movie.rootFolderPath
        var info: [InfoItem] = [
            InfoItem(label: MR.strings().status.localized(), value: movie.status.resource.localized()),
            InfoItem(label: MR.strings().minimum_availability.localized(), value: movie.minimumAvailability.name),
            InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue, onClick: onEditPathClick),
            InfoItem(label: MR.strings().path.localized(), value: movie.path ?? unknown, onClick: onEditPathClick)
        ]
        if let inCinemas = movie.inCinemas?.format(pattern: "MMM d, yyyy") {
            info.append(InfoItem(label: MR.strings().in_cinemas.localized(), value: inCinemas))
        }
        if let physicalRelease = movie.physicalRelease?.format(pattern: "MMM d, yyyy") {
            info.append(InfoItem(label: MR.strings().physical_release.localized(), value: physicalRelease))
        }
        if let digitalRelease = movie.digitalRelease?.format(pattern: "MMM d, yyyy") {
            info.append(InfoItem(label: MR.strings().digital_release.localized(), value: digitalRelease))
        }
        info.append(InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel))
        info.append(InfoItem(label: MR.strings().tags.localized(), value: tagsLabel))
        return info
    }

    private func artistInfoItems(_ artist: Arrtist, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let qualityLabel = qualityProfiles.first(where: { $0.id == artist.qualityProfileId })?.name ?? unknown
        let tagsLabel = artist.formatTags(availableTags: tags) ?? MR.strings().none.localized()
        let monitorLabel = artist.monitorNewItems == .all ? MR.strings().monitored.localized() : MR.strings().unmonitored.localized()
        let rootFolderValue = (artist.rootFolderPath?.isEmpty == false) ? artist.rootFolderPath! : unknown
        return [
            InfoItem(label: MR.strings().status.localized(), value: artist.status.resource.localized()),
            InfoItem(label: MR.strings().size_on_disk.localized(), value: artist.fileSize?.int64Value.bytesAsFileSizeString() ?? unknown),
            InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue, onClick: onEditPathClick),
            InfoItem(label: MR.strings().path.localized(), value: artist.path ?? unknown, onClick: onEditPathClick),
            InfoItem(label: MR.strings().new_albums.localized(), value: monitorLabel),
            InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
            InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
        ]
    }

    private func authorInfoItems(_ author: Author, qualityProfiles: [QualityProfile], tags: [Tag]) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let qualityLabel = qualityProfiles.first(where: { $0.id == author.qualityProfileId })?.name ?? unknown
        let tagsLabel = author.formatTags(availableTags: tags) ?? MR.strings().none.localized()
        let monitorLabel = author.monitorNewItems == .all ? MR.strings().monitored.localized() : MR.strings().unmonitored.localized()
        let rootFolderValue = (author.rootFolderPath?.isEmpty == false) ? author.rootFolderPath! : unknown
        return [
            InfoItem(label: MR.strings().status.localized(), value: author.status.resource.localized()),
            InfoItem(label: MR.strings().size_on_disk.localized(), value: author.fileSize?.int64Value.bytesAsFileSizeString() ?? unknown),
            InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue, onClick: onEditPathClick),
            InfoItem(label: MR.strings().path.localized(), value: author.path ?? unknown, onClick: onEditPathClick),
            InfoItem(label: MR.strings().new_books.localized(), value: monitorLabel),
            InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
            InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
        ]
    }

    private func audiobookInfoItems(_ audiobook: Audiobook) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        let authorString = audiobook.authors.joined(separator: " • ")
        let narratorsString = audiobook.narrators.joined(separator: " • ")
        var info: [InfoItem] = [
            InfoItem(label: MR.strings().audiobook_info_authors.localized(), value: authorString),
            InfoItem(label: MR.strings().audiobook_info_narrators.localized(), value: narratorsString),
            InfoItem(label: MR.strings().publisher.localized(), value: audiobook.publisher ?? unknown)
        ]
        if let language = audiobook.language {
            info.append(InfoItem(label: MR.strings().language.localized(), value: language.capitalized))
        }
        info.append(InfoItem(label: MR.strings().size_on_disk.localized(), value: audiobook.fileSize?.int64Value.bytesAsFileSizeString() ?? unknown))
        info.append(InfoItem(label: MR.strings().path.localized(), value: audiobook.path ?? unknown, onClick: onEditPathClick))
        return info
    }
}
