//
//  UnifiedMediaDetailsHeader.swift
//  iosApp
//

import SwiftUI
import Shared

struct UnifiedMediaDetailsHeader: View {
    let success: UnifiedMediaDetailsUiStateSuccess
    let type: InstanceType?
    @State private var infoHeight: CGFloat = 0

    private var infoString: String {
        var items: [String] = []
        if let year = success.year {
            items.append(year)
        }
        if let runtime = success.runtimeString, !runtime.isEmpty {
            items.append(runtime)
        }
        if let seasonCount = success.seasonCount {
            items.append(MR.plurals().seasons.localized(seasonCount.intValue))
        }
        if let certification = success.getCertification(countryCode: Locale.current.region?.identifier ?? "") {
            items.append(certification)
        }
        return items.joined(separator: " • ")
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            MediaHeaderBanner(
                bannerUrl: URL(string: success.bannerUrl ?? ""),
                height: 400,
                gradientHeight: infoHeight * 2
            )

            HStack(alignment: .bottom, spacing: 24) {
                if let posterUrl = success.posterUrl {
                    GenericPosterItem(posterUrl: posterUrl)
                        .frame(width: 150)
                } else if let arrMedia = success.arrMedia {
                    PosterItem(item: arrMedia, aspectRatio: type?.aspectRatio ?? .poster)
                        .frame(width: 150)
                }

                VStack(alignment: .leading, spacing: 8) {
                    if let arrMedia = success.arrMedia {
                        ClearLogoView(item: arrMedia)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        let ratings = success.ratings
                        if !ratings.isEmpty {
                            FlowLayout(spacing: 12) {
                                ForEach(ratings, id: \.self) { rating in
                                    HStack(spacing: 4) {
                                        if let icon = rating.icon {
                                            Image(resource: icon)
                                                .resizable()
                                                .frame(width: 16, height: 16)
                                        } else {
                                            Image(systemName: "star.fill")
                                                .foregroundColor(.arrOrange)
                                                .font(.system(size: 16))
                                        }
                                        Text(rating.score)
                                            .font(.system(size: 16, weight: .bold))
                                            .fixedSize(horizontal: true, vertical: false)
                                    }
                                }
                            }
                        }

                        if !infoString.isEmpty {
                            Text(infoString)
                                .font(.system(size: 16))
                        }

                        if let releasedBy = success.releasedBy {
                            Text(releasedBy)
                                .font(.system(size: 14))
                        }

                        Text(success.genres.joined(separator: " • "))
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                    }
                }
                .background(GeometryReader { geometry in
                    Color.clear.preference(key: ViewHeightKey.self, value: geometry.size.height)
                })
                .frame(maxWidth: .infinity, alignment: .leading)
                .onPreferenceChange(ViewHeightKey.self) { height in
                    if height > 0 {
                        self.infoHeight = height
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
        .frame(height: 400)
    }
}
