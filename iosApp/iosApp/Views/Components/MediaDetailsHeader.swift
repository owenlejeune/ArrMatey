//
//  MediaDetailsHeader.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-29.
//

import Shared
import SwiftUI

struct ViewHeightKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}

struct MediaDetailsHeader: View {
    let item: ArrMedia
    let type: InstanceType
    
    @Environment(\.colorScheme) var colorScheme
    @State private var infoHeight: CGFloat = 0
    
    private var infoString: String {
        var result = ""
        if let year = item.year {
            result += "\(year)"
        }
        if !item.runtimeString.isEmpty {
            result += (result.isEmpty ? "" : " • ") + item.runtimeString
        }
        if let certification = item.certification {
            result += (result.isEmpty ? "" : " • ") + certification
        }
        return result
    }
    
    var body: some View {
        ZStack(alignment: .bottom) {
            MediaHeaderBanner(
                bannerUrl: URL(string: item.getBanner()?.remoteUrl ?? ""),
                height: 350,
                gradientHeight: infoHeight * 2
            )
            
            HStack(alignment: .bottom, spacing: 24) {
                PosterItem(item: item, aspectRatio: type.aspectRatio)
                    .frame(width: 150)
                
                VStack(alignment: .leading, spacing: 8) {
                    ClearLogoView(item: item)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        let ratings = item.ratings?.toRatingItems() ?? []
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

                        if !(item is Arrtist) && !(item is Author) {
                            if !infoString.isEmpty {
                                Text(infoString)
                                    .font(.system(size: 16))
                            }
                            
                            if let releasedBy = item.releasedBy {
                                Text(releasedBy)
                                    .font(.system(size: 14))
                            }
                        }
                        
                        Text(item.genres.joined(separator: " • "))
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                            .background(GeometryReader { geometry in
                                Color.clear.preference(key: ViewHeightKey.self, value: geometry.size.height)
                            })
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
        .frame(height: 350)
        .onPreferenceChange(ViewHeightKey.self) { height in
            self.infoHeight = height
        }
    }
}

struct RequestMediaDetailsHeader: View {
    let item: RequestMediaDetails
    @State private var infoHeight: CGFloat = 0
    
    var body: some View {
        ZStack(alignment: .bottom) {
            MediaHeaderBanner(
                bannerUrl: URL(string: item.fullBackdropPath ?? ""),
                height: 350,
                gradientHeight: infoHeight * 2
            )
            
            HStack(alignment: .bottom, spacing: 24) {
                GenericPosterItem(posterUrl: item.fullPosterPath)
                    .frame(width: 150)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(infoString)
                        .font(.system(size: 16))
                        .padding(.top, 6)
                    
                    Text(item.genres.map { $0.name }.joined(separator: " • "))
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                        .lineSpacing(2)
                }
                .background(GeometryReader { geometry in
                    Color.clear.preference(key: ViewHeightKey.self, value: geometry.size.height)
                })
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
        .frame(height: 350)
        .onPreferenceChange(ViewHeightKey.self) { height in
            self.infoHeight = height
        }
    }
    
    private var infoString: String {
        var items: [String] = []
        if let displayDate = item.displayDate {
            items.append(displayDate.format(pattern: "MMM d, yyyy"))
        }
        if let movie = item as? MovieDetails, let runtime = movie.runtime {
            items.append(Int(truncating: runtime).formatAsRuntime())
        }
        if let tv = item as? TvDetails {
            items.append(MR.plurals().seasons.localized(Int(tv.seasons.count)))
        }
        if let certification = item.getCertification(localeCode: Locale.current.region?.identifier ?? "") {
            items.append(certification)
        }
        return items.joined(separator: " • ")
    }
}
