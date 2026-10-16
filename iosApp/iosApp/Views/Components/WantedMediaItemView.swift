//
//  WantedMediaItemView.swift
//  iosApp
//
//  Created by Antigravity on 2026-10-15.
//

import Shared
import SwiftUI

struct WantedMediaItemView: View {
    let item: WantedMediaItem

    private var associatedColor: Color {
        item.instanceType?.associatedSwiftColor ?? .accentColor
    }

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(associatedColor)
                .frame(width: 5)

            HStack(alignment: .top, spacing: 12) {
                if let media = item.media {
                    PosterItem(
                        item: media,
                        instanceType: item.instanceType,
                        aspectRatio: item.instanceType?.aspectRatio ?? .poster,
                        elevation: .none,
                        radius: .small,
                        posterHeight: 80
                    )
                    .frame(width: 55, height: 80)
                } else if let album = item.album {
                    AlbumCoverView(album: album)
                        .frame(width: 55, height: 80)
                        .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                } else {
                    ZStack {
                        Color(uiColor: .secondarySystemBackground)
                        Image(systemName: "film")
                            .font(.system(size: 24))
                            .foregroundColor(.secondary)
                    }
                    .frame(width: 55, height: 80)
                    .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack(alignment: .top) {
                        Text((item.title ?? "").breakable())
                            .font(.system(size: 15, weight: .bold))
                            .lineLimit(2)
                            .foregroundColor(.primary)

                        Spacer()

                        if let instanceName = item.instanceName, !instanceName.isEmpty {
                            Text(instanceName)
                                .font(.system(size: 11, weight: .medium))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color(uiColor: .secondarySystemBackground))
                                .cornerRadius(4)
                                .foregroundColor(.secondary)
                        }
                    }

                    if let year = item.year {
                        Text(String(describing: year))
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.themePrimary)
                    }

                    let overview = item.media?.overview ?? item.album?.overview ?? item.book?.authorTitle
                    if let text = overview, !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                        Text(text.breakable())
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                    }
                }
            }
            .padding(.vertical, 10)
            .padding(.horizontal, 12)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(Color(uiColor: .secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
        )
    }
}
