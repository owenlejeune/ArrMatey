//
//  MediaServerSourceChips.swift
//  iosApp
//

import SwiftUI
import Shared

struct MediaServerSourceChips: View {
    let details: [MediaServerMediaDetails]
    var onWatchClick: ((String, String?) -> Void)? = nil
    var onToggleWatched: ((MediaServerType, String, Bool) -> Void)? = nil

    var body: some View {
        if !details.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(details, id: \.itemId) { detail in
                        chip(for: detail)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func chip(for detail: MediaServerMediaDetails) -> some View {
        let isMovie = detail.totalEpisodesCount == nil
        let (statusText, statusColor): (String, Color) = {
            if detail.played {
                return (MR.strings().watched.localized(), Color(red: 0x50 / 255.0, green: 0xD2 / 255.0, blue: 0x7D / 255.0))
            } else if let progress = detail.progressPercentage?.floatValue, progress > 0 {
                let percent = Int(progress * 100)
                return ("\(percent)%", Color.orange)
            } else if let total = detail.totalEpisodesCount?.int32Value, total > 0 {
                let watched = detail.episodesWatchedCount?.int32Value ?? 0
                return ("\(watched)/\(total)", Color(red: 0x50 / 255.0, green: 0xD2 / 255.0, blue: 0x7D / 255.0))
            } else {
                return (MR.strings().available.localized(), Color.secondary)
            }
        }()

        HStack(spacing: 6) {
            Button {
                if let onWatch = onWatchClick {
                    onWatch(detail.deepLinkUrl, detail.webFallbackUrl)
                } else {
                    openWatchUrl(deepLink: detail.deepLinkUrl, fallback: detail.webFallbackUrl)
                }
            } label: {
                HStack(spacing: 6) {
                    detail.serverType.icon.toImage(renderingMode: .template)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 16, height: 16)
                        .foregroundColor(detail.serverType.brandColor.toSwiftUI())

                    Text(detail.serverLabel)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.primary)

                    Text("• \(statusText)")
                        .font(.system(size: 12))
                        .foregroundColor(statusColor)
                }
            }
            .buttonStyle(.plain)

            if let onToggle = onToggleWatched, isMovie {
                Button {
                    onToggle(detail.serverType, detail.itemId, !detail.played)
                } label: {
                    Image(systemName: detail.played ? "checkmark.circle.fill" : "checkmark.circle")
                        .font(.system(size: 15))
                        .foregroundColor(detail.played ? Color(red: 0x50 / 255.0, green: 0xD2 / 255.0, blue: 0x7D / 255.0) : Color.secondary)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.leading, 10)
        .padding(.trailing, (onToggleWatched != nil && isMovie) ? 6 : 10)
        .padding(.vertical, 6)
        .background(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(Color(.secondarySystemBackground))
        )
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color.primary.opacity(0.08), lineWidth: 1)
        )
    }

    private func openWatchUrl(deepLink: String, fallback: String?) {
        if let deepUrl = URL(string: deepLink), UIApplication.shared.canOpenURL(deepUrl) {
            UIApplication.shared.open(deepUrl)
        } else if let fb = fallback, let fbUrl = URL(string: fb) {
            UIApplication.shared.open(fbUrl)
        }
    }
}

