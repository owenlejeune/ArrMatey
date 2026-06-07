//
//  MediaHeaderBanner.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-29.
//

import SwiftUI

struct MediaHeaderBanner: View {
    let bannerUrl: URL?
    let height: CGFloat
    
    init(bannerUrl: URL?, height: CGFloat = 400) {
        self.bannerUrl = bannerUrl
        self.height = height
    }
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                Color(.systemBackground)
                
                if let url = bannerUrl {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(width: geometry.size.width, height: geometry.size.height)
                        case .failure(_):
                            Color.gray.opacity(0.3)
                        case .empty:
                            ProgressView()
                        @unknown default:
                            Color.clear
                        }
                    }
                }
            }
            .frame(width: geometry.size.width, height: geometry.size.height)
        }
        .frame(height: height)
        .clipped()
    }
}
