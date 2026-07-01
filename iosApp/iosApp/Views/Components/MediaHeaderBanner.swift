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
    let gradientHeight: CGFloat
    
    init(bannerUrl: URL?, height: CGFloat = 400, gradientHeight: CGFloat = 150) {
        self.bannerUrl = bannerUrl
        self.height = height
        self.gradientHeight = gradientHeight
    }
    
    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .bottom) {
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
                
                LinearGradient(
                    gradient: Gradient(colors: [
                        .clear,
                        Color(.systemBackground).opacity(0.8),
                        Color(.systemBackground)
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(height: max(gradientHeight, 150))
            }
            .frame(width: geometry.size.width, height: geometry.size.height)
        }
        .frame(height: height)
        .clipped()
    }
}
