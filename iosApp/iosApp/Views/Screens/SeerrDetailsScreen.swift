//
//  SeerrDetailsScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrDetailsScreen: View {
    @StateObject private var viewModel: SeerrMediaDetailsViewModelS
    @EnvironmentObject private var navigationManager: NavigationManager
    
    init(tmdbId: Int64, requestType: RequestType) {
        _viewModel = StateObject(wrappedValue: SeerrMediaDetailsViewModelS(tmdbId: tmdbId, requestType: requestType))
    }
    
    var body: some View {
        Group {
            if let state = viewModel.uiState as? SeerrDetailsStateSuccess {
                successContent(state: state)
            } else if let errorState = viewModel.uiState as? SeerrDetailsStateError {
                ErrorView(
                    errorType: errorState.errorType,
                    message: errorState.message ?? MR.strings().unknown.localized(),
                    onOpenSettings: {
                        if let id = viewModel.selectedInstance?.id {
                            navigationManager.goToEditInstance(of: .seerr, id)
                        }
                    },
                    onRetry: { viewModel.refreshDetails() }
                )
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .refreshable {
            viewModel.refreshDetails()
        }
        .toolbar {
            if viewModel.buttonState.showReportIssueButton {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: { viewModel.showReportIssueSheet() }) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.orange)
                    }
                }
            }
        }
    }
    
    // MARK: - Success Content
    
    @ViewBuilder
    private func successContent(state: SeerrDetailsStateSuccess) -> some View {
        let item = state.item
        
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                detailsHeader(item: item)
                    .frame(height: 400)
                
                VStack(alignment: .leading, spacing: 12) {
                    if let tagline = item.tagline, !tagline.isEmpty {
                        Text(tagline)
                            .font(.title3)
                            .italic()
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 24)
                    }
                    
                    if let overview = item.overview, !overview.isEmpty {
                        Text(overview)
                            .font(.body)
                            .padding(.horizontal, 24)
                    }
                    
                    if let tvDetails = item as? TvDetails {
                        seasonsSection(tvDetails)
                    }
                    
                    if let credits = item.credits {
                        creditsSection(credits)
                    }
                    
                    infoSection(item: item, state: state)
                }
                .padding(.bottom, 24)
                .padding(.horizontal, 12)
            }
        }
        .ignoresSafeArea(edges: .top)
    }
    
    // MARK: - Header
    
    private func detailsHeader(item: RequestMediaDetails) -> some View {
        GeometryReader { geometry in
            ZStack(alignment: .topLeading) {
                MediaHeaderBanner(bannerUrl: URL(string: item.fullBackdropPath ?? ""))
                    .frame(width: geometry.size.width)

                HStack(alignment: .top, spacing: 0) {
                    GenericPosterItem(posterUrl: item.fullPosterPath)
                        .frame(height: 220)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(item.displayTitle)
                            .font(.title2.bold())
                        
                        HStack(spacing: 8) {
                            if let year = item.displayDate?.year {
                                Text(String(year))
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            Text(item.status)
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(Color.accentColor.opacity(0.15))
                                .clipShape(Capsule())
                        }
                        
                        if !item.genres.isEmpty {
                            Text(item.genres.prefix(3).map { $0.name }.joined(separator: " \u{2022} "))
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .top)
                }
                .padding(.top, 170)
                .padding(.horizontal, 12)
            }
        }
    }
    
    // MARK: - Seasons
    
    private func seasonsSection(_ series: TvDetails) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(MR.strings().seasons_header.localized())
                .font(.title3.bold())
                .padding(.horizontal, 24)
            
            ForEach(series.seasons, id: \.seasonNumber) { season in
                SeasonDisclosureRow(
                    seasonNumber: season.seasonNumber,
                    episodeCount: season.episodeCount,
                    episodes: season.episodes.map { ep in
                        SeerrEpisodeData(
                            episodeNumber: ep.episodeNumber,
                            name: ep.name,
                            airDate: ep.airDate,
                            overview: ep.overview,
                            stillPath: ep.stillPath
                        )
                    }
                )
                .padding(.horizontal, 24)
            }
        }
    }
    
    // MARK: - Credits
    
    private func creditsSection(_ credits: Credits) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            if !credits.cast.isEmpty {
                Text(MR.strings().cast.localized())
                    .font(.title3.bold())
                    .padding(.horizontal, 24)
                
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(credits.cast.prefix(20), id: \.id) { member in
                            CastMemberView(member: member)
                        }
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
    }
    
    // MARK: - Info
    
    private func infoSection(item: RequestMediaDetails, state: SeerrDetailsStateSuccess) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 16) {
                Spacer()
                if let rt = state.rtRatings {
                    VStack(spacing: 2) {
                        Text("\(rt.criticsScore)%")
                            .font(.headline)
                        Text("Critics")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                    VStack(spacing: 2) {
                        Text("\(rt.audienceScore)%")
                            .font(.headline)
                        Text("Audience")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
                VStack(spacing: 2) {
                    Text("\(Int((item.voteAverage * 10).rounded()))%")
                        .font(.headline)
                    Text("TMDB")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                Spacer()
            }
            
            let infoItems: [(String, String)] = buildInfoItems(item: item)
            
            if !infoItems.isEmpty {
                VStack(spacing: 8) {
                    ForEach(infoItems, id: \.0) { label, value in
                        HStack(alignment: .top) {
                            Text(label)
                                .font(.subheadline.bold())
                                .frame(width: 120, alignment: .leading)
                            Text(value)
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                            Spacer()
                        }
                    }
                }
                .padding(16)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .padding(.horizontal, 24)
            }
        }
    }
    
    private func buildInfoItems(item: RequestMediaDetails) -> [(String, String)] {
        var items: [(String, String)] = []
        items.append((MR.strings().status.localized(), item.status))
        
        if let movie = item as? MovieDetails {
            if let releaseDate = movie.releaseDate {
                items.append((MR.strings().release_date.localized(), releaseDate.format(pattern: "MMM dd, yyyy")))
            }
        }
        
        let countries = item.productionCountries.map { $0.name }.joined(separator: "\n")
        if !countries.isEmpty {
            items.append((MR.strings().production_countries.localized(), countries))
        }
        
        let studios = item.productionCompanies.map { $0.name }.joined(separator: "\n")
        if !studios.isEmpty {
            items.append((MR.strings().studios.localized(), studios))
        }
        
        return items
    }
}

// MARK: - Data Types

private struct SeerrEpisodeData: Identifiable {
    let episodeNumber: Int32
    let name: String
    let airDate: LocalDate?
    let overview: String?
    let stillPath: String?
    var id: Int32 { episodeNumber }
}

// MARK: - Season Disclosure Row

private struct SeasonDisclosureRow: View {
    let seasonNumber: Int32
    let episodeCount: Int32
    let episodes: [SeerrEpisodeData]
    @State private var isExpanded = false
    
    var body: some View {
        VStack(spacing: 0) {
            Button(action: { withAnimation { isExpanded.toggle() } }) {
                HStack {
                    Text(seasonNumber == 0
                         ? MR.strings().specials.localized()
                         : MR.strings().season_label.formatted(args: [seasonNumber]))
                        .font(.headline)
                    
                    Text(MR.plurals().episodes.localized(episodeCount))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    
                    Spacer()
                    
                    Image(systemName: "chevron.down")
                        .rotationEffect(.degrees(isExpanded ? 180 : 0))
                        .animation(.easeInOut(duration: 0.2), value: isExpanded)
                }
                .padding(12)
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
            .buttonStyle(.plain)
            
            if isExpanded {
                VStack(spacing: 0) {
                    ForEach(Array(episodes.enumerated()), id: \.offset) { index, episode in
                        SeerrEpisodeRow(
                            episodeNumber: episode.episodeNumber,
                            name: episode.name,
                            airDate: episode.airDate,
                            overview: episode.overview,
                            stillPath: episode.stillPath
                        )
                        
                        if index < Int(episodeCount) - 1 {
                            Divider()
                                .padding(.horizontal, 8)
                        }
                    }
                }
                .padding(.leading, 8)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
    }
}

// MARK: - Episode Row

private struct SeerrEpisodeRow: View {
    let episodeNumber: Int32
    let name: String
    let airDate: LocalDate?
    let overview: String?
    let stillPath: String?
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .top) {
                Text("\(episodeNumber) - \(name)")
                    .font(.subheadline.bold())
                    .foregroundColor(.accentColor)
                
                Spacer()
                
                if let airDate {
                    Text(airDate.format(pattern: "MMM d, yyyy"))
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            
            if let overview, !overview.isEmpty {
                Text(overview)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(3)
            }
            
            if let stillPath, let url = URL(string: "https://image.tmdb.org/t/p/w500\(stillPath)") {
                AsyncImage(url: url) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color(.systemGray5)
                }
                .frame(height: 120)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 4)
    }
}

// MARK: - Cast Member

private struct CastMemberView: View {
    let member: CastMember
    
    var body: some View {
        VStack(spacing: 4) {
            if let profilePath = member.fullProfilePath,
               let url = URL(string: profilePath) {
                AsyncImage(url: url) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color(.systemGray4)
                }
                .frame(width: 80, height: 80)
                .clipShape(Circle())
            } else {
                Circle()
                    .fill(Color(.systemGray4))
                    .frame(width: 80, height: 80)
                    .overlay {
                        Image(systemName: "person.fill")
                            .foregroundColor(.gray)
                    }
            }
            
            Text(member.name)
                .font(.caption)
                .lineLimit(1)
            
            Text(member.character)
                .font(.caption2)
                .foregroundColor(.secondary)
                .lineLimit(1)
        }
        .frame(width: 80)
    }
}
