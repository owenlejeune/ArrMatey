//
//  MediaDetailsHeader.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-29.
//

import Shared
import SwiftUI

struct MediaDetailsHeader: View {
    let item: ArrMedia
    let type: InstanceType
    
    @Environment(\.colorScheme) var colorScheme
    
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
            MediaHeaderBanner(bannerUrl: URL(string: item.getBanner()?.remoteUrl ?? ""))
            
            HStack(alignment: .bottom, spacing: 24) {
                PosterItem(item: item, aspectRatio: type.aspectRatio)
                    .frame(width: 150)
                
                VStack(alignment: .leading, spacing: 8) {
                    ClearLogoView(item: item)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        let ratings = item.ratings?.toRatingItems() ?? []
                        if !ratings.isEmpty {
                            HStack(spacing: 12) {
                                ForEach(ratings, id: \.self) { rating in
                                    HStack(spacing: 4) {
                                        if let icon = rating.icon {
                                            Image(resource: icon)
                                                .resizable()
                                                .frame(width: 14, height: 14)
                                        } else {
                                            Image(systemName: "star.fill")
                                                .foregroundColor(.yellow)
                                                .font(.system(size: 14))
                                        }
                                        Text(rating.score)
                                            .font(.system(size: 16, weight: .bold))
                                    }
                                }
                            }
                        }

                        if !(item is Arrtist) && !(item is Author) {
                            if !infoString.isEmpty {
                                Text(infoString)
                                    .font(.system(size: 16))
                            }
                            
                            if !(item is Audiobook) {
                                Text([item.releasedBy ?? "", item.statusString].filter { !$0.isEmpty }.joined(separator: " • "))
                                    .font(.system(size: 14))
                            }
                        }
                        
                        Text(item.genres.joined(separator: " • "))
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
    }
}
