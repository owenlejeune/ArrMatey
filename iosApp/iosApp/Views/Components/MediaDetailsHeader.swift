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
        guard !(item is Arrtist) else { return "" }
        var result = ""
        if let year = item.year {
            result += "\(year)"
        }
        result += " • \(item.runtimeString)"
        if let certifcation = item.certification {
            result += " • \(certifcation)"
        }
        return result
    }
    
    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .topLeading) {
                MediaHeaderBanner(bannerUrl: URL(string: item.getBanner()?.remoteUrl ?? ""))
                    .frame(width: geometry.size.width)
                
                HStack(alignment: .bottom, spacing: 12) {
                    PosterItem(item: item, aspectRatio: type.aspectRatio)
                        .frame(height: 220)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        ClearLogoView(item: item)
                            .padding(.bottom, 12)
                        
                        if !(item is Arrtist) && !(item is Author) {
                            Text(infoString)
                                .font(.system(size: 16))
                            
                            Text([item.releasedBy ?? "", item.statusString].joined(separator: " • "))
                                .font(.system(size: 14))
                        }
                        
                        Text(item.genres.joined(separator: " • "))
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                        
                    }
                    .frame(maxWidth: .infinity, alignment: .top)
                }
                .padding(.top, 170)
                .padding(.horizontal, 12)
            }
        }
        .frame(maxWidth: .infinity)
    }
}
