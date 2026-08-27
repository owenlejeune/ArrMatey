//
//  EpisodeRow.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI
import Shared

struct EpisodeRow: View {
    let episode: EpisodeWrapper
    var searchInProgress: (Int64) -> Bool = { _ in false }
    var onAutomaticSearch: (Int64) -> Void = { _ in }
    var onToggleMonitor: (Episode) -> Void = { _ in }
    var onNavigateToSeriesRelease: ((Int64) -> Void)? = nil
    var onClick: (() -> Void)? = nil
    
    @EnvironmentObject private var navigation: NavigationManager
    
    init(
        episode: EpisodeWrapper,
        searchInProgress: @escaping (Int64) -> Bool = { _ in false },
        onAutomaticSearch: @escaping (Int64) -> Void = { _ in },
        onToggleMonitor: @escaping (Episode) -> Void = { _ in },
        onNavigateToSeriesRelease: ((Int64) -> Void)? = nil,
        onClick: (() -> Void)? = nil
    ) {
        self.episode = episode
        self.searchInProgress = searchInProgress
        self.onAutomaticSearch = onAutomaticSearch
        self.onToggleMonitor = onToggleMonitor
        self.onNavigateToSeriesRelease = onNavigateToSeriesRelease
        self.onClick = onClick
    }
    
    init(
        episode: Episode,
        onToggleEpisodeMonitor: @escaping (Episode) -> Void,
        onAutomaticSearch: @escaping () -> Void,
        automaticSearchDisabled: Bool = false,
        onClicked: @escaping () -> Void
    ) {
        self.episode = EpisodeWrapper(
            arrEpisode: episode,
            seerrEpisode: nil,
            bazarrEpisode: nil,
            isActive: false,
            activityProgress: nil
        )
        self.searchInProgress = { _ in false }
        self.onAutomaticSearch = { _ in onAutomaticSearch() }
        self.onToggleMonitor = onToggleEpisodeMonitor
        self.onNavigateToSeriesRelease = nil
        self.onClick = onClicked
    }
    
    private var arrEp: Episode? {
        episode.arrEpisode
    }
    
    private var statusInfo: (text: String, color: Color, italic: Bool)? {
        if episode.isActive, let progress = episode.activityProgress {
            return (progress, Color.purple, false)
        } else if let quality = episode.fileQualityName {
            return (quality, Color.secondary, false)
        } else if episode.airDate?.isTodayOrAfter() == true {
            return (MR.strings().unaired.localized(), Color.secondary, true)
        } else if arrEp != nil {
            return (MR.strings().missing.localized(), Color.red, true)
        }
        return nil
    }
    
    private var formattedDate: String? {
        episode.formatAirDateUtc() ?? episode.airDate?.format(pattern: "MMM d, yyyy")
    }
    
    private var resolvedStillUrl: URL? {
        guard let stillPath = episode.stillPath, !stillPath.isEmpty else { return nil }
        if stillPath.hasPrefix("http") {
            return URL(string: stillPath)
        } else {
            return URL(string: "https://image.tmdb.org/t/p/w500\(stillPath)")
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            // Main row with title, status, and action buttons
            HStack(alignment: .top, spacing: 8) {
                VStack(alignment: .leading, spacing: 3) {
                    // Title line
                    HStack(spacing: 0) {
                        Text("\(episode.episodeNumber). ")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.themePrimary)
                        
                        Text(episode.title ?? "")
                            .font(.system(size: 16, weight: .medium))
                            .lineLimit(1)
                            .foregroundColor(.primary)
                        
                        if let finaleType = episode.finaleType {
                            Text(" • \(finaleType.resource.localized())")
                                .font(.system(size: 12))
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    // Status & Air date line
                    HStack(spacing: 4) {
                        if let status = statusInfo {
                            Text(status.text)
                                .font(.system(size: 14))
                                .foregroundColor(status.color)
                                .italic(status.italic)
                        }
                        
                        if let dateStr = formattedDate {
                            let isToday = episode.airDate?.isToday() == true
                            let prefix = statusInfo != nil ? " • " : ""
                            Text("\(prefix)\(dateStr)")
                                .font(.system(size: 14))
                                .fontWeight(isToday ? .medium : .regular)
                                .foregroundColor(isToday ? .themePrimary : .secondary)
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .contentShape(Rectangle())
                .onTapGesture {
                    onClick?()
                }
                
                // Action buttons for arr episodes
                if let arrEp = arrEp {
                    HStack(spacing: 12) {
                        Button {
                            if let onNavigateToSeriesRelease = onNavigateToSeriesRelease {
                                onNavigateToSeriesRelease(arrEp.id)
                            } else {
                                let route: MediaRoute = .seriesReleases(seriesId: nil, seasonNumber: nil, episodeId: arrEp.id)
                                navigation.go(to: route, of: .sonarr)
                            }
                        } label: {
                            Image(systemName: "person.fill")
                                .font(.system(size: 16))
                                .foregroundColor(.primary)
                        }
                        .buttonStyle(.plain)
                        
                        let isSearching = searchInProgress(arrEp.id)
                        Button {
                            onAutomaticSearch(arrEp.id)
                        } label: {
                            if isSearching {
                                ProgressView()
                                    .controlSize(.small)
                            } else {
                                Image(systemName: "magnifyingglass")
                                    .font(.system(size: 16))
                                    .foregroundColor(.primary)
                            }
                        }
                        .buttonStyle(.plain)
                        .disabled(!arrEp.monitored || isSearching)
                        
                        Button {
                            onToggleMonitor(arrEp)
                        } label: {
                            Image(systemName: arrEp.monitored ? "bookmark.fill" : "bookmark")
                                .font(.system(size: 16))
                                .foregroundColor(.primary)
                        }
                        .buttonStyle(.plain)
                        .animation(.easeInOut(duration: 0.2), value: arrEp.monitored)
                    }
                    .padding(.top, 2)
                }
            }
            
            // Still image + Overview + Bazarr Subtitles
            let epOverview = episode.overview?.trimmingCharacters(in: .whitespacesAndNewlines)
            let hasOverview = epOverview != nil && !epOverview!.isEmpty
            let hasStill = resolvedStillUrl != nil
            
            if hasStill || hasOverview || episode.bazarrEpisode != nil {
                HStack(alignment: .top, spacing: 10) {
                    if let stillUrl = resolvedStillUrl {
                        AsyncImage(url: stillUrl) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .aspectRatio(1.77, contentMode: .fill)
                            case .failure:
                                Color(.secondarySystemBackground)
                            case .empty:
                                Color(.secondarySystemBackground)
                            @unknown default:
                                Color(.secondarySystemBackground)
                            }
                        }
                        .frame(width: 120, height: 68)
                        .clipped()
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    
                    VStack(alignment: .leading, spacing: 4) {
                        if let overview = epOverview, !overview.isEmpty {
                            Text(overview)
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .lineLimit(3)
                                .truncationMode(.tail)
                        }
                        
                        if let bazarrEp = episode.bazarrEpisode {
                            EpisodeSubtitlesRow(bazarrEpisode: bazarrEp)
                                .padding(.top, 2)
                        }
                    }
                    .frame(maxWidth: .infinity, minHeight: hasStill ? 68 : 0, alignment: .topLeading)
                }
                .contentShape(Rectangle())
                .onTapGesture {
                    onClick?()
                }
            }
        }
        .padding(.vertical, 4)
    }
}

// MARK: - EpisodeSubtitlesRow

struct EpisodeSubtitlesRow: View {
    let bazarrEpisode: BazarrEpisode
    
    private var existingSubs: [BazarrSubtitle] {
        var seen = Set<String>()
        return bazarrEpisode.subtitles.filter { !$0.isEmbedded }.filter { sub in
            let key = "\((sub.code2 ?? "").lowercased())_\(sub.hi)_\(sub.forced)"
            if seen.contains(key) { return false }
            seen.insert(key)
            return true
        }
    }
    
    private var missingSubs: [BazarrSubtitleLanguage] {
        var seen = Set<String>()
        return bazarrEpisode.missingSubtitles.filter { missing in
            let key = "\((missing.code2 ?? "").lowercased())_\(missing.hi)_\(missing.forced)"
            if seen.contains(key) { return false }
            seen.insert(key)
            return true
        }
    }
    
    var body: some View {
        if !existingSubs.isEmpty || !missingSubs.isEmpty {
            FlowLayout(spacing: 4) {
                Image(systemName: "captions.bubble")
                    .font(.system(size: 13))
                    .foregroundColor(.secondary.opacity(0.8))
                    .padding(.trailing, 2)
                
                ForEach(Array(existingSubs.enumerated()), id: \.offset) { _, sub in
                    let label = subtitleLabel(sub)
                    Text(label)
                        .font(.system(size: 10, weight: .medium))
                        .foregroundColor(.primary)
                        .padding(.horizontal, 4)
                        .padding(.vertical, 1)
                        .background(Color(.secondarySystemBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                }
                
                ForEach(Array(missingSubs.enumerated()), id: \.offset) { _, missing in
                    let label = missingLabel(missing)
                    HStack(spacing: 2) {
                        Image(systemName: "exclamationmark")
                            .font(.system(size: 9, weight: .bold))
                        Text(label)
                            .font(.system(size: 10, weight: .medium))
                    }
                    .foregroundColor(.red)
                    .padding(.horizontal, 4)
                    .padding(.vertical, 1)
                    .overlay(
                        RoundedRectangle(cornerRadius: 4)
                            .stroke(Color.red.opacity(0.6), lineWidth: 1)
                    )
                }
            }
        }
    }
    
    private func subtitleLabel(_ subtitle: BazarrSubtitle) -> String {
        let code2 = subtitle.code2 ?? ""
        var label = code2.isEmpty ? subtitle.name : code2.uppercased()
        if subtitle.hi { label += " · HI" }
        if subtitle.forced { label += " · Forced" }
        return label
    }
    
    private func missingLabel(_ lang: BazarrSubtitleLanguage) -> String {
        let code2 = lang.code2 ?? ""
        var label = code2.isEmpty ? lang.name : code2.uppercased()
        if lang.hi { label += " · HI" }
        if lang.forced { label += " · Forced" }
        return label
    }
}
