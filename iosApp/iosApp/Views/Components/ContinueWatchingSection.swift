//
//  ContinueWatchingSection.swift
//  iosApp
//

import SwiftUI
import Shared

struct DashboardContinueWatchingSection: View {
    let state: CombinedDashboardStateSuccess
    let isEditing: Bool
    var onItemClick: ((UnifiedMediaItem) -> Void)? = nil
    var onPlayItem: ((UnifiedMediaItem) -> Void)? = nil

    var body: some View {
        let items = state.continueWatching

        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Image(systemName: "tv")
                    .font(.system(size: 16, weight: .bold))
                Text(MR.strings().continue_watching.localized())
                    .font(.headline)
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)

            if !state.hasMediaServers {
                Text(MR.strings().no_media_servers_configured.localized())
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 12)
                    .padding(.horizontal, 16)
            } else if items.isEmpty {
                Text(MR.strings().nothing_to_continue_watching.localized())
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 12)
                    .padding(.horizontal, 16)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(items, id: \.id) { item in
                            ContinueWatchingCard(
                                item: item,
                                isEditing: isEditing,
                                onClick: { onItemClick?(item) },
                                onPlayClick: { onPlayItem?(item) }
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                }
                .disabled(isEditing)
                .padding(.bottom, 16)
            }
        }
        .background(Color(.secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
        )
    }
}

struct ContinueWatchingCard: View {
    let item: UnifiedMediaItem
    let isEditing: Bool
    let onClick: () -> Void
    let onPlayClick: () -> Void

    var body: some View {
        let imageUrl = item.backdropUrl ?? item.stillUrl ?? item.posterUrl

        let (titleText, subtitleText): (String, String) = {
            if item.type == .episode {
                let title = item.seriesTitle ?? item.title
                let s = item.seasonNumber?.intValue ?? 0
                let e = item.episodeNumber?.intValue ?? 0
                let epPrefix = "S\(s):E\(e)"
                let subtitle: String
                if let st = item.seriesTitle, !st.isEmpty, !item.title.isEmpty {
                    subtitle = "\(epPrefix) • \(item.title)"
                } else {
                    subtitle = epPrefix
                }
                return (title, subtitle)
            } else {
                let sub = item.year?.stringValue ?? item.formattedRuntime ?? ""
                return (item.title, sub)
            }
        }()

        let remainingString: String? = {
            let dur = item.durationTicks?.int64Value ?? 0
            let pos = item.playbackPositionTicks?.int64Value ?? 0
            let remainingTicks = max(0, dur - pos)
            let remainingMinutes = Int(remainingTicks / 600_000_000)
            if remainingMinutes >= 60 {
                return "\(remainingMinutes / 60)h \(remainingMinutes % 60)m left"
            } else if remainingMinutes > 0 {
                return "\(remainingMinutes)m left"
            }
            return nil
        }()

        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 6) {
                ZStack(alignment: .bottom) {
                    CachedAsyncImage(url: URL(string: imageUrl ?? "")) { img in
                        img.resizable().aspectRatio(16 / 9, contentMode: .fill)
                    } placeholder: {
                        Color(.systemGray5)
                            .overlay(Image(systemName: "film").foregroundColor(.secondary))
                    }
                    .frame(width: 220, height: 124)
                    .clipped()

                    // Dim scrim overlay
                    Color.black.opacity(0.25)
                        .frame(width: 220, height: 124)

                    // Server type badge (top-leading)
                    HStack(spacing: 4) {
                        item.serverType.icon.toImage(renderingMode: .template)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 12, height: 12)
                            .foregroundColor(item.serverType.brandColor.toSwiftUI())
                        Text(item.serverType.name)
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color.black.opacity(0.65))
                    .cornerRadius(4)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                    .padding(6)

                    // Remaining time badge (top-trailing)
                    if let rem = remainingString {
                        Text(rem)
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.black.opacity(0.65))
                            .cornerRadius(4)
                            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                            .padding(6)
                    }

                    // Play button (center)
                    Button(action: onPlayClick) {
                        Circle()
                            .fill(Color.black.opacity(0.6))
                            .frame(width: 40, height: 40)
                            .overlay(
                                Circle()
                                    .stroke(Color.white.opacity(0.3), lineWidth: 1)
                            )
                            .overlay(
                                Image(systemName: "play.fill")
                                    .font(.system(size: 16))
                                    .foregroundColor(.white)
                            )
                    }
                    .buttonStyle(.plain)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)

                    // Progress bar (bottom)
                    if let progress = item.progressPercentage?.floatValue, progress > 0 {
                        GeometryReader { geo in
                            VStack {
                                Spacer()
                                ZStack(alignment: .leading) {
                                    Rectangle()
                                        .fill(Color.white.opacity(0.3))
                                        .frame(height: 3)
                                    Rectangle()
                                        .fill(Color.accentColor)
                                        .frame(width: geo.size.width * CGFloat(min(1.0, max(0.0, progress))), height: 3)
                                }
                            }
                        }
                        .frame(width: 220, height: 124)
                    }
                }
                .frame(width: 220, height: 124)
                .cornerRadius(8)

                VStack(alignment: .leading, spacing: 2) {
                    Text(titleText)
                        .font(.system(size: 13, weight: .bold))
                        .lineLimit(1)
                        .foregroundColor(.primary)

                    if !subtitleText.isEmpty {
                        Text(subtitleText)
                            .font(.system(size: 11))
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
                .frame(width: 220, alignment: .leading)
            }
        }
        .buttonStyle(.plain)
        .disabled(isEditing)
    }
}

